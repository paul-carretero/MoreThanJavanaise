package jvn;

import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JvnObjectImpl implements JvnObject {

	private static final long 	serialVersionUID	= 1L;

	private final int 		jvnObjectId;
	private Serializable	serializableObject;
	
	private final Lock threadLock;
	private final Condition waitingServers;

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
		this.threadLock			= new ReentrantLock();
		this.waitingServers		= this.threadLock.newCondition();
	}

	public void jvnLockRead() throws JvnException {
		this.threadLock.lock();
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
		this.threadLock.unlock();
	}

	public void jvnLockWrite() throws JvnException {
		this.threadLock.lock();
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
		this.threadLock.unlock();
	}

	public void jvnUnLock() throws JvnException {
		this.threadLock.lock();
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
		this.threadLock.unlock();
	}

	public int jvnGetObjectId() throws JvnException {
		return this.jvnObjectId;
	}

	public void jvnInvalidateReader() throws JvnException {
		this.threadLock.lock();
		try {
			while(this.lock == LockState.READ) {
				this.waitingServers.await();
			}
			this.lock = LockState.NOLOCK;
			this.waitingServers.signalAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			this.threadLock.unlock();
		}
	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		this.threadLock.lock();
		try {
			while(this.lock == LockState.WRITE) {
				this.waitingServers.await();
			}
			this.lock = LockState.NOLOCK;
			this.waitingServers.signalAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			this.threadLock.unlock();
		}
		return this.serializableObject;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		this.threadLock.lock();
		try {
			while(this.lock == LockState.WRITE) {
				this.waitingServers.await();
			}
			if (this.lock == LockState.WRITECACHED) {
				this.lock = LockState.READCACHED;
			}
			else if (this.lock == LockState.WRITECACHEDREAD) {
				this.lock = LockState.READ;
			}
			this.waitingServers.signalAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			this.threadLock.unlock();
		}
		return this.serializableObject;
	}

	synchronized public Serializable jvnGetObjectState() throws JvnException {
		return this.serializableObject;
	}
	
	synchronized public void setSerializableObject(Serializable o) {
		this.serializableObject = o;
	}

}
