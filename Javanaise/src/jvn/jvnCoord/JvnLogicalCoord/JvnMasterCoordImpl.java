package jvn.jvnCoord.JvnLogicalCoord;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jvn.jvnCoord.JvnLogicalCoord.runnables.JvnCoordNotifyWorker;
import jvn.jvnCoord.JvnLogicalCoord.runnables.JvnInvalidateKey;
import jvn.jvnCoord.JvnLogicalCoord.runnables.JvnLockRead;
import jvn.jvnCoord.JvnLogicalCoord.runnables.JvnLockWrite;
import jvn.jvnCoord.JvnLogicalCoord.runnables.JvnRegister;
import jvn.jvnCoord.JvnLogicalCoord.runnables.JvnTerminate;
import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

/**
 * @author Paul Carretero
 * Coordinateur Master
 * Traite les demande des serveur javanaise client.
 * Gère notamenent les demande de verrou, les terminaison de serveur etc.
 * Envoie de requête de synchronisation asynchrone a un coordinateur slave afin de proposer une redondance
 */
public class JvnMasterCoordImpl extends JvnAbstractCoord {
	
	/**
	 * serialVersionUID
	 */
	private static final long 		serialVersionUID = -618926970037788035L;
	
	/**
	 * id de ce coordinateur master
	 */
	private final int 				id;
	
	/**
	 * pool de thread en attente si le verrou en lecture n'est pas disponible 
	 * afin de ne pas trop avantagé les lecteur
	 */
	private final Executor 			waitThreadPool;
	
	/**
	 * Nombre de thread disponible sur la machine physique
	 */
	private static final int		numberOfThread	= Runtime.getRuntime().availableProcessors();
	
	/**
	 * Tableau d'executor FIFO (newSingleThreadExecutor)
	 * Permet la synchro avec le slave dans le même ordre que celui dont les reqûete sont traitée ici
	 */
	private final ExecutorService[] syncExecutors 	= new ExecutorService[numberOfThread];
	
	/**
	 * délai avant un tentative de reconnection au slave/serveur client
	 */
	private static final int		TIMEOUT			= 3000;
	
	/**
	 * Coordinateur slave sur le même id que ce coordinateur master
	 */
	private JvnRemoteCoordExtended 	slave;
	
	/**
	 * Lock de synchronization
	 * Permet d'éviter qu'une operation avec un serveur client soit en cours lorsque ce coordinateur 
	 * sera détruit (pour être déplacer sur une autre machine physique)
	 */
	private final ReadWriteLock		syncLock;

	/**
	 * Constructeur par défault
	 * Utilisé par les machine physique au démarrage pour créer un nouveau coordinateur master
	 * Ne sera pas réutilisé par la suite
	 * @param id id de ce coordinateur
	 * @throws RemoteException
	 * @throws MalformedURLException
	 * @throws JvnException
	 */
	public JvnMasterCoordImpl(int id) throws RemoteException, MalformedURLException, JvnException {
		super();
		this.jvnObjects		= new JvnObjectMapCoord();
		this.waitThreadPool	= Executors.newCachedThreadPool();
		this.id 			= id;
		this.syncLock 		= new ReentrantReadWriteLock();
		
		for(int i = 0; i < numberOfThread; i++) {
			this.syncExecutors[i] = Executors.newSingleThreadExecutor();
		}
		
		Naming.rebind(HOST+"JvnCoord_"+ this.id, this);
		System.out.println("[COORDINATEUR] [MASTER] ["+this.id+"] [NEW] [UP]");
	}
	
	/**
	 * Constructeur d'upgrade.
	 * Sera utilisé par un slave afin de se terminé et de se relancer en temps que master
	 * @param objectMap map des objets javanaise traité par le slave
	 * @param objectLocks map des verrou associés aux objets javanaise du slave
	 * @param waitingWriters Nombre de thread en attente de verrou en lecture pour chaque objet JVN
	 * @param id id de ce coordinateur
	 * @throws RemoteException
	 * @throws MalformedURLException
	 * @throws JvnException
	 */
	public JvnMasterCoordImpl(JvnObjectMapCoord objectMap, Map<Integer,Lock> objectLocks, Map<Integer,AtomicInteger> waitingWriters, int id) throws RemoteException, MalformedURLException, JvnException {
		super();
		this.waitThreadPool	= Executors.newCachedThreadPool();
		this.id 			= id;
		this.syncLock 		= new ReentrantReadWriteLock();
		
		for(int i = 0; i < numberOfThread; i++) {
			this.syncExecutors[i] = Executors.newSingleThreadExecutor();
		}
		
		this.waitingWriters = waitingWriters;
		this.objectLocks 	= objectLocks;
		this.jvnObjects		= objectMap;
		Naming.rebind(HOST+"JvnCoord_"+ this.id, this);
		System.out.println("[COORDINATEUR] [MASTER] ["+this.id+"] [UPGRADE] [UP]");
	}
	
