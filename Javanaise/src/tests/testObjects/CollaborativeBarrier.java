package tests.testObjects;

/**
 * @author Paul Carretero
 * Objet de test permettant de manière simple de synchronizer plusieurs applications javanaise
 */
public class CollaborativeBarrier implements CollaborativeBarrierItf {
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -6196504744667282465L;
	
	/**
	 * nombre total de participant
	 */
	private int i;

	/**
	 * Constructeur par défault
	 * @param i nombre de participant
	 */
	public CollaborativeBarrier(int i) {
		this.i = i;
	}
	
	@Override
	synchronized public boolean go() {
		return this.i <= 0;
	}
	
	@Override
	synchronized public void addMe() {
		this.i--;
	}

	@Override
	synchronized public void reset(int nproc) {
		this.i = nproc;
	}

}
