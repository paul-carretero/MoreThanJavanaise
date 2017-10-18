package jvn.proxy;

import java.io.Serializable;
import java.rmi.RemoteException;

import jvn.jvnCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

public class callHandler implements JvnRemoteCoord{

	public callHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int jvnGetObjectId() throws RemoteException, JvnException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws RemoteException, JvnException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws RemoteException, JvnException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void jvnTerminate(JvnRemoteServer js) throws RemoteException, JvnException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void jvnResetCoord() throws RemoteException, JvnException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void invalidateKey(int joi, Serializable o, JvnRemoteServer js) throws RemoteException, JvnException {
		// TODO Auto-generated method stub
		
	}

}
