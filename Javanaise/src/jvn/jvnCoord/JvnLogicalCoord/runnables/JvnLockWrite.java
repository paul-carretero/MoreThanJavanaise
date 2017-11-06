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
 * permet de synchroniser un coordinateur slave lors d'une demande de verrou en ecriture
 */
public class JvnLockWrite extends JvnSlaveSync{

	/**
	 * id d'un objet javanaise ayant eu une demande de lecture
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
	 * @param joi id d'un objet javanaise ayant eu une demande d'ecriture
	 * @param o contenu de l'objet javanaise
	 * @param js serveur distant ayant initier la demande de verrou
	 */
	public JvnLockWrite(final JvnMasterCoordImpl master, final JvnRemoteCoordExtended slave, final int joi, final JvnRemoteServer js, final Serializable o) {
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
			} catch (@SuppressWarnings("unused") RemoteException | JvnException e) {
				this.slave = null;
				checkSlave();
				if(this.slave != null) {
					try {
						this.slave.jvnLockWriteSync(this.o,this.joi,this.js);
					} catch (RemoteException | JvnException e1) {
						Shared.log("JvnLockWriteSync", e1.getMessage());
					}
				}
			}
		}
	}

}
