package tests.irc;

import java.io.Serializable;

import jvn.proxy.LockAsked;
import jvn.proxy.LockAsked.Lock;

public interface SentenceItf extends Serializable {
	@LockAsked ( lock = Lock.READ )
	public String read();
	@LockAsked ( lock = Lock.WRITE )
	public void  write(String text);
}
