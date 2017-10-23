package jvn.jvnCoord.JvnLogicalCoord.runnables;

import java.rmi.RemoteException;

import jvn.jvnCoord.JvnLogicalCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

public class JvnRegister implements Runnable{
	
	private final String jon;
	private final JvnObject jo;
	private final JvnRemoteServer js;
	private final JvnRemoteCoord slave;

	/**
	 * @param slave 
	 * @param jon
	 * @param jo
	 * @param js
	 */
	public JvnRegister(final JvnRemoteCoord slave, final String jon, final JvnObject jo, final JvnRemoteServer js) {
		super();
		this.jon 	= jon;
		this.jo 	= jo;
		this.js 	= js;
		this.slave 	= slave;
	}


	@Override
	public void run() {
		if(this.slave != null) {
			try {
				this.slave.jvnRegisterObject(this.jon, this.jo, this.js);
			} catch (RemoteException | JvnException e) {
				e.printStackTrace();
			}
		}
	}

}
