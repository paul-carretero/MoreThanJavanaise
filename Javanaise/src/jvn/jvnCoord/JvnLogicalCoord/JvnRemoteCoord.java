/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn.jvnCoord.JvnLogicalCoord;

import java.rmi.*;

import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

import java.io.*;


/**
 * Remote Interface of the JVN Coordinator  
 */

public interface JvnRemoteCoord extends Remote {

	/**
	 *  Allocate a NEW JVN object id (usually allocated to a 
	 *  newly created JVN object)
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public int jvnGetObjectId() throws java.rmi.RemoteException,jvn.jvnExceptions.JvnException; 

	/**
	 * Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.jvnExceptions.JvnException; 

	/**
	 * Get the reference of a JVN object managed by a given JVN server 
	 * @param jon : the JVN object name
	 * @param js : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.jvnExceptions.JvnException; 

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws RemoteException, JvnException;

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws RemoteException, JvnException;

	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	public void jvnTerminate(JvnRemoteServer js) throws RemoteException, JvnException;

	/**
	 * methode permettant d'informer le coordinateur que le serveur distant à éliminé un objet de son cache
	 * @param joi identifiant d'un objet Javanaise
	 * @param js un serveur de cache
	 * @throws RemoteException
	 * @throws JvnException
	 */
	public void invalidateKey(int joi, Serializable o, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException;

	public void jvnLockWriteSync(Serializable o, int joi, JvnRemoteServer js) throws RemoteException, JvnException;
	
	public void upgrade() throws RemoteException, JvnException;

	public void kill() throws RemoteException, JvnException;
	
	public void ping() throws RemoteException;

	public JvnSlaveInitData getData() throws RemoteException, JvnException;

	void jvnLockReadSync(Serializable o, int joi, JvnRemoteServer js) throws RemoteException, JvnException;

}


