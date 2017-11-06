package tests.testObjects;

import java.io.Serializable;

import jvn.proxy.LockAsked;
import jvn.proxy.LockAsked.Lock;

/**
 * @author Paul Carretero
 * Interface d'une barrière de synchro pour les tests
 */
@SuppressWarnings("javadoc")
public interface CollaborativeBarrierItf extends Serializable {

	@LockAsked ( lock = Lock.READ )
	boolean go();

	@LockAsked ( lock = Lock.WRITE )
	void addMe();
	
	@LockAsked ( lock = Lock.WRITE )
	void reset(int nproc);

}