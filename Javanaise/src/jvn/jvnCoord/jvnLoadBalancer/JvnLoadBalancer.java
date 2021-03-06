package jvn.jvnCoord.jvnLoadBalancer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import jvn.jvnCoord.jvnPhysicalLayer.JvnRemotePhysical;
import jvn.jvnExceptions.JvnException;

/**
 * @author Paul Carretero
 * Interface permettant aux loadbalancer de communiquer entre eux et de donner des identifiant d'objet
 */
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
	 * @throws RemoteException 
	 * @throws JvnException 
	 */
	public void jvnPhysicalCoordRegister(JvnRemotePhysical coord) throws RemoteException, JvnException; 
	
	/**
	 * valide temps qu'on ne retourne pas d'exeption
	 * @throws RemoteException
	 */
	public void ping() throws RemoteException;
	
	/**
	 * met à jour la map des coordinateurs du slave (depuis celle du master)
	 * @param jcm une map des coordinateurs
	 * @throws RemoteException
	 * @throws JvnException
	 */
	public void updateJvnCoordMap(JvnCoordMap jcm) throws RemoteException, JvnException;

	/**
	 * @return l'id actuel pour synchro
	 * @throws RemoteException
	 * @throws JvnException 
	 */
	public int jvnInitObjectId() throws RemoteException, JvnException;
}
