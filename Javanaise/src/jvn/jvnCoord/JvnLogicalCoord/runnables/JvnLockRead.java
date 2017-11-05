package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.rmi.RemoteException;

import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

public class JvnLockRead extends JvnSlaveSync{

	private final int joi;

	public JvnLockRead(final JvnMasterCoordImpl master, final JvnRemoteCoord slave, final int joi, final JvnRemoteServer js) {
		super(master, slave, js);
		this.joi 	= joi;
	}


	@Override
	public void run() {
		checkSlave();
		if(this.slave != null) {
			try {
				this.slave.jvnLockRead(this.joi, this.js);
			} catch (RemoteException | JvnException e) {
				checkSlave();
				try {
					this.slave.jvnLockRead(this.joi, this.js);
				} catch (RemoteException | JvnException e1) {
					System.out.println(e1.getMessage());
				}
			}
		}
	}

}