	@Override
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws RemoteException, JvnException{
		this.syncLock.readLock().lock();
		synchronized (jon.intern()) {
			this.jvnObjects.put(jo, jon, js);
			this.objectLocks.put(jo.jvnGetObjectId(), new ReentrantLock(true));
			this.waitingWriters.put(jo.jvnGetObjectId(), new AtomicInteger(0));
			slaveSynchronize(jo.jvnGetObjectId(), new JvnRegister(this, this.slave, jon, jo, js));
		}
		this.syncLock.readLock().unlock();
	}
	
	/**
	 * met à jour le coordinateur slave associé à ce coordinateur master (si besoin)
	 * @return l'adresse du slave de ce coordinateur master ou null si il n'existe pas
	 */
	synchronized public JvnRemoteCoordExtended updateSlave() {
		try {
			this.slave = ((JvnRemoteCoordExtended) this.rmiRegistry.lookup("JvnCoordSlave_"+ this.id));
		} catch (@SuppressWarnings("unused") RemoteException | NotBoundException e) {
			try {
				Thread.sleep(TIMEOUT);
				this.slave = ((JvnRemoteCoordExtended) this.rmiRegistry.lookup("JvnCoordSlave_"+ this.id));
			} catch (@SuppressWarnings("unused") Exception e1) {
				this.slave = null;
			}
		}
		return this.slave;
	}

	/**
	 * grâce à l'id de l'objet, la tache est repartie dans un des executor fifo.
	 * Les operation sur un objets se feront donc dans l'ordre sur le slave
	 * @param joi id d'un objet javanaise
	 * @param r une tache (runnable) chargé de mettre à jour un objet javanaise
	 */
	private void slaveSynchronize(int joi, Runnable r) {
		this.syncLock.readLock().lock();
	    int h = (joi+42) % numberOfThread; // 42 => noise (double usage de "%")
	    this.syncExecutors[h].submit(r);
	    this.syncLock.readLock().unlock();
	}
	
	/**
	 * effectue le transfert de verrou vers le serveur client js
	 * @param joi id d'un objet javanaise sur lequel la demande de verrou en lecture s'effectue
	 * @param js le serveur client à l'origine de cette demande
	 * @return l'objet applicatif
	 * @throws JvnException
	 */
	public Serializable jvnLockReadHandler(int joi, JvnRemoteServer js) throws JvnException {
		this.syncLock.readLock().lock();
		this.objectLocks.get(joi).lock();
		if(this.jvnObjects.getWritingServer(joi) != null && !this.jvnObjects.getWritingServer(joi).equals(js)) {
			try {
				this.jvnObjects.get(joi).setSerializableObject(this.jvnObjects.getWritingServer(joi).jvnInvalidateWriterForReader(joi));
			} catch (@SuppressWarnings("unused") RemoteException e) {
				jvnRemoveOnFail(this.jvnObjects.getWritingServer(joi));
				this.jvnObjects.setWritingServer(joi, null);
			}
			
			if(this.jvnObjects.getWritingServer(joi) != null) {
				this.jvnObjects.addReadingServer(joi, this.jvnObjects.getWritingServer(joi));
			}
			this.jvnObjects.setWritingServer(joi, null);
		}
		this.jvnObjects.addReadingServer(joi, js);
		Serializable o = this.jvnObjects.get(joi).jvnGetObjectState();
		slaveSynchronize(joi, new JvnLockRead(this, this.slave, joi, js, o));
		this.objectLocks.get(joi).unlock();
		this.syncLock.readLock().unlock();
		return o;
	}

