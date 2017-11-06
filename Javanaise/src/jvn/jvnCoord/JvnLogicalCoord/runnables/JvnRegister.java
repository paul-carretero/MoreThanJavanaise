package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.rmi.RemoteException;

import jvn.Shared;
import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoordExtended;
import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

/**
 * @author Paul Carretero
 * permet de synchroniser un coordinateur slave lors d'une demande d'enregistrement d'un objet JVN
 */
public class JvnRegister extends JvnSlaveSync{

	/**
	 * nom de l'objet (au niveau applicatif)
	 */
	private final String 	jon;
	
	/**
	 * objet JVN
	 */
	private final JvnObject jo;

	/**
	 * Constructeur par default
	 * @param master coordinateur master à l'origine de la demande de synchro
	 * @param slave recoit la demande de synchro
	 * @param jon nom de l'objet
	 * @param jo objet JVN
	 * @param js serveur distant ayant initier la demande de verrou
	 */
	public JvnRegister(final JvnMasterCoordImpl master, final JvnRemoteCoordExtended slave, final String jon, final JvnObject jo, final JvnRemoteServer js) {
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
			} catch (@SuppressWarnings("unused") RemoteException | JvnException e) {
				this.slave = null;
				checkSlave();
				if(this.slave != null) {
					try {
						this.slave.jvnRegisterObject(this.jon, this.jo, this.js);
					} catch (RemoteException | JvnException e1) {
						Shared.log("JvnRegister", e1.getMessage());
					}
				}
			}
		}
	}

}
