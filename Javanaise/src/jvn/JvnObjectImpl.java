package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

	private static final long 	serialVersionUID	= 1L;

	private final int 		jvnObjectId;
	private Serializable	serializableObject;

	private static final JvnLocalServer LOCAL_SERVER = JvnServerImpl.jvnGetServer();

	private LockState lock;
	private enum LockState{
		NOLOCK,
		READCACHED,
		WRITECACHED,
		READ,
		WRITE,
		WRITECACHEDREAD;
	};

	public JvnObjectImpl(int jvnObjectId, Serializable serializableObject) {
		this.serializableObject	= serializableObject;
		this.jvnObjectId 		= jvnObjectId;
		this.lock				= LockState.WRITE;
	}

	synchronized public void jvnLockRead() throws JvnException {
		switch (this.lock) {
		case NOLOCK:
			this.serializableObject = LOCAL_SERVER.jvnLockRead(this.jvnGetObjectId());
			this.lock = LockState.READ;
			break;
		case READCACHED:
			this.lock = LockState.READ;
			break;
		case WRITECACHED:
			this.lock = LockState.WRITECACHEDREAD;
			break;
		case READ:
			// do nothing
			break;
		case WRITE:
			this.lock = LockState.WRITECACHEDREAD;
			break;
		case WRITECACHEDREAD:
			// do nothing
			break;
		default:
			// lol?
			break;
		}
	}

	synchronized public void jvnLockWrite() throws JvnException {
		switch (this.lock) {
		case NOLOCK:
			this.serializableObject = LOCAL_SERVER.jvnLockWrite(this.jvnGetObjectId());
			this.lock = LockState.WRITE;
			break;
		case READCACHED:
			this.serializableObject = LOCAL_SERVER.jvnLockWrite(this.jvnGetObjectId());
			this.lock = LockState.WRITE;
			break;
		case WRITECACHED:
			this.lock = LockState.WRITE;
			break;
		case READ:
			this.serializableObject = LOCAL_SERVER.jvnLockWrite(this.jvnGetObjectId());
			this.lock = LockState.WRITE;
			break;
		case WRITE:
			// do nothing
			break;
		case WRITECACHEDREAD:
			this.lock = LockState.WRITE;
			break;
		default:
			// lol?
			break;
		}
	}

	synchronized public void jvnUnLock() throws JvnException {
		switch (this.lock) {
		case READ:
			this.lock = LockState.READCACHED;
			break;
		case WRITE:
			this.lock = LockState.WRITECACHED;
			break;
		case WRITECACHEDREAD:
			this.lock = LockState.WRITECACHED;
			break;
		default:
			this.lock = LockState.NOLOCK;
		}
		this.notifyAll();
	}

	public int jvnGetObjectId() throws JvnException {
		return this.jvnObjectId;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		return this.serializableObject;
	}

	synchronized public void jvnInvalidateReader() throws JvnException {
		while(this.lock == LockState.READ) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.lock = LockState.NOLOCK;

		this.notifyAll();
	}

	synchronized public Serializable jvnInvalidateWriter() throws JvnException {
		while(this.lock == LockState.WRITE) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.lock = LockState.NOLOCK;

		this.notifyAll();
		return this.serializableObject;
	}

	synchronized public Serializable jvnInvalidateWriterForReader() throws JvnException {
		while(this.lock == LockState.WRITE) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (this.lock == LockState.WRITECACHED) {
			this.lock = LockState.READCACHED;
		}
		else if (this.lock == LockState.WRITECACHEDREAD) {
			this.lock = LockState.READ;
		}

		this.notifyAll();
		return this.serializableObject;
	}

	synchronized public void setSerializableObject(Serializable o) {
		this.serializableObject = o;
	}

}
