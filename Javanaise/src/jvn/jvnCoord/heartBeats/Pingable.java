package jvn.jvnCoord.heartBeats;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Paul Carretero
 * Une classe permettant à d'autre classe (éventuellement distante) de vérifier qu'elle est toujours en vie
 */
public interface Pingable extends Remote{

	/**
	 * ne retourne pas d'exception = toujours vivant
	 * @throws RemoteException 
	 */
	public void ping() throws RemoteException;

}
