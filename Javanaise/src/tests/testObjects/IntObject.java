package tests.testObjects;

/**
 * @author Paul Carretero
 * Objet simple représentant un entier (et ses getter/setter)
 */
public class IntObject implements IntObjectItf {
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5658211103101652356L;
	
	/**
	 * un entier (encapsuler)
	 */
	public Integer i;
	
	/**
	 * Constructeur par défault, n'initialize pas i
	 */
	public IntObject() {}
	
	/**
	 * Constructeur initializant l'entier i
	 * @param j un entier
	 */
	public IntObject(int j) {
		this.i = j;
	}

	@Override
	public void set(int n) {
		this.i = n;
	}
	
	@Override
	public int get() {
		return this.i;
	}

}
