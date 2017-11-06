/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn.jvnCoord.JvnLogicalCoord;

import java.rmi.*;

import jvn.jvnCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

import java.io.*;


/**
 * @author Paul Carretero
 * Extension de l'interface JvnRemoteCoord 
 * vise à proposer des methodes aux coordinateurs et au loadbalancer 
 * afin de permettre les opérations de maintenance et de synchronisation
 */
public interface JvnRemoteCoordExtended extends JvnRemoteCoord {

	/**
	 * permet de synchroniser le slave avec le master lors d'une demande de verrou en ecriture
	 * @param o un objet applicatif
	 * @param joi l'id de l'objet javanaise sur lequel la demande de verrou en ecriture porte
	 * @param js le serveur client à l'origine de la demande
	 * @throws RemoteException
	 * @throws JvnException
	 */
	public void jvnLockWriteSync(Serializable o, int joi, JvnRemoteServer js) throws RemoteException, JvnException;
	
	/**
	 * permet de signaler à un slave de s'upgrader en master (conserve ses donnés)
	 * @throws RemoteException
	 * @throws JvnException
	 */
	public void upgrade() throws RemoteException, JvnException;

	/**
	 * demande à un coordinateur de se terminer
	 * @throws RemoteException
	 * @throws JvnException
	 */
	public void kill() throws RemoteException, JvnException;
	
	/**
	 * permet de vérifier qu'un coordinateur est toujours actif
	 * @throws RemoteException
	 */
	public void ping() throws RemoteException;

	/**
	 * @return les données d'initialisation qu'un master envoie à son slave afind le mettre à jour avec les dernières données
	 * @throws RemoteException
	 * @throws JvnException
	 */
	public JvnSlaveInitData getData() throws RemoteException, JvnException;

	/**
	 * permet de synchroniser le slave avec le master lors d'une demande de verrou en lecture
	 * @param o un objet applicatif
	 * @param joi l'id de l'objet javanaise sur lequel la demande de verrou en ecriture porte
	 * @param js le serveur client à l'origine de la demande
	 * @throws RemoteException
	 * @throws JvnException
	 */
	void jvnLockReadSync(Serializable o, int joi, JvnRemoteServer js) throws RemoteException, JvnException;

}


