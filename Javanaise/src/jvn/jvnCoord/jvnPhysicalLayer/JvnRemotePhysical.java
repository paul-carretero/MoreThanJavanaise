package jvn.jvnCoord.jvnPhysicalLayer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JvnRemotePhysical extends Remote {
	
	/**
	 * demande à une JVM de lancer un coordinateur
	 * permet un découplage entre la couche physique et logique 
	 * (un coordinateur physique peut se retrouver à devoir lancer plusieurs instance si les autres sont en panne)
	 * @param id id du nouveau coordinateur slave
	 * @throws RemoteException 
	 */
	public void jvnNewSlaveCoordInstance(int id) throws RemoteException;
	
	/**
	 * demande à une JVM de lancer un LoadBalancer SLAVE 
	 * (seul le master à la possibilité d'invoquer de nouvel instance)
	 * @throws RemoteException 
	 */
	public void jvnNewSlaveLoadBalancer() throws RemoteException;

	public void jvnNewMasterCoordInstance(int id) throws RemoteException;
	
	public void killCoord(int coordId) throws RemoteException;
	
	public void upgradeCoord(int coordId) throws RemoteException;
	/**
	 * vérifie si le coordinateur physique est toujours en vie, retourne une exception sinon
	 * @throws RemoteException
	 */
	public void ping() throws RemoteException;

	public boolean isLoadBalancer() throws RemoteException;

	
}
