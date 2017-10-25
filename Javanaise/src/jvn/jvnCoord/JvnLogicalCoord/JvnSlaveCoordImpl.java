package jvn.jvnCoord.JvnLogicalCoord;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

public class JvnSlaveCoordImpl extends JvnAbstractCoord{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 7394367557661106177L;
	private final int id;

	public JvnSlaveCoordImpl(int id) throws RemoteException, MalformedURLException, JvnException {
		super();
		this.id = id;
		Naming.rebind(HOST+"JvnCoordSlave_"+ this.id, this);
		System.out.println("[COORDINATEUR] [SLAVE] ["+this.id+"]");
	}

	/**
	 * ordonné et séquentiel par objet par le master dans l'ordre de traitement
	 * Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object 
	 * @param js  : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.jvnExceptions.JvnException{
		jo.defaultLock();
		this.jvnObjects.put(jo, jon, js);
		this.objectLocks.put(jo.jvnGetObjectId(), new ReentrantLock(true));
		this.waitingWriters.put(jo.jvnGetObjectId(), new AtomicInteger(0));
	}

	// ordonné et séquentiel par objet par le master dans l'ordre de traitement
	@Override
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		if(this.jvnObjects.getWritingServer(joi) != null && !this.jvnObjects.getWritingServer(joi).equals(js)) {
			this.jvnObjects.addReadingServer(joi, this.jvnObjects.getWritingServer(joi));
			this.jvnObjects.setWritingServer(joi, null);
		}
		this.jvnObjects.addReadingServer(joi, js);
		return null;
	}

	// ordonné et séquentiel par objet par le master dans l'ordre de traitement
	@Override
	public Serializable jvnLockWrite(Serializable o, int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		this.jvnObjects.get(joi).setSerializableObject(o);
		this.jvnObjects.resetReadingServer(joi);
		this.jvnObjects.setWritingServer(joi, js);
		return null;
	}

	@Override
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		throw new JvnException("need serializable");
	}

	@Override
	public void upgrade() throws RemoteException, JvnException {
		try {
			new JvnMasterCoordImpl(this.jvnObjects, this.objectLocks, this.waitingWriters, this.id);
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
		} catch (NotBoundException | RemoteException | MalformedURLException e) {
			//e.printStackTrace(); // osef
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
