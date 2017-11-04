package tests.testObjects;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

public class CollaborativeObject implements CollaborativeObjectItf{

	private static final long serialVersionUID = -6646538978045156920L;
	private ArrayDeque<Integer> proofOfCollaboration;
	
	public CollaborativeObject() {
		this.proofOfCollaboration = new ArrayDeque<Integer>();
	}
	
	@Override
	public void addMe(int i) {
		this.proofOfCollaboration.add(i);
	}
	
	@Override
	public Queue<Integer> getResult() {
		return this.proofOfCollaboration;
	}
	
	@Override
	public int getLast() {
		try {
			return this.proofOfCollaboration.getLast();
		} catch (NoSuchElementException e) {
			return 0;
		}
	}

	@Override
	public void reset() {
		this.proofOfCollaboration = new ArrayDeque<Integer>();
	}
}
