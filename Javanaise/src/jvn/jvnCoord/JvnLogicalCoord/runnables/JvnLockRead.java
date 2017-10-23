package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.rmi.RemoteException;

import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

public class JvnLockRead implements Runnable{

	private final int joi;
	private final JvnRemoteServer js;
	private final JvnRemoteCoord slave;

	/**
	 * 
	 * @param slave
	 * @param joi
	 * @param js
	 */
	public JvnLockRead(final JvnRemoteCoord slave, final int joi, final JvnRemoteServer js) {
		super();
		this.joi 	= joi;
		this.js 	= js;
		this.slave 	= slave;
	}


	@Override
	public void run() {
		if(this.slave != null) {
			try {
				this.slave.jvnLockRead(this.joi, this.js);
			} catch (RemoteException | JvnException e) {
				e.printStackTrace();
			}
		}
	}

}
