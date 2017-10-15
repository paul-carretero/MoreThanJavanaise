package jvn.jvnObject;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import jvn.JvnException;
import jvn.jvnServer.JvnLocalServer;
import jvn.jvnServer.JvnServerImpl;

/**
 * @author Paul Carretero
 * Implémentation d'un objet Javanaise
 */
public class JvnObjectImpl implements JvnObject {

	/**
	 * represente l'etat d'un verrou sur un objet Javanaise
	 * Il s'agit du type de verrou sur cet objet que possède le serveur et non l'application (synchro applicatif nécessaire)
	 */
	public enum LockState{
		/**
		 * pas de verrou
		 */
		NOLOCK,
		/**
		 * pas de verrou en cours mais le serveur possède un verrou en lecture
		 */
		READCACHED,
		/**
		 * pas de verrou en cours mais le serveur possède un verrou en ecriture
		 */
		WRITECACHED,
		/**
		 * verrou en lecture
		 */
		READ,
		/**
		 * verrou en ecriture
		 */
		WRITE,
		/**
		 * verrou en lecture mais le serveur possède un verrou en ecriture
		 */
		WRITECACHEDREAD;
	}

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID 			= -4993207529464463527L;
	/**
	 * Instance du serveur javanaise local
	 */
	private static final JvnLocalServer	LOCAL_SERVER 	= JvnServerImpl.jvnGetServer();

	/**
	 * identifiant unique sur l'ensemble du systeme de cet objet
	 */
	private final int 		jvnObjectId;
	/**
	 * objet applicatif encapsulé dans cet objet javanaise
	 */
	private Serializable	serializableObject;

	/**
	 * vrai si le coordinateur à invalidé le verrou sur cet objet
	 */
	private volatile boolean	HasBeenInvalidated;
	/**
	 * etat du verrou sur cet objet sur le serveur local
	 */
	private volatile LockState	lock;

	/**
	 * monitor pour l'attente d'un verrou en lecture
	 */
	private final Lock appLevelLock;

	/**
	 * @param jvnObjectId
	 * @param serializableObject
	 */
	public JvnObjectImpl(int jvnObjectId, Serializable serializableObject) {
		this.serializableObject	= serializableObject;
		this.jvnObjectId 		= jvnObjectId;
		this.lock				= LockState.WRITE;
		this.HasBeenInvalidated	= false;
		this.appLevelLock		= new ReentrantLock(true);
	}

	/**
	 * utilisé pour les test
	 * @return l'état courrant du verrou sur cet objet
	 */
	public LockState jvngetLock() {
		return this.lock;

	}

	/**
	 * Demande un verrou en lecture au coordinateur si l'on ne n'en dispose pas déjà (ou mieux).
	 * @return true si l'on dispose du verrou en lecture, 
	 * faux si on recevra un callback du coordinateur pour le donner au serveur de cache ulterieurement
	 * @throws JvnException
	 */
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
			break;
		case WRITE:
			this.lock = LockState.WRITECACHEDREAD;
			break;
		case WRITECACHEDREAD:
			break;
		default:
			throw new JvnException("etat du verrou inconsistant sur l'objet");
		}
		return true;
	}

	@Override
	synchronized public void jvnLockRead() throws JvnException {
		this.appLevelLock.lock();
		if(!jvnLockReadHandler()) {
			try {
				this.wait();
				this.lock = LockState.READ;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.appLevelLock.unlock();
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
			this.lock = LockState.READCACHED;
			this.notifyAll();
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
			throw new JvnException("etat du verrou inconsistant sur l'objet");
		}
		return true;
	}

	@Override
	synchronized public boolean jvnLockWrite() throws JvnException {
		this.appLevelLock.lock();
		boolean askForUpgrade = this.lock == LockState.READ;
		boolean res = true;
		if(!jvnLockWriteHandler()) {
			try {
				this.wait();
				res = (!askForUpgrade || this.lock == LockState.READCACHED);
				this.lock = LockState.WRITE;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.appLevelLock.unlock();
		return res;
	}

	@Override
	synchronized public void notifyWaiters(Serializable o) {
		this.serializableObject = o;
		this.notifyAll();
	}

	/**
	 *
	 */
	@Override
	synchronized public boolean jvnUnLock() throws JvnException {
		this.appLevelLock.lock();
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
		this.appLevelLock.unlock();
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
}