package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.rmi.RemoteException;

import jvn.Shared;
import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoordExtended;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

public class JvnTerminate extends JvnSlaveSync{


	public JvnTerminate(final JvnMasterCoordImpl master, final JvnRemoteCoordExtended slave, final JvnRemoteServer js) {
		super(master,slave,js);
	}

	@Override
	public void run() {
		checkSlave();
		if(this.slave != null) {
			try {
				this.slave.jvnTerminate(this.js);
			} catch (RemoteException | JvnException e) {
				this.slave = null;
				checkSlave();
				if(this.slave != null) {
					try {
						this.slave.jvnTerminate(this.js);
					} catch (RemoteException | JvnException e1) {
						Shared.log("JvnTerminate", e1.getMessage());
					}
				}
			}
		}
	}

}
