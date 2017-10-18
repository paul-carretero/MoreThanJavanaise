package jvn.jvnCoord.heartBeats;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Paul Carretero
 * une classe permettant de s'améliorer, typiquement de slave=>master
 */
public interface Upgradable extends Remote{
	
	/**
	 * demande à l'obet selectionné de se mettre à jour
	 */
	public void upgrade() throws RemoteException;

}
