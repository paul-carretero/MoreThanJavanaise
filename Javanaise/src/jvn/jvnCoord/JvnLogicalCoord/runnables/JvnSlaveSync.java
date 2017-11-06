package jvn.jvnCoord.JvnLogicalCoord.runnables;

import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoordExtended;
import jvn.jvnServer.JvnRemoteServer;

/**
 * @author Paul Carretero
 * classe abstraite servant de base pour la synchronisation du coordinateur master avec le slave
 */
public abstract class JvnSlaveSync implements Runnable {
	
	/**
	 * le master à l'origine de la demande de synchro
	 */
	protected final JvnMasterCoordImpl	master;
	
	/**
	 * le serveur client conserné par la demande de synchro
	 */
	protected final JvnRemoteServer		js;
	
	/**
	 * le coordinateur slave à synchroniser
	 */
	protected JvnRemoteCoordExtended	slave;

	/**
	 * @param master le master à l'origine de la demande de synchro
	 * @param slave le coordinateur slave à synchroniser
	 * @param js le serveur client conserné par la demande de synchro
	 */
	public JvnSlaveSync(final JvnMasterCoordImpl master, final JvnRemoteCoordExtended slave, final JvnRemoteServer js) {
		this.master = master;
		this.js 	= js;
		this.slave 	= slave;
	}
	
	/**
	 * methode permettant de rechercher un coordinateur slave si celui ci est null
	 */
	protected void checkSlave(){
		if(this.slave == null) {
			this.slave = this.master.updateSlave();
		}
	}

}
