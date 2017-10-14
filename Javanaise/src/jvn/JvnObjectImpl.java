package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

	public enum LockState{
		NOLOCK,
		READCACHED,
		WRITECACHED,
		READ,
		WRITE,
		WRITECACHEDREAD;
	}

	private static final long 			serialVersionUID	= 1L;
	private static final JvnLocalServer	LOCAL_SERVER 		= JvnServerImpl.jvnGetServer();

	private final int 		jvnObjectId;
	private Serializable	serializableObject;

	private volatile boolean	HasBeenInvalidated;
	private volatile LockState	lock;
	
	private final String waitForReadLockNotify	= "waitForReadLockNotify";
	private final String waitForWriteLockNotify	= "waitForWriteLockNotify";

	public JvnObjectImpl(int jvnObjectId, Serializable serializableObject) {
		this.serializableObject	= serializableObject;
		this.jvnObjectId 		= jvnObjectId;
		this.lock				= LockState.WRITE;
		this.HasBeenInvalidated	= false;
	}

	public LockState jvngetLock() {
		return this.lock;

	}

	synchronized public boolean jvnLockReadHandler() throws JvnException {
		switch (this.lock) {
		case NOLOCK:
			Serializable o = LOCAL_SERVER.jvnLockRead(this.jvnGetObjectId());
			if(o == null) {
				return false;
			}
			this.serializableObject = o;
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
			// impossible
			break;
		}
		return true;
	}
	
	@Override
	public void jvnLockRead() throws JvnException {
		if(!jvnLockReadHandler()) {
			synchronized(this.waitForReadLockNotify) {
				try {
					this.waitForReadLockNotify.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	synchronized public boolean jvnLockWriteHandler() throws JvnException {
		Serializable o;
		switch (this.lock) {
		case NOLOCK:
			o = LOCAL_SERVER.jvnLockWrite(this.jvnGetObjectId());
			if(o == null) {
				return false;
			}
			this.serializableObject = o;
			this.lock = LockState.WRITE;
			break;
		case READCACHED:
			o = LOCAL_SERVER.jvnLockWrite(this.jvnGetObjectId());
			if(o == null) {
				return false;
			}
			this.serializableObject = o;
			this.lock = LockState.WRITE;
			break;
		case WRITECACHED:
			this.lock = LockState.WRITE;
			break;
		case READ:
			o = LOCAL_SERVER.jvnLockWrite(this.jvnGetObjectId());
			if(o == null) {
				return false;
			}
			this.serializableObject = o;
			this.lock = LockState.WRITE;
			break;
		case WRITE:
			// do nothing
			break;
		case WRITECACHEDREAD:
			this.lock = LockState.WRITE;
			break;
		default:
			// impossible
			break;
		}
		return true;
	}
	
	@Override
	public void jvnLockWrite() throws JvnException {
		if(!jvnLockWriteHandler()) {
			synchronized(this.waitForWriteLockNotify) {
				try {
					this.waitForWriteLockNotify.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	synchronized public void notifyWaitingWriter(Serializable o) {
		this.lock = LockState.WRITE;
		this.serializableObject = o;
		synchronized(this.waitForWriteLockNotify) {
			this.waitForWriteLockNotify.notifyAll();
		}
		synchronized(this.waitForReadLockNotify) {
			this.waitForReadLockNotify.notifyAll();
		}
	}
	
	@Override
	synchronized public void notifyWaitingReader(Serializable o) {
		this.lock = LockState.READ;
		this.serializableObject = o;
		synchronized(this.waitForReadLockNotify) {
			this.waitForReadLockNotify.notifyAll();
		}
	}
	
	

	/**
	 *
	 */
	@Override
	synchronized public boolean jvnUnLock() throws JvnException {
		if(this.HasBeenInvalidated) {
			this.HasBeenInvalidated	= false;
			this.lock 				= LockState.NOLOCK;
			this.notifyAll();
			return false;
		}

		switch (this.lock) {
		case READ:
			this.lock = LockState.READCACHED;
			break;
		case READCACHED:
			this.lock = LockState.READCACHED;
			break;
		case WRITECACHED:
			this.lock = LockState.WRITECACHED;
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
		return true;
	}

	@Override
	public void defaultLock() {
		this.lock = LockState.NOLOCK;
	}

	@Override
	public int jvnGetObjectId(){
		return this.jvnObjectId;
	}

	@Override
	synchronized public void jvnInvalidateReader() throws JvnException {
		while(this.lock == LockState.READ) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.lock = LockState.NOLOCK;
	}

	@Override
	synchronized public Serializable jvnInvalidateWriter() throws JvnException {
		while(this.lock == LockState.WRITE) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.lock = LockState.NOLOCK;
		return this.serializableObject;
	}

	@Override
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
		return this.serializableObject;
	}

	@Override
	public Serializable jvnGetObjectState() throws JvnException {
		return this.serializableObject;
	}

	@Override
	public void setSerializableObject(Serializable o) {
		this.serializableObject = o;
	}

	@Override
	synchronized public boolean isFreeOfLock() {
		return this.lock == LockState.NOLOCK || this.lock == LockState.READCACHED || this.lock == LockState.WRITECACHED;
	}

	@Override
	public void jvnInvalidatePremptively() throws JvnException {
		this.HasBeenInvalidated = true;
	}

	@Override
	public String toString() {
		return "["+this.lock.toString() + "]" + " " + this.serializableObject.toString();
	}

	@Override
	public String getLockStatus() {
		return this.lock.toString();
	}
}
