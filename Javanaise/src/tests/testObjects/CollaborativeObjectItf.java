package tests.testObjects;

import java.io.Serializable;
import java.util.Queue;

import jvn.proxy.LockAsked;
import jvn.proxy.LockAsked.Lock;

public interface CollaborativeObjectItf extends Serializable{

	@LockAsked ( lock = Lock.WRITE )
	public void addMe(int i);

	@LockAsked ( lock = Lock.READ )
	public Queue<Integer> getResult();

	@LockAsked ( lock = Lock.READ )
	public int getLast();

	@LockAsked ( lock = Lock.WRITE )
	public void reset();

}