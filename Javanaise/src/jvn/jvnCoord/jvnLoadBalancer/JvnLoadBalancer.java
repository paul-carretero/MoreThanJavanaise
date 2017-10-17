package jvn.jvnCoord.jvnLoadBalancer;

import java.rmi.Remote;
import java.rmi.RemoteException;

import jvn.jvnCoord.JvnRemoteCoord;
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
	 * Informe le LoadBalancer qu'un Coordinateur est inaccessible.
	 * Le LoadBalancer devra le remplacer par son SLAVE (si ce n'est pas déjà fait)
	 * Le serveur devra rebind sur la string
	 * @param jvnCoord un nom de coordinateur en échec
	 * @throws RemoteException
	 * @throws JvnException
	 */
	public void jvnReSync(String jvnCoord) throws RemoteException, JvnException; 
	
	/**
	 * @return un tableau contenant une liste ordonné 
	 * des database serveur en charge de la gestion des objets.
	 * de telle sorte que l'objet ayant l'id id doit situé sur le serveur en position (id % length)
	 */
	public String[] jvnGetCoordName() throws RemoteException, JvnException; 
	
	/**
	 * augment le conteur des id des objet de 1 (de manière atomique)
	 */
	public void jvnIncrementCounter() throws RemoteException, JvnException; 
	
	/**
	 * 
	 * @param coord
	 * @return 
	 */
	public String jvnCoordRegister(JvnRemoteCoord coord) throws RemoteException, JvnException; 
}
