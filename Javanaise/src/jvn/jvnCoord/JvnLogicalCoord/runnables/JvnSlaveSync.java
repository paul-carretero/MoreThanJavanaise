package jvn.jvnCoord.JvnLogicalCoord.runnables;

import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnServer.JvnRemoteServer;

public abstract class JvnSlaveSync implements Runnable {
	
	protected final JvnMasterCoordImpl	master;
	protected final JvnRemoteServer		js;
	protected JvnRemoteCoord			slave;

	public JvnSlaveSync(final JvnMasterCoordImpl master, final JvnRemoteCoord slave, final JvnRemoteServer js) {
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
