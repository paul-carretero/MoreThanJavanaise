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

import jvn.jvnCoord.JvnLogicalCoord.runnables.JvnInvalidateKey;
import jvn.jvnCoord.JvnLogicalCoord.runnables.JvnLockRead;
import jvn.jvnCoord.JvnLogicalCoord.runnables.JvnLockWrite;
import jvn.jvnCoord.JvnLogicalCoord.runnables.JvnRegister;
import jvn.jvnCoord.JvnLogicalCoord.runnables.JvnTerminate;
import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

public class JvnMasterCoordImpl extends JvnAbstractCoord {
	
	/**
	 * serialVersionUID
	 */
	private static final long 		serialVersionUID = -618926970037788035L;
	private final int 				id;
	private final Executor 			waitThreadPool;
	
	private static final int		numberOfThread	= Runtime.getRuntime().availableProcessors();
	private final ExecutorService[] syncExecutors 	= new ExecutorService[numberOfThread];
	
	private JvnRemoteCoord 			slave;
	private final ReadWriteLock		syncLock;

	public JvnMasterCoordImpl(int id) throws RemoteException, MalformedURLException, JvnException {
		super();
		this.waitThreadPool	= Executors.newCachedThreadPool();
		this.id 			= id;
		this.syncLock 		= new ReentrantReadWriteLock();
		
		for(int i = 0; i < numberOfThread; i++) {
			this.syncExecutors[i] = Executors.newSingleThreadExecutor();
		}
		Naming.rebind(HOST+"JvnCoord_"+ this.id, this);
		System.out.println("[COORDINATEUR] [MASTER] ["+this.id+"] [1]");
	}
	
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
		System.out.println("[COORDINATEUR] [MASTER] ["+this.id+"] [2]");
	}
	
	/**
	 * Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object 
	 * @param js  : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.jvnExceptions.JvnException{
		this.syncLock.readLock().lock();
		synchronized (jon.intern()) {
			jo.defaultLock();
			this.jvnObjects.put(jo, jon, js);
			this.objectLocks.put(jo.jvnGetObjectId(), new ReentrantLock(true));
			this.waitingWriters.put(jo.jvnGetObjectId(), new AtomicInteger(0));
			slaveSynchronize(jo.jvnGetObjectId(), new JvnRegister(this.slave, jon, jo, js));
		}
		this.syncLock.readLock().unlock();
	}
	

	public void slaveSynchronize(int joi, Runnable r) {
		this.syncLock.readLock().lock();
	    int h = joi % numberOfThread;
	    this.syncExecutors[h].submit(r);
	}
	
	public Serializable jvnLockReadHandler(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		this.syncLock.readLock().lock();
		this.objectLocks.get(joi).lock();
		if(this.jvnObjects.getWritingServer(joi) != null && !this.jvnObjects.getWritingServer(joi).equals(js)) {
			this.jvnObjects.get(joi).setSerializableObject(this.jvnObjects.getWritingServer(joi).jvnInvalidateWriterForReader(joi));
			this.jvnObjects.addReadingServer(joi, this.jvnObjects.getWritingServer(joi));
			this.jvnObjects.setWritingServer(joi, null);
		}
		this.jvnObjects.addReadingServer(joi, js);
		Serializable o = this.jvnObjects.get(joi).jvnGetObjectState();
		slaveSynchronize(joi, new JvnLockRead(this.slave, joi, js));
		this.objectLocks.get(joi).unlock();
		this.syncLock.readLock().unlock();
		return o;
	}

	public Serializable jvnLockWriteHandler(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		this.syncLock.readLock().lock();
		this.objectLocks.get(joi).lock();

		if(this.jvnObjects.getWritingServer(joi) != null && !this.jvnObjects.getWritingServer(joi).equals(js)) {
			this.jvnObjects.get(joi).setSerializableObject(this.jvnObjects.getWritingServer(joi).jvnInvalidateWriter(joi));
		}
		
		for(JvnRemoteServer server : this.jvnObjects.getReadingServer(joi)) {
			if(!server.equals(js)) {
				server.jvnInvalidateReader(joi);
			}
		}
		this.jvnObjects.resetReadingServer(joi);

		this.jvnObjects.setWritingServer(joi, js);
		this.waitingWriters.get(joi).decrementAndGet();
		synchronized (this.waitingWriters.get(joi)) {
			this.waitingWriters.get(joi).notifyAll();
		}

		Serializable o = this.jvnObjects.get(joi).jvnGetObjectState();
		slaveSynchronize(joi, new JvnLockWrite(this.slave, joi, js, o));
		this.objectLocks.get(joi).unlock();
		return o;
	}
	
	/**
	 * Get a Read lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	@Override
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
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
	
	/**
	 * Get a Write lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	@Override
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		this.syncLock.readLock().lock();
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
	public Serializable jvnLockWrite(Serializable o, int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		throw new JvnException("wrong call");
	}
	
	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	@Override
	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		this.syncLock.readLock().lock();
		super.jvnTerminate(js);
		slaveSynchronize(1, new JvnTerminate(this.slave, js));
		this.syncLock.readLock().unlock();
	}

	@Override
	public void invalidateKey(int joi, Serializable o, JvnRemoteServer js) {
		this.syncLock.readLock().lock();
		super.invalidateKey(joi, o, js);
		slaveSynchronize(joi, new JvnInvalidateKey(this.slave, joi, o, js));
		this.syncLock.readLock().unlock();
	}

	@Override
	public void upgrade() throws RemoteException, JvnException {
		throw new JvnException("déjà master lol");
	}
	
	@Override
	public void kill() {
		this.syncLock.writeLock().lock();
		try {
			Naming.unbind(HOST+"JvnCoord_"+ this.id);
		 	unexportObject(this,true);
		 	this.finalize();
		} catch (@SuppressWarnings("unused") NotBoundException | RemoteException | MalformedURLException e) {
			//e.printStackTrace(); // osef
		} catch (Throwable e) {
			e.printStackTrace();
		}
		finally {
			this.syncLock.writeLock().unlock();
		}
	}
}