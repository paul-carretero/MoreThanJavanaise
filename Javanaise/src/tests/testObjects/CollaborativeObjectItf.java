package tests.testObjects;

import java.io.Serializable;
import java.util.Queue;

import jvn.proxy.LockAsked;
import jvn.proxy.LockAsked.Lock;

/**
 * @author Paul Carretero
 * interface d'un objet collaboratif pour tester le system en burn
 */
@SuppressWarnings("javadoc")
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