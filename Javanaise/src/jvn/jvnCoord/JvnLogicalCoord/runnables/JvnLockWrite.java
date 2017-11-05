package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.io.Serializable;
import java.rmi.RemoteException;

import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

public class JvnLockWrite extends JvnSlaveSync{

	private final int 			joi;
	private final Serializable 	o;

	public JvnLockWrite(final JvnMasterCoordImpl master, final JvnRemoteCoord slave, final int joi, final JvnRemoteServer js, final Serializable o) {
		super(master, slave, js);
		this.joi 	= joi;
		this.o 		= o;
	}


	@Override
	public void run() {
		checkSlave();
		if(this.slave != null) {
			try {
				this.slave.jvnLockWriteSync(this.o,this.joi,this.js);
			} catch (RemoteException | JvnException e) {
				checkSlave();
				try {
					this.slave.jvnLockWriteSync(this.o,this.joi,this.js);
				} catch (RemoteException | JvnException e1) {
					System.out.println(e1.getMessage());
				}
			}
		}
	}

}
