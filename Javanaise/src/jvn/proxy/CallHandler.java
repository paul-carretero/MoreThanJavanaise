package jvn.proxy;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import jvn.jvnCoord.JvnRemoteCoord;
import jvn.jvnCoord.jvnLoadBalancer.JvnLoadBalancer;
import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

/**
 * @author Paul Carretero
 * Facade/Interpteur permettant au JvnServeur de fonctionner comme sur Javanaise V2 
 * de manière transparente avec le loadbalancing et la gestion des erreurs coordinateur
 */
public class CallHandler implements JvnRemoteCoord{

	private final Registry		rmiRegistry;
	private JvnLoadBalancer		jvnloadBalancer;
	private JvnRemoteCoord[]	jvnCoords;
	private final int 			numberOfCoords;
	private static final String HOST 	= "localhost";
	private static final int	WAIT_DELAY	= 3000;

	public CallHandler() throws RemoteException, NotBoundException, JvnException {
		this.rmiRegistry		= LocateRegistry.getRegistry(HOST);
		this.jvnloadBalancer 	= (JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancer");
		this.numberOfCoords 	= this.jvnloadBalancer.jvnGetNumberCoord();
		this.jvnCoords 			= new JvnRemoteCoord[this.numberOfCoords];
		for(int i = 0; i < this.numberOfCoords; i++) {
			this.jvnCoords[i] 	=  (JvnRemoteCoord) this.rmiRegistry.lookup("JvnCoord_"+i);
		}
	}
	
	synchronized private void refreshCoord(int id) {
		try {
			Thread.sleep(WAIT_DELAY);
			this.jvnCoords[id] = (JvnRemoteCoord) this.rmiRegistry.lookup("JvnCoord_"+id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int jvnGetObjectId() throws RemoteException, JvnException {
		try {
			return this.jvnloadBalancer.jvnGetObjectId();
		} catch (Exception e) {
			try {
				Thread.sleep(WAIT_DELAY);
				this.jvnloadBalancer = (JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancer");
				return this.jvnloadBalancer.jvnGetObjectId();
			} catch (InterruptedException | NotBoundException e1) {
				e.printStackTrace();
				e1.printStackTrace();
				throw new JvnException();
			}
		}

	}

	@Override
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws RemoteException, JvnException {
		int idCoord = jo.jvnGetObjectId() % this.numberOfCoords;
		try {
			this.jvnCoords[idCoord].jvnRegisterObject(jon, jo, js);
		} catch (@SuppressWarnings("unused") Exception e) {
			refreshCoord(idCoord);
			this.jvnCoords[idCoord].jvnRegisterObject(jon, jo, js);
		}
	}

	@Override
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws RemoteException, JvnException {
		JvnObject o = null;
		for(int idCoord = 0; idCoord < this.numberOfCoords; idCoord++) {
			try {
				o = this.jvnCoords[idCoord].jvnLookupObject(jon, js);
			} catch (@SuppressWarnings("unused") Exception e) {
				refreshCoord(idCoord);
				o = this.jvnCoords[idCoord].jvnLookupObject(jon, js);
			}
			if(o != null) {
				return o;
			}
		}
		return null;
	}

	@Override
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		int idCoord = joi % this.numberOfCoords;
		try {
			return this.jvnCoords[idCoord].jvnLockRead(joi, js);
		} catch (@SuppressWarnings("unused") Exception e) {
			try {
				refreshCoord(idCoord);
				return this.jvnCoords[idCoord].jvnLockRead(joi, js);
			} catch (@SuppressWarnings("unused") Exception e1) {
				refreshCoord(idCoord);
				return this.jvnCoords[idCoord].jvnLockRead(joi, js);
			}
		}
	}

	@Override
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		int idCoord = joi % this.numberOfCoords;
		try {
			return this.jvnCoords[idCoord].jvnLockWrite(joi, js);
		} catch (@SuppressWarnings("unused") Exception e) {
			try {
				refreshCoord(idCoord);
				return this.jvnCoords[idCoord].jvnLockWrite(joi, js);
			} catch (@SuppressWarnings("unused") Exception e1) {
				refreshCoord(idCoord);
				return this.jvnCoords[idCoord].jvnLockWrite(joi, js);
			}
		}
	}

	@Override
	public void jvnTerminate(JvnRemoteServer js) throws RemoteException, JvnException {
		for(int idCoord = 0; idCoord < this.numberOfCoords; idCoord++) {
			try {
				this.jvnCoords[idCoord].jvnTerminate(js);
			} catch (@SuppressWarnings("unused") RemoteException e) {
				System.out.println(idCoord);
				refreshCoord(idCoord);
				this.jvnCoords[idCoord].jvnTerminate(js);
			}
		}
	}

	@Override
	public void invalidateKey(int joi, Serializable o, JvnRemoteServer js) throws RemoteException, JvnException {
		int idCoord = joi % this.numberOfCoords;
		try {
			this.jvnCoords[idCoord].invalidateKey(joi, o, js);
		} catch (@SuppressWarnings("unused") Exception e) {
			refreshCoord(idCoord);
			this.jvnCoords[idCoord].invalidateKey(joi, o, js);
		}
	}
}
