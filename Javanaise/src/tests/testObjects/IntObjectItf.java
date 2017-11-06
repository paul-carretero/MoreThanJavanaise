package tests.testObjects;

import java.io.Serializable;

import jvn.proxy.LockAsked;
import jvn.proxy.LockAsked.Lock;

/**
 * @author Paul Carretero
 * Interface d'un objet encaspulant un entier pour les tests
 */
@SuppressWarnings("javadoc")
public interface IntObjectItf extends Serializable {
	@LockAsked(lock = Lock.WRITE)
	void set(int n);
	@LockAsked(lock = Lock.READ)
	int get();
}
