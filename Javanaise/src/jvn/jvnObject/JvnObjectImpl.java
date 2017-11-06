package jvn.jvnObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import jvn.jvnExceptions.JvnConcurrentLockUpgradeException;
import jvn.jvnExceptions.JvnException;
import jvn.jvnExceptions.JvnPreemptiveInvalidationException;
import jvn.jvnServer.JvnLocalServer;
import jvn.jvnServer.JvnServerImpl;

/**
 * @author Paul Carretero
 * Implémentation d'un objet Javanaise
 */
public class JvnObjectImpl implements JvnObject {
	
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
	private transient volatile boolean HasBeenInvalidated;
	
	/**
	 * etat du verrou sur cet objet sur le serveur local
	 */
	private transient volatile LockState lock;
	
	/**
	 * Compteur du nombre d'appel à un verrou (on ne déverouille que si personne d'autre n'a le verrou)
	 */
	private transient volatile int lockAskedCount;

	/**
	 * constructeur par défault de l'objet javanaise à partir d'un id et de l'objet applicatif serializable
	 * @param jvnObjectId un id unique
	 * @param serializableObject un objet applicatif
	 */
	public JvnObjectImpl(int jvnObjectId, Serializable serializableObject) {
		this.serializableObject	= serializableObject;
		this.jvnObjectId 		= jvnObjectId;
		this.lock				= LockState.WRITECACHED;
		this.HasBeenInvalidated	= false;
		this.lockAskedCount 	= 0;
	}
	
	/**
	 * permet d'initialiser les champs transient lors de la deserialization
	 * @param in ObjectInputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.lock 				= LockState.NOLOCK;
        this.HasBeenInvalidated = false;
        this.lockAskedCount 	= 0;
    }

	/**
	 * utilisé pour les test
	 * @return l'état courrant du verrou sur cet objet
	 */
	@Override
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
		this.lockAskedCount++;
		if(!jvnLockReadHandler()) {
			try {
				this.wait();
				this.lock = LockState.READ;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Demande un verrou en ecriture au coordinateur si l'on ne n'en dispose pas déjà (ou mieux).
	 * @return true si l'on dispose du verrou en ecriture, 
	 * faux si on recevra un callback du coordinateur pour le donner au serveur de cache ulterieurement
	 * @throws JvnException
	 */
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
	synchronized public void jvnLockWrite() throws JvnException, JvnConcurrentLockUpgradeException {
		this.lockAskedCount++;
		boolean askForUpgrade = this.lock == LockState.READ;
		boolean res = true;
		if(!jvnLockWriteHandler()) {
			try {
				this.lock = LockState.READCACHED;
				this.wait();
				res = (!askForUpgrade || this.lock == LockState.READCACHED);
				this.lock = LockState.WRITE;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(!res) {
			throw new JvnConcurrentLockUpgradeException();
		}
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
	synchronized public void jvnUnLock() throws JvnException, JvnPreemptiveInvalidationException {
		this.lockAskedCount--;
		if(this.HasBeenInvalidated) {
			this.HasBeenInvalidated	= false;
			this.lock 				= LockState.NOLOCK;
			this.notifyAll();
			throw new JvnPreemptiveInvalidationException();
		}
		
		if(this.lockAskedCount <= 0) {
			this.lockAskedCount = 0;
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
		}
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
	public Serializable jvnGetObjectState(){
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
