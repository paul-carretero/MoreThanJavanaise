package jvn.jvnCoord.jvnLoadBalancer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysical;
import jvn.jvnExceptions.JvnException;

public interface JvnLoadBalancer extends Remote {

	/**
	 * alloue un identifiant unique à un objet
	 * @return un identifiant unique pour un objet javanaise
	 * @throws RemoteException
	 * @throws JvnException
	 */
	public int jvnGetObjectId() throws RemoteException, JvnException; 
	
	/**
	 * @return le nombre de coordinateur logique
	 * @throws RemoteException 
	 * @throws JvnException 
	 */
	public int jvnGetNumberCoord() throws RemoteException, JvnException; 
	
	/**
	 * permet à un coordinateur physique de s'enregistrer
	 * normalement appelé à au démarage de celui ci
	 * @param coord un nouveau coordinateur
	 * @return true si la jvm doit créer un slave loadbalacer, faux sinon
	 * @throws RemoteException 
	 * @throws JvnException 
	 */
	public boolean jvnPhysicalCoordRegister(JvnRemotePhysical coord) throws RemoteException, JvnException; 
	
	/**
	 * @param lb 
	 * @return
	 * @throws RemoteException
	 * @throws JvnException
	 */
	public int jvnLoadBalancerRegister(JvnLoadBalancer lb) throws RemoteException, JvnException;

	/**
	 * valide temps qu'on ne retourne pas d'exeption
	 * @throws RemoteException
	 */
	public void ping() throws RemoteException;
	
	public void updateJvnCoordMap(JvnCoordMap jcm) throws RemoteException, JvnException;
}
