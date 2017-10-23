package tests;

import java.io.Serializable;

import jvn.proxy.LockAsked;
import jvn.proxy.LockAsked.Lock;

public class TestObject implements Serializable, TestObjectItf{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7409099308652802059L;

	private String s;
	
	public TestObject(String s) {
		this.setS(s);
	}
	
	public TestObject() {
		this.setS("nullString");
	}

	/**
	 * @return the s
	 */
	@LockAsked(lock = Lock.READ)
	public String getS() {
		return this.s;
	}

	/**
	 * @param s the s to set
	 */
	@LockAsked(lock = Lock.WRITE)
	public void setS(String s) {
		this.s = s;
	}

}
