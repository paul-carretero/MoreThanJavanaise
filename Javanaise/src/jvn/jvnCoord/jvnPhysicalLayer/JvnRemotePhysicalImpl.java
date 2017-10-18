package jvn.jvnCoord.jvnPhysicalLayer;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import jvn.jvnCoord.jvnLoadBalancer.JvnAbstractLoadBalancer;
import jvn.jvnCoord.jvnLoadBalancer.JvnCoordData;
import jvn.jvnCoord.jvnLoadBalancer.JvnMasterLoadBalancerImpl;
import jvn.jvnCoord.jvnLoadBalancer.JvnSlaveLoadBalancerImpl;
import jvn.jvnExceptions.JvnException;

public class JvnRemotePhysicalImpl extends UnicastRemoteObject implements JvnRemotePhysical{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -3473522236789019332L;
	
	private JvnAbstractLoadBalancer remoteLoadBalancer;

	public JvnRemotePhysicalImpl() throws RemoteException {
		super();
		this.remoteLoadBalancer = null;
	}

	@Override
	public void jvnNewCoordInstance(JvnCoordData data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void jvnNewSlaveLoadBalancer() throws RemoteException {
		try {
			this.remoteLoadBalancer = new JvnSlaveLoadBalancerImpl();
		} catch (MalformedURLException | JvnException | NotBoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void ping() throws RemoteException {}
	
	/**
	 * Méthode d'initiation global
	 * Appelée seulement au lancement du premier serveur physique
	 */
	public void jvnNewMasterLoadBalancer() {
		try {
			this.remoteLoadBalancer = new JvnMasterLoadBalancerImpl(this);
		} catch (RemoteException | MalformedURLException | JvnException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Détruit ce serveur physique (et les objets associés)
	 */
	public void destroy() {
		if(this.remoteLoadBalancer != null) {
			this.remoteLoadBalancer.destroy();
		}
		try {
			UnicastRemoteObject.unexportObject(this,true);
		} catch (Exception e) {}
	}

	/**
	 * pour les tests seulement
	 * @return l'id d'objet sur le loadBalancer de cette JVM
	 * @throws JvnException 
	 * @throws RemoteException 
	 */
	public Object jvnGetObjectId() throws RemoteException, JvnException {
		return this.remoteLoadBalancer.jvnGetObjectId();
	}

}
