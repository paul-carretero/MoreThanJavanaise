package jvn.jvnServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class JvnTransactData {
	
	private byte[] backup;
	
	private int readCount = 0;
	
	private int writeCount = 0;

	public JvnTransactData() {
		this.writeCount	= 0;
		this.readCount	= 0;
	}

	public int getTotalLockCount() {
		return this.readCount + this.writeCount;
	}

	public Serializable getSerializableObject() {
		try {
			return getBackup();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void read() {
		this.readCount++;
	}
	
	public void write(Serializable o) {
		if(this.backup == null) {
			try {
				createBackup(o);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.writeCount++;
	}
	
   private Serializable getBackup() throws IOException, ClassNotFoundException {
        ObjectInputStream steam	= new ObjectInputStream(new ByteArrayInputStream(this.backup));
        Serializable o  		= (Serializable) steam.readObject();
        steam.close();
        return o;
   }

    private void createBackup(Serializable o) throws IOException {
        ByteArrayOutputStream byteSteam	= new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteSteam);
        outputStream.writeObject(o);
        outputStream.close();
        this.backup = byteSteam.toByteArray();
    }

	public boolean haveBackup() {
		return this.backup != null;
	}
}
