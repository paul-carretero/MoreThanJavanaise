package jvn.jvnCoord.jvnPhysicalLayer;

import java.rmi.Remote;
import java.rmi.RemoteException;

import jvn.jvnCoord.jvnLoadBalancer.JvnCoordData;

public interface JvnRemotePhysical extends Remote {
	
	/**
	 * demande à une JVM de lancer un coordinateur
	 * permet un découplage entre la couche physique et logique 
	 * (un coordinateur physique peut se retrouver à devoir lancer plusieurs instance si les autres sont en panne)
	 * @param data information pour instancier un nouveau coordinateur
	 */
	public void jvnNewCoordInstance(JvnCoordData data) throws RemoteException;
	
	/**
	 * demande à une JVM de lancer un LoadBalancer SLAVE 
	 * (seul le master à la possibilité d'invoquer de nouvel instance)
	 */
	public void jvnNewSlaveLoadBalancer() throws RemoteException;

	/**
	 * vérifie si le coordinateur physique est toujours en vie, retourne une exception sinon
	 * @throws RemoteException
	 */
	public void ping() throws RemoteException;
}
