package tests.testObjects;

public class StringObject implements StringObjectItf{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7409099308652802059L;

	private String s;
	
	public StringObject(String s) {
		this.setS(s);
	}
	
	public StringObject() {
		this.setS("nullString");
	}

	/**
	 * @return the s
	 */
	public String getS() {
		return this.s;
	}

	/**
	 * @param s the s to set
	 */
	public void setS(String s) {
		this.s = s;
	}

}
