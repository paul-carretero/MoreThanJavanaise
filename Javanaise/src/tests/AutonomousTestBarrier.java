package tests;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class AutonomousTestBarrier implements Serializable {
	
	private static final long serialVersionUID = -7615829776420196964L;
	private final AtomicInteger partyMember;

	/**
	 * @param partyMember le nombre de participant
	 */
	public AutonomousTestBarrier(int partyMember) {
		this.partyMember = new AtomicInteger(partyMember);
	}

	/**
	 * @return true si tout le monde est arrivé
	 */
	public void imHere() {
		this.partyMember.decrementAndGet();
	}
	
	public boolean isItOkToLeaveNow() {
		System.out.println(this.partyMember.get());
		return this.partyMember.get() == 0;
	}
}
