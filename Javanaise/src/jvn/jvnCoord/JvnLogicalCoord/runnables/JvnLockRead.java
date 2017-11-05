package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.io.Serializable;
import java.rmi.RemoteException;

import jvn.Shared;
import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoordExtended;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

public class JvnLockRead extends JvnSlaveSync{

	private final int joi;
	private final Serializable o;

	public JvnLockRead(final JvnMasterCoordImpl master, final JvnRemoteCoordExtended slave, final int joi, final JvnRemoteServer js, final Serializable o) {
		super(master, slave, js);
		this.joi 	= joi;
		this.o 		= o;
	}


	@Override
	public void run() {
		checkSlave();
		if(this.slave != null) {
			try {
				this.slave.jvnLockReadSync(this.o, this.joi, this.js);
			} catch (RemoteException | JvnException e) {
				this.slave = null;
				checkSlave();
				if(this.slave != null) {
					try {
						this.slave.jvnLockReadSync(this.o, this.joi, this.js);
					} catch (RemoteException | JvnException e1) {
						Shared.log("JvnLockReadSync", e1.getMessage());
					}
				}
			}
		}
	}

}
