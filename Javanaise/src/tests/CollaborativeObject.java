package tests;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

public class CollaborativeObject implements Serializable{

	private static final long serialVersionUID = -6646538978045156920L;
	private final ArrayDeque<Integer> proofOfCollaboration;
	
	public CollaborativeObject() {
		this.proofOfCollaboration = new ArrayDeque<Integer>();
	}
	
	public void addMe(int i) {
		this.proofOfCollaboration.add(i);
	}
	
	public Queue<Integer> getResult() {
		return this.proofOfCollaboration;
	}
	
	public int getLast() {
		try {
			return this.proofOfCollaboration.getLast();
		} catch (NoSuchElementException e) {
			return 0;
		}
	}

}
