package jvn;

import java.rmi.RemoteException;

public class JvnCoordNotifyWorker implements Runnable {

	private final JvnCoordImpl coord;
	private final int joi;
	private final JvnRemoteServer js;

	/**
	 * @param coord
	 * @param joi
	 * @param jvnServer
	 */
	public JvnCoordNotifyWorker(JvnCoordImpl coord, int joi, JvnRemoteServer jvnServer) {
		this.coord = coord;
		this.joi = joi;
		this.js = jvnServer;
	}

	@Override
	public void run() {
		try {
			this.coord.waitOnWW(this.joi);
			this.js.notifyForReadLock(this.joi, this.coord.jvnLockReadHandler(this.joi, this.js));
		} catch (RemoteException | JvnException e) {
			e.printStackTrace();
		}
	}
}
