package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.rmi.RemoteException;

import jvn.jvnCoord.JvnLogicalCoord.JvnMasterCoordImpl;
import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

/**
 * @author Paul Carretero
 * Runnable permettant d'effectuer des appels asynchrone 
 * (permet entre autre de libérer des verrou et detecter des deadlocks)
 */
public class JvnCoordNotifyWorker implements Runnable {

	/**
	 * le coordinateur à l'ortigine de cet objet
	 */
	private final JvnMasterCoordImpl	coord;
	
	/**
	 * id d'un objet javanaise sur lequel effecture le callback
	 */
	private final int 					joi;
	
	/**
	 * le serveur distant ayant demandé un verrou en lecture ou en ecriture
	 */
	private final JvnRemoteServer 		js;
	
	/**
	 * true si il s'agit d'une demande de verrou en lecture, false sinon
	 */
	private final boolean 				askForRead;

	/**
	 * constructeur par défault
	 * @param coord le coordinateur à l'origine de cet objet
	 * @param joi un id d'objet javanaise
	 * @param jvnServer le serveur sur lequel effectuer le callback
	 * @param askForRead true si il s'agit d'une demande de verrou en lecture, false sinon
	 */
	public JvnCoordNotifyWorker(JvnMasterCoordImpl coord, int joi, JvnRemoteServer jvnServer, boolean askForRead) {
		this.coord		= coord;
		this.joi 		= joi;
		this.js 		= jvnServer;
		this.askForRead	= askForRead;
	}

	@Override
	public void run() {
		try {
			if(this.askForRead) {
				this.coord.waitOnWW(this.joi);
				this.js.notifyForReadWriteLock(this.joi, this.coord.jvnLockReadHandler(this.joi, this.js));
			}
			else {
				this.js.notifyForReadWriteLock(this.joi, this.coord.jvnLockWriteHandler(this.joi, this.js));
			}
		} catch (RemoteException | JvnException e) {
			e.printStackTrace();
		}
	}
}
