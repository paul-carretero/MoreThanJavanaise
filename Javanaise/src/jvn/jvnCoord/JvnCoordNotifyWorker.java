package jvn.jvnCoord;

import java.rmi.RemoteException;

import jvn.jvnExceptions.JvnException;
import jvn.jvnServer.JvnRemoteServer;

public class JvnCoordNotifyWorker implements Runnable {

	private final JvnCoordImpl coord;
	private final int joi;
	private final JvnRemoteServer js;
	private final boolean askForRead;

	/**
	 * @param coord
	 * @param joi
	 * @param jvnServer
	 */
	public JvnCoordNotifyWorker(JvnCoordImpl coord, int joi, JvnRemoteServer jvnServer, boolean askForRead) {
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
