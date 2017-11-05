package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.rmi.RemoteException;

import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

public class JvnTerminate extends JvnSlaveSync{
	

	public JvnTerminate(final JvnMasterCoordImpl master, final JvnRemoteCoord slave, final JvnRemoteServer js) {
		super(master,slave,js);
	}

	@Override
	public void run() {
		checkSlave();
		if(this.slave != null) {
			try {
				this.slave.jvnTerminate(this.js);
			} catch (RemoteException | JvnException e) {
				checkSlave();
				try {
					this.slave.jvnTerminate(this.js);
				} catch (RemoteException | JvnException e1) {
					System.out.println(e1.getMessage());
				}
			}
		}
	}

}
