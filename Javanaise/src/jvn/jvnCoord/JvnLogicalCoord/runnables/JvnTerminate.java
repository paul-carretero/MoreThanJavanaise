package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.rmi.RemoteException;

import jvn.Shared;
import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoordExtended;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

/**
 * @author Paul Carretero
 * permet de synchroniser un coordinateur slave lors d'une demande de terminaison d'un serveur JVN
 */
public class JvnTerminate extends JvnSlaveSync{


	/**
	 * @param master le master à l'origine de la demande de synchro
	 * @param slave le coordinateur slave à synchroniser
	 * @param js le serveur client conserné par la demande de synchro
	 */
	public JvnTerminate(final JvnMasterCoordImpl master, final JvnRemoteCoordExtended slave, final JvnRemoteServer js) {
		super(master,slave,js);
	}

	@Override
	public void run() {
		checkSlave();
		if(this.slave != null) {
			try {
				this.slave.jvnTerminate(this.js);
			} catch (@SuppressWarnings("unused") RemoteException | JvnException e) {
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
