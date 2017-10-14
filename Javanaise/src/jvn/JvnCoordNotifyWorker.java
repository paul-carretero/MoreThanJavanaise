package jvn;

import java.rmi.RemoteException;

import jvn.JvnCoordImpl.RequestType;

public class JvnCoordNotifyWorker implements Runnable {

	private final JvnCoordImpl coord;
	private final int joi;
	private final JvnRemoteServer js;
	private final RequestType requestType;

	/**
	 * @param coord
	 * @param joi
	 * @param jvnServer
	 * @param requestType
	 */
	public JvnCoordNotifyWorker(JvnCoordImpl coord, int joi, JvnRemoteServer jvnServer, RequestType requestType) {
		this.coord = coord;
		this.joi = joi;
		this.js = jvnServer;
		this.requestType = requestType;
	}

	@Override
	public void run() {
		try {
			if(this.requestType == RequestType.READ) {
				this.coord.waitOnWW(this.joi);
				this.js.notifyForReadLock(this.joi, this.coord.jvnLockReadHandler(this.joi, this.js));
			}
			else {
				//this.js.notifyForWriteLock(this.joi, this.coord.jvnLockWriteHandler(this.joi, this.js));
			}
		} catch (RemoteException | JvnException e) {
			e.printStackTrace();
		}
	}
}
