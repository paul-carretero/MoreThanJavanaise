package jvn.proxy;

import java.io.Serializable;

import jvn.proxy.LockAsked.Lock;

public interface PrintLolItf extends Serializable {

	@LockAsked(lock = Lock.WRITE)
	public void printLol();
}
