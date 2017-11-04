package tests.testObjects;

import java.io.Serializable;

import jvn.proxy.LockAsked;
import jvn.proxy.LockAsked.Lock;

public interface IntObjectItf extends Serializable {
	@LockAsked(lock = Lock.WRITE)
	void set(int n);
	@LockAsked(lock = Lock.READ)
	int get();
}
