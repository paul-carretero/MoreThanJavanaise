package tests.testObjects;

import java.io.Serializable;

import jvn.proxy.LockAsked;
import jvn.proxy.LockAsked.Lock;

public interface StringObjectItf extends Serializable{
	@LockAsked(lock = Lock.READ)
	public String getS();
	@LockAsked(lock = Lock.WRITE)
	public void setS(String string);

}
