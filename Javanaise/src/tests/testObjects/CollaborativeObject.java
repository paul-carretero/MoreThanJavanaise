package tests.testObjects;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * @author Paul Carretero
 * Objet de test proposant une queue que plusieurs application javanaise peuvent remplire mutuellement
 */
public class CollaborativeObject implements CollaborativeObjectItf{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -6646538978045156920L;
	
	/**
	 * Queue sur laquelle plusieurs applications peuvent travailler
	 */
	private ArrayDeque<Integer> proofOfCollaboration;
	
	/**
	 * Constructeur par défault, initialize une queue vide
	 */
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
		} catch (@SuppressWarnings("unused") NoSuchElementException e) {
			return 0;
		}
	}

	@Override
	public void reset() {
		this.proofOfCollaboration = new ArrayDeque<Integer>();
	}
}
