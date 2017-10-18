package jvn.jvnCoord.heartBeats;

import java.rmi.RemoteException;

/**
 * @author Paul Carretero
 * ping un pingable et upgrade un upgradable lors de la mort du pingable
 */
public class HeartBeats extends Thread {
	
	private final Pingable 		toPing;
	private final Upgradable 	toUpgrade;
	private static final int 	REFRESH_RATE = 1000;

	public HeartBeats(Pingable toPing, Upgradable toUpgrade) {
		this.toPing 	= toPing;
		this.toUpgrade 	= toUpgrade;
	}

	@Override
	public void run() {
		boolean alive = true;
		while(!isInterrupted() && alive ) {
			try {
				this.toPing.ping();
			} catch (RemoteException e) {
				alive = false;
			}
			try {
				Thread.sleep(REFRESH_RATE);
			} catch (InterruptedException e) {
				System.out.println("Goodbye old friend...");
			}
		}
		if(!alive) {
			try {
				this.toUpgrade.upgrade();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}
