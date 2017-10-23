package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.io.Serializable;
import java.rmi.RemoteException;

import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

public class JvnInvalidateKey implements Runnable{

	private final int joi;
	private final Serializable o;
	private final JvnRemoteServer js;
	private final JvnRemoteCoord slave;

	/**
	 * @param slave 
	 * @param joi 
	 * @param o 
	 * @param js
	 */
	public JvnInvalidateKey(final JvnRemoteCoord slave, final int joi, final Serializable o, final JvnRemoteServer js) {
		super();
		this.joi 	= joi;
		this.o 		= o;
		this.js 	= js;
		this.slave 	= slave;
	}


	@Override
	public void run() {
		if(this.slave != null) {
			try {
				this.slave.invalidateKey(this.joi, this.o, this.js);;
			} catch (RemoteException | JvnException e) {
				e.printStackTrace();
			}
		}
	}

}
