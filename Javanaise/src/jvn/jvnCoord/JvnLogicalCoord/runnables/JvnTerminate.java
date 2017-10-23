package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.rmi.RemoteException;

import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

public class JvnTerminate implements Runnable{
	
	private final JvnRemoteServer js;
	private final JvnRemoteCoord slave;

	/**
	 * @param slave
	 * @param js
	 */
	public JvnTerminate(final JvnRemoteCoord slave, final JvnRemoteServer js) {
		this.js = js;
		this.slave = slave;
	}

	@Override
	public void run() {
		if(this.slave != null) {
			try {
				this.slave.jvnTerminate(this.js);
			} catch (RemoteException | JvnException e) {
				e.printStackTrace();
			}
		}
	}

}
