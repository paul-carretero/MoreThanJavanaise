package tests.testObjects;

import java.io.Serializable;

import jvn.proxy.LockAsked;
import jvn.proxy.LockAsked.Lock;

/**
 * @author Paul Carretero
 * interface d'un objet possèdant un id et des référence vers d'autre objet javanaise
 */
@SuppressWarnings("javadoc")
public interface ReferenceObjectItf extends Serializable{

	public StringObjectItf getStringRef();

	@LockAsked(lock = Lock.READ)
	public int getId();

	IntObjectItf getIntRef();
}