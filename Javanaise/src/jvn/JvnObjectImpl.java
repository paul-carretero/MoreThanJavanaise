package jvn;

import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	
	private final Lock 		threadLock;
	private final Condition	waitingServers;
	
	private volatile boolean	HasBeenInvalidated;
	private volatile LockState	lock;

	public JvnObjectImpl(int jvnObjectId, Serializable serializableObject) {
		this.serializableObject	= serializableObject;
		this.jvnObjectId 		= jvnObjectId;
		this.lock				= LockState.WRITE;
		this.threadLock			= new ReentrantLock();
		this.waitingServers		= this.threadLock.newCondition();
		this.HasBeenInvalidated	= false;
	}
	
	public LockState jvngetLock() {
		this.threadLock.lock();
		LockState l = this.lock;
		this.threadLock.unlock();
		return l;
		
	}

	@Override
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
			// impossible
			break;
		}
		this.threadLock.unlock();
	}

	@Override
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
			// impossible
			break;
		}
		this.threadLock.unlock();
	}

	/**
	 * 
	 */
	@Override
	public boolean jvnUnLock() throws JvnException {
		this.threadLock.lock();
		
		if(this.HasBeenInvalidated) {
			this.HasBeenInvalidated	= false;
			this.lock 				= LockState.NOLOCK;
			this.waitingServers.signal();
			this.threadLock.unlock();
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
		this.waitingServers.signal();
		this.threadLock.unlock();
		return true;
	}
	
	@Override
	public void defaultLock() {
		this.threadLock.lock();
		this.lock = LockState.NOLOCK;
		this.threadLock.unlock();
	}

	@Override
	public int jvnGetObjectId(){
		return this.jvnObjectId;
	}

	@Override
	public void jvnInvalidateReader() throws JvnException {
		this.threadLock.lock();
		try {
			while(this.lock == LockState.READ) {
				this.waitingServers.await();
			}
			this.lock = LockState.NOLOCK;
			this.waitingServers.signal();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			this.threadLock.unlock();
		}
	}

	@Override
	public Serializable jvnInvalidateWriter() throws JvnException {
		this.threadLock.lock();
		try {
			while(this.lock == LockState.WRITE) {
				this.waitingServers.await();
			}
			this.lock = LockState.NOLOCK;
			this.waitingServers.signal();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			this.threadLock.unlock();
		}
		return this.serializableObject;
	}

	@Override
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
			this.waitingServers.signal();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally {
			this.threadLock.unlock();
		}
		return this.serializableObject;
	}

	@Override
	synchronized public Serializable jvnGetObjectState() throws JvnException {
		return this.serializableObject;
	}
	
	@Override
	synchronized public void setSerializableObject(Serializable o) {
		this.serializableObject = o;
	}

	@Override
	public boolean isFreeOfLock() {
		synchronized (this.lock) {
			return this.lock == LockState.NOLOCK || this.lock == LockState.READCACHED || this.lock == LockState.WRITECACHED;
		}
	}
	
	@Override
	public void jvnInvalidatePremptively() throws JvnException {
		this.HasBeenInvalidated = true;
	}
	
	@Override
	public String toString() {
		return "["+this.lock.toString() + "]" + " " + this.serializableObject.toString();
	}
}
