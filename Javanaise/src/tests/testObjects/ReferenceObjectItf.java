package tests.testObjects;

import java.io.Serializable;

import jvn.proxy.LockAsked;
import jvn.proxy.LockAsked.Lock;

public interface ReferenceObjectItf extends Serializable{

	public StringObjectItf getStringRef();

	@LockAsked(lock = Lock.READ)
	public int getId();

	IntObjectItf getIntRef();
}