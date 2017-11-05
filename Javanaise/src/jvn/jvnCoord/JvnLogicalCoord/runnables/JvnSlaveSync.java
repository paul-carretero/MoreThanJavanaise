package jvn.jvnCoord.JvnLogicalCoord.runnables;

import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoordExtended;
import jvn.jvnServer.JvnRemoteServer;

public abstract class JvnSlaveSync implements Runnable {
	
	protected final JvnMasterCoordImpl	master;
	protected final JvnRemoteServer		js;
	protected JvnRemoteCoordExtended	slave;

	public JvnSlaveSync(final JvnMasterCoordImpl master, final JvnRemoteCoordExtended slave, final JvnRemoteServer js) {
		this.master = master;
		this.js 	= js;
		this.slave 	= slave;
	}
	
	protected void checkSlave(){
		if(this.slave == null) {
			this.slave = this.master.updateSlave();
		}
	}

}
