package jvn.jvnCoord.JvnLogicalCoord;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class JvnSlaveInitData implements Serializable {
	
	

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -2172107637059994765L;
	private final Map<Integer, AtomicInteger>	waitingWriters;
	private final Map<Integer, Lock>			objectLocks;
	private final JvnObjectMapCoord 			jvnObjects;
	
	/**
	 * @param waitingWriters
	 * @param objectLocks
	 * @param jvnObjects
	 */
	public JvnSlaveInitData(Map<Integer, AtomicInteger> waitingWriters, Map<Integer, Lock> objectLocks, JvnObjectMapCoord jvnObjects) {
		super();
		this.waitingWriters	= waitingWriters;
		this.objectLocks 	= objectLocks;
		this.jvnObjects 	= jvnObjects;
	}

	/**
	 * @return the jvnObjects
	 */
	public JvnObjectMapCoord getJvnObjects() {
		return jvnObjects;
	}

	/**
	 * @return the waitingWriters
	 */
	public Map<Integer, AtomicInteger> getWaitingWriters() {
		return waitingWriters;
	}

	/**
	 * @return the objectLocks
	 */
	public Map<Integer, Lock> getObjectLocks() {
		return objectLocks;
	}


}
