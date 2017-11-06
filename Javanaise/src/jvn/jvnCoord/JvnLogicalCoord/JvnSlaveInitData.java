package jvn.jvnCoord.JvnLogicalCoord;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

/**
 * @author Paul Carretero
 * contient les données d'initialisation d'un coordinateur slave.
 * Ces données sont envoyé par un coordinateur master lors de l'initialisation du slave
 */
public class JvnSlaveInitData implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -2172107637059994765L;
	
	/**
	 * map des compteur des demande de verrou en ecriture
	 */
	private final Map<Integer, AtomicInteger>	waitingWriters;
	
	/**
	 * map contenant les verroux associés aux objets JVN
	 */
	private final Map<Integer, Lock>			objectLocks;
	
	/**
	 * représentation de l'ensemble des objets JVN géré par le coordinateur (au moment du transfert)
	 */
	private final JvnObjectMapCoord 			jvnObjects;
	
	/**
	 * constructeur par défault
	 * @param waitingWriters map des compteur des demande de verrou en ecriture
	 * @param objectLocks map contenant les verroux associés aux objets JVN
	 * @param jvnObjects représentation de l'ensemble des objets JVN géré par le coordinateur (au moment du transfert
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
		return this.jvnObjects;
	}

	/**
	 * @return the waitingWriters
	 */
	public Map<Integer, AtomicInteger> getWaitingWriters() {
		return this.waitingWriters;
	}

	/**
	 * @return the objectLocks
	 */
	public Map<Integer, Lock> getObjectLocks() {
		return this.objectLocks;
	}

}
