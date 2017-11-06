package tests.testObjects;

import java.io.Serializable;

import jvn.jvnExceptions.JvnConcurrentLockUpgradeException;
import jvn.proxy.LockAsked;
import jvn.proxy.LockAsked.Lock;

/**
 * @author Paul Carretero
 * Interface d'un objet de test encapsulant une string
 */
@SuppressWarnings("javadoc")
public interface StringObjectItf extends Serializable{
	@LockAsked(lock = Lock.READ)
	public String getS();
	@LockAsked(lock = Lock.WRITE)
	public void setS(String string) throws JvnConcurrentLockUpgradeException;

}
