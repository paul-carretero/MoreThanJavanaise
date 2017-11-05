package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.io.Serializable;
import java.rmi.RemoteException;

import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

public class JvnInvalidateKey extends JvnSlaveSync{

	private final int 			joi;
	private final Serializable 	o;

	public JvnInvalidateKey(final JvnMasterCoordImpl master, final JvnRemoteCoord slave, final int joi, final Serializable o, final JvnRemoteServer js) {
		super(master, slave, js);
		this.joi 	= joi;
		this.o 		= o;
	}


	@Override
	public void run() {
		checkSlave();
		if(this.slave != null) {
			try {
				this.slave.invalidateKey(this.joi, this.o, this.js);
			} catch (RemoteException | JvnException e) {
				this.slave = null;
				checkSlave();
				if(this.slave != null) {
					try {
						this.slave.invalidateKey(this.joi, this.o, this.js);
					} catch (RemoteException | JvnException e1) {
						System.err.println(e1.getLocalizedMessage());
					}
				}
			}
		}
	}

}
