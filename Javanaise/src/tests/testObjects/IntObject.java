package tests.testObjects;

public class IntObject implements IntObjectItf {
	
	private static final long serialVersionUID = 5658211103101652356L;
	public Integer i;
	
	public IntObject() {}
	
	@Override
	public void set(int n) {this.i = n;}
	@Override
	public int get() {return this.i;}

}