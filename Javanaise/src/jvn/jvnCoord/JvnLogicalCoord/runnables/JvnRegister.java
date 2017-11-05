package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.rmi.RemoteException;

import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

public class JvnRegister extends JvnSlaveSync{

	private final String 	jon;
	private final JvnObject jo;

	public JvnRegister(final JvnMasterCoordImpl master, final JvnRemoteCoord slave, final String jon, final JvnObject jo, final JvnRemoteServer js) {
		super(master,slave,js);
		this.jon 	= jon;
		this.jo 	= jo;
	}


	@Override
	public void run() {
		checkSlave();
		if(this.slave != null) {
			try {
				this.slave.jvnRegisterObject(this.jon, this.jo, this.js);
			} catch (RemoteException | JvnException e) {
				this.slave = null;
				checkSlave();
				if(this.slave != null) {
					try {
						this.slave.jvnRegisterObject(this.jon, this.jo, this.js);
					} catch (RemoteException | JvnException e1) {
						System.out.println(e1.getMessage());
					}
				}
			}
		}
	}

}
