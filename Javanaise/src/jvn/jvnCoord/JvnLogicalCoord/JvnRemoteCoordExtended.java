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


public interface JvnRemoteCoordExtended extends JvnRemoteCoord {

	public void jvnLockWriteSync(Serializable o, int joi, JvnRemoteServer js) throws RemoteException, JvnException;
	
	public void upgrade() throws RemoteException, JvnException;

	public void kill() throws RemoteException, JvnException;
	
	public void ping() throws RemoteException;

	public JvnSlaveInitData getData() throws RemoteException, JvnException;

	void jvnLockReadSync(Serializable o, int joi, JvnRemoteServer js) throws RemoteException, JvnException;

}


