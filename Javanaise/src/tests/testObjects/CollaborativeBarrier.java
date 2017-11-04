package tests.testObjects;

public class CollaborativeBarrier implements CollaborativeBarrierItf {
	
	private static final long serialVersionUID = -6196504744667282465L;
	private int i;

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
