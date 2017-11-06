package jvn.jvnCoord.jvnPhysicalLayer;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Paul Carretero
 * Interface d'une machine physique servant de base aux coordinateurs et aux loadbalancer
 */
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

	/**
	 * demande à cette machine physique de lancer une nouvelle instance d'un coordinateur master ayant l'id spécifié
	 * @param id id du coordinateur à lancé
	 * @throws RemoteException
	 */
	public void jvnNewMasterCoordInstance(int id) throws RemoteException;
	
	/**
	 * termine le coordinateur spécifié par l'id
	 * @param coordId id d'un coordinateur
	 * @throws RemoteException
	 */
	public void killCoord(int coordId) throws RemoteException;
	
	/**
	 * demande l'upgrade d'un coordinateur slave vers un coordinateur master
	 * @param coordId id du coordinateur à upgrader
	 * @throws RemoteException
	 */
	public void upgradeCoord(int coordId) throws RemoteException;
	
	/**
	 * vérifie si le coordinateur physique est toujours en vie, retourne une exception sinon
	 * @throws RemoteException
	 */
	public void ping() throws RemoteException;

	/**
	 * @return true si la machine dispose d'un loadbalancer, false sinon
	 * @throws RemoteException
	 */
	public boolean isLoadBalancer() throws RemoteException;

	
}