	/**
	 * effectue le transfert de verrou vers le serveur client js
	 * @param joi id d'un objet javanaise sur lequel la demande de verrou en ecriture s'effectue
	 * @param js le serveur client à l'origine de cette demande
	 * @return l'objet applicatif
	 * @throws JvnException
	 */
	public Serializable jvnLockWriteHandler(int joi, JvnRemoteServer js) throws JvnException {
		this.syncLock.readLock().lock();
		this.objectLocks.get(joi).lock();

		if(this.jvnObjects.getWritingServer(joi) != null && !this.jvnObjects.getWritingServer(joi).equals(js)) {
			try {
				Serializable o = this.jvnObjects.getWritingServer(joi).jvnInvalidateWriter(joi);
				this.jvnObjects.get(joi).setSerializableObject(o);
			} catch (@SuppressWarnings("unused") RemoteException e) {
				jvnRemoveOnFail(this.jvnObjects.getWritingServer(joi));
			}
		}
		
		for(JvnRemoteServer server : this.jvnObjects.getReadingServer(joi)) {
			if(!server.equals(js)) {
				try {
					server.jvnInvalidateReader(joi);
				} catch (@SuppressWarnings("unused") RemoteException e) {
					jvnRemoveOnFail(this.jvnObjects.getWritingServer(joi));
				}
			}
		}
		this.jvnObjects.resetReadingServer(joi);

		this.jvnObjects.setWritingServer(joi, js);
		this.waitingWriters.get(joi).decrementAndGet();
		synchronized (this.waitingWriters.get(joi)) {
			this.waitingWriters.get(joi).notifyAll();
		}

		Serializable o = this.jvnObjects.get(joi).jvnGetObjectState();
		slaveSynchronize(joi, new JvnLockWrite(this, this.slave, joi, js, o));
		this.objectLocks.get(joi).unlock();
		this.syncLock.readLock().unlock();
		return o;
	}
	
	@Override
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws RemoteException, JvnException{
		this.syncLock.readLock().lock();
		if(TRYLOCK_ON_READ) {
			if(this.waitingWriters.get(joi).get() == 0 && this.objectLocks.get(joi).tryLock()) {
				Serializable o = jvnLockReadHandler(joi,js);
				this.objectLocks.get(joi).unlock();
				this.syncLock.readLock().unlock();
				return o;
			}
			this.waitThreadPool.execute(new JvnCoordNotifyWorker(this, joi, js, true));
			this.syncLock.readLock().unlock();
			return null;
		}
		waitOnWW(joi);
		this.syncLock.readLock().unlock();
		return jvnLockReadHandler(joi,js);
	}
	
	@Override
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		this.syncLock.readLock().lock();
		if(this.waitingWriters.get(joi) == null) {
			this.waitingWriters.put(joi, new AtomicInteger(0));
		}
		this.waitingWriters.get(joi).incrementAndGet();
		try {
			if(this.objectLocks.get(joi).tryLock(MAX_WAIT_TIME_BEFORE_QUEUEING, TimeUnit.MILLISECONDS)) {
				Serializable o = jvnLockWriteHandler(joi,js);
				this.objectLocks.get(joi).unlock();
				this.syncLock.readLock().unlock();
				return o;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.waitThreadPool.execute(new JvnCoordNotifyWorker(this, joi, js, false));
		this.syncLock.readLock().unlock();
		return null;
	}


	@Override
	public void jvnLockWriteSync(Serializable o, int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		throw new JvnException("wrong call");
	}
	
	@Override
	public void jvnTerminate(JvnRemoteServer js) throws RemoteException {
		this.syncLock.readLock().lock();
		super.jvnTerminate(js);
		slaveSynchronize(42, new JvnTerminate(this, this.slave, js));
		this.syncLock.readLock().unlock();
	}

	@Override
	public void invalidateKey(int joi, Serializable o, JvnRemoteServer js) throws RemoteException {
		this.syncLock.readLock().lock();
		super.invalidateKey(joi, o, js);
		slaveSynchronize(joi, new JvnInvalidateKey(this, this.slave, joi, o, js));
		this.syncLock.readLock().unlock();
	}

	@Override
	public void upgrade() throws RemoteException, JvnException {
		throw new JvnException("operation impossible");
	}
	
	@Override
	public void kill() {
		this.syncLock.writeLock().lock();
		try {
			Naming.unbind(HOST+"JvnCoord_"+ this.id);
		 	unexportObject(this,true);
		 	this.finalize();
		} catch (@SuppressWarnings("unused") Throwable e) {}
		finally {
			this.syncLock.writeLock().unlock();
		}
		System.out.println("[COORDINATEUR] [MASTER] ["+this.id+"] [DOWN]");
	}

	@Override
	public void ping() throws RemoteException {}

	@Override
	public JvnSlaveInitData getData() throws RemoteException, JvnException {
		return new JvnSlaveInitData(this.waitingWriters, this.objectLocks, this.jvnObjects);
	}

	@Override
	public void jvnLockReadSync(Serializable o, int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		throw new JvnException("wrong call");
	}
}
