/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn.jvnServer;

import java.rmi.*;
import java.io.*;


/**
 * Remote interface of a JVN server (used by a remote JvnCoord)
 */

public interface JvnRemoteServer extends Remote {

	/**
	 * Invalidate the Read lock of a JVN object 
	 * @param joi : the JVN object id
	 * @throws java.rmi.RemoteException
	 * @throws jvn.JvnException 
	 **/
	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException,jvn.JvnException;

	/**
	 * Invalidate the Write lock of a JVN object 
	 * @param joi : the JVN object id
	 * @return the current JVN object state 
	 * @throws java.rmi.RemoteException
	 * @throws jvn.JvnException 
	 **/
	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException,jvn.JvnException;

	/**
	 * Reduce the Write lock of a JVN object 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException
	 * @throws jvn.JvnException 
	 **/
	public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException,jvn.JvnException;

	/**
	 * notify ce serveur local que le verrou en lecture est disponible et met à jour la valeur de l'objet considéré
	 * callback asynchrone du coordinateur selon disponibilité pour éviter les interblocages
	 * @param joi identifiant d'un objet javanaise
	 * @param o le nouvel objet applicatif contenu dans l'objet javanaise associé à cet id (pour mise à jour du cache local)
	 * @throws RemoteException
	 */
	public void notifyForReadWriteLock(int joi, Serializable o) throws java.rmi.RemoteException;
}


