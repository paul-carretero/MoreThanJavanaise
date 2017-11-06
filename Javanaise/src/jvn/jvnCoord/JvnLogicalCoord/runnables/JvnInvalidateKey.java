package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.io.Serializable;
import java.rmi.RemoteException;

import jvn.Shared;
import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoordExtended;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

/**
 * @author Paul Carretero
 * demande de synchronisation pour un invalidation de clé pour un serveur distant
 * Notament utilisé pour informer les coordinateurs qu'un objet à été retiré du cache
 */
public class JvnInvalidateKey extends JvnSlaveSync{

	/**
	 * id d'un objet javanaise ayant été retiré du cache
	 */
	private final int 			joi;
	
	/**
	 * contenu de l'objet javanaise
	 */
	private final Serializable 	o;

	/**
	 * Constructeur par défault
	 * @param master coordinateur master à l'origine de la demande de synchro
	 * @param slave recoit la demande de synchro
	 * @param joi id d'un objet javanaise ayant été retiré du cache
	 * @param o contenu de l'objet javanaise
	 * @param js serveur distant ayant retiré cet objet de son cache
	 */
	public JvnInvalidateKey(final JvnMasterCoordImpl master, final JvnRemoteCoordExtended slave, final int joi, final Serializable o, final JvnRemoteServer js) {
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
			} catch (@SuppressWarnings("unused") RemoteException | JvnException e) {
				this.slave = null;
				checkSlave();
				if(this.slave != null) {
					try {
						this.slave.invalidateKey(this.joi, this.o, this.js);
					} catch (RemoteException | JvnException e1) {
						Shared.log("JvnInvalidateKey", e1.getMessage());
					}
				}
			}
		}
	}

}
