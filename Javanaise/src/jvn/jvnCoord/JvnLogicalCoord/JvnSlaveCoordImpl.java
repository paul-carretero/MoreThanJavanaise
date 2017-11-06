package jvn.jvnCoord.JvnLogicalCoord;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

/**
 * @author Paul Carretero
 * Coordinateur slave
 * Recoit des mise à jour d'un coordinateur master.
 * Peut, à la demande du loadbalancer, créer un coordinateur master avec ses données et se détruire.
 * est associé qu'à un unique master (et donc à son id)
 */
public class JvnSlaveCoordImpl extends JvnAbstractCoord{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7394367557661106177L;

	/**
	 * id de ce coordinateur
	 */
	private final int id;

	/**
	 * constructeur par défault
	 * @param id l'id associé à ce slave coordinateur
	 * @throws RemoteException
	 * @throws MalformedURLException
	 * @throws JvnException
	 */
	public JvnSlaveCoordImpl(int id) throws RemoteException, MalformedURLException, JvnException {
		super();
		this.id = id;
		Naming.rebind(HOST+"JvnCoordSlave_"+ this.id, this);
		System.out.println("[COORDINATEUR] [SLAVE] ["+this.id+"] [UP]");
		try {
			JvnSlaveInitData data	= ((JvnRemoteCoordExtended) this.rmiRegistry.lookup("JvnCoord_"+ this.id)).getData();
			this.jvnObjects 		= data.getJvnObjects();
			this.objectLocks.putAll(data.getObjectLocks());
			this.waitingWriters.putAll(data.getWaitingWriters());
		} catch (Exception e) {
			System.err.println(e.getMessage());
			kill();
		}
	}

	/**
	 * ordonné et séquentiel par objet par le master dans l'ordre de traitement
	 * Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object 
	 * @param js  : the remote reference of the JVNServer
	 **/
	@Override
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws RemoteException, JvnException{
		this.jvnObjects.put(jo, jon, js);
		this.objectLocks.put(jo.jvnGetObjectId(), new ReentrantLock(true));
		this.waitingWriters.put(jo.jvnGetObjectId(), new AtomicInteger(0));
	}

	// ordonné et séquentiel par objet par le master dans l'ordre de traitement
	@Override
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		throw new JvnException("need serializable");
	}

	// ordonné et séquentiel par objet par le master dans l'ordre de traitement
	@Override
	public void jvnLockReadSync(Serializable o, int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		if(this.jvnObjects.getWritingServer(joi) != null && !this.jvnObjects.getWritingServer(joi).equals(js)) {
			this.jvnObjects.addReadingServer(joi, this.jvnObjects.getWritingServer(joi));
			this.jvnObjects.setWritingServer(joi, null);
		}
		this.jvnObjects.addReadingServer(joi, js);
		this.jvnObjects.get(joi).setSerializableObject(o);
	}

	// ordonné et séquentiel par objet par le master dans l'ordre de traitement
	@Override
	public void jvnLockWriteSync(Serializable o, int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		this.jvnObjects.get(joi).setSerializableObject(o);
		this.jvnObjects.resetReadingServer(joi);
		this.jvnObjects.setWritingServer(joi, js);
	}

	@Override
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		throw new JvnException("need serializable");
	}

	@SuppressWarnings("unused")
	@Override
	public void upgrade() throws RemoteException, JvnException {
		try {
			new JvnMasterCoordImpl(this.jvnObjects, this.objectLocks, this.waitingWriters, this.id);
			this.kill();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void kill() {
		try {
			Naming.unbind(HOST+"JvnCoordSlave_"+ this.id);
			unexportObject(this,true);
			this.finalize();
		} catch (@SuppressWarnings("unused") Throwable e) {}
		System.out.println("[COORDINATEUR] [SLAVE] ["+this.id+"] [DOWN]");
	}

	@Override
	public void ping() throws RemoteException {}

	@Override
	public JvnSlaveInitData getData() throws RemoteException, JvnException {
		return new JvnSlaveInitData(this.waitingWriters, this.objectLocks, this.jvnObjects);
	}
}
