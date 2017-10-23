package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.io.Serializable;
import java.rmi.RemoteException;

import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

public class JvnLockWrite implements Runnable{

	private final int joi;
	private final JvnRemoteServer js;
	private final JvnRemoteCoord slave;
	private final Serializable o;

	/**
	 * 
	 * @param slave
	 * @param joi
	 * @param js
	 * @param o
	 */
	public JvnLockWrite(final JvnRemoteCoord slave, final int joi, final JvnRemoteServer js, final Serializable o) {
		super();
		this.joi 	= joi;
		this.js 	= js;
		this.slave 	= slave;
		this.o 		= o;
	}


	@Override
	public void run() {
		if(this.slave != null) {
			try {
				this.slave.jvnLockWrite(this.o,this.joi,this.js);
			} catch (RemoteException | JvnException e) {
				e.printStackTrace();
			}
		}
	}

}
