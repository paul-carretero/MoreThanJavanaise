package jvn.jvnServer;

import java.io.Serializable;

public class JvnTransactData {
	
	private Serializable o;
	
	private int readCount = 0;
	
	private int writeCount = 0;

	public JvnTransactData() {
		this.o 			= null;
		this.writeCount	= 0;
		this.readCount	= 0;
	}

	public int getTotalLockCount() {
		return this.readCount + this.writeCount;
	}

	public Serializable getSerializableObject() {
		return this.o;
	}

	public void read() {
		this.readCount++;
	}
	
	public void write(Serializable o) {
		if(this.o == null) {
			this.o = o;
		}
		this.writeCount++;
	}
}
