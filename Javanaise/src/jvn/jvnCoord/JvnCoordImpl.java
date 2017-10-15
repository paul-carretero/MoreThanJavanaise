/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn.jvnCoord;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import jvn.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

import java.io.Serializable;
import java.net.MalformedURLException;


public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID				= 5217895175945110282L;
	private static final int MAX_WAIT_TIME_BEFORE_QUEUEING	= 20;
	private static final boolean TRYLOCK_ON_READ			= false;
	private static final String HOST 						= "//localhost/";

	private final AtomicInteger currentOjectId;
	private Map<Integer,AtomicInteger> 	waitingWriters;
	private Map<Integer,Lock> 			objectLocks;
	private final Executor				threadPool;

	/**
	 * Ensemble Objets JVN stockés
	 */
	private JvnObjectMapCoord jvnObjects;


	/**
	 * Default constructor
	 * @throws JvnException
	 * @throws RemoteException 
	 * @throws MalformedURLException 
	 **/
	public JvnCoordImpl() throws JvnException, RemoteException, MalformedURLException {
		super();
		Naming.rebind(HOST+"JvnCoord", this);
		this.currentOjectId = new AtomicInteger();
		this.waitingWriters = new ConcurrentHashMap<Integer,AtomicInteger>();
		this.objectLocks 	= new ConcurrentHashMap<Integer,Lock>();
		this.jvnObjects 	= new JvnObjectMapCoord();
		this.threadPool		= Executors.newCachedThreadPool();
	}

	/**
	 *  Allocate a NEW JVN object id (usually allocated to a newly created JVN object)
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public int jvnGetObjectId() throws java.rmi.RemoteException,jvn.JvnException {
		return this.currentOjectId.getAndIncrement();
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo  : the JVN object 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		synchronized (jon.intern()) {
			jo.defaultLock();
			this.jvnObjects.put(jo, jon, js);
			this.objectLocks.put(jo.jvnGetObjectId(), new ReentrantLock(true));
			this.waitingWriters.put(jo.jvnGetObjectId(), new AtomicInteger(0));
		}

	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server 
	 * @param jon : the JVN object name
	 * @param js : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		return this.jvnObjects.get(jon);
	}

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	@Override
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		if(TRYLOCK_ON_READ) {
			if(this.waitingWriters.get(joi).get() == 0 && this.objectLocks.get(joi).tryLock()) {
				Serializable o = jvnLockReadHandler(joi,js);
				this.objectLocks.get(joi).unlock();
				return o;
			}
			this.threadPool.execute(new JvnCoordNotifyWorker(this, joi, js, true));
			return null;
		}
		waitOnWW(joi);
		return jvnLockReadHandler(joi,js);
	}

	public Serializable jvnLockReadHandler(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		this.objectLocks.get(joi).lock();
		if(this.jvnObjects.getWritingServer(joi) != null && !this.jvnObjects.getWritingServer(joi).equals(js)) {
			this.jvnObjects.get(joi).setSerializableObject(this.jvnObjects.getWritingServer(joi).jvnInvalidateWriterForReader(joi));
			this.jvnObjects.addReadingServer(joi, this.jvnObjects.getWritingServer(joi));
			this.jvnObjects.setWritingServer(joi, null);
		}
		this.jvnObjects.addReadingServer(joi, js);
		Serializable o = this.jvnObjects.get(joi).jvnGetObjectState();
		this.objectLocks.get(joi).unlock();
		return o;
	}

	/**
	 * met le thread en attente (1 seule fois) si il y a des demande de verou en écriture sur cet objet
	 * @param joi id d'un objet javanaise
	 */
	public void waitOnWW(int joi) {
		AtomicInteger ww = this.waitingWriters.get(joi);
		if(ww.get() > 0) {
			synchronized (ww) {
				try {
					ww.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Serializable jvnLockWriteHandler(int joi, JvnRemoteServer js) throws RemoteException, JvnException {
		this.objectLocks.get(joi).lock();

		if(this.jvnObjects.getWritingServer(joi) != null && !this.jvnObjects.getWritingServer(joi).equals(js)) {
			this.jvnObjects.get(joi).setSerializableObject(this.jvnObjects.getWritingServer(joi).jvnInvalidateWriter(joi));
		}
		
		for(JvnRemoteServer server : this.jvnObjects.getReadingServer(joi)) {
			if(!server.equals(js)) {
				server.jvnInvalidateReader(joi);
			}
		}
		this.jvnObjects.resetReadingServer(joi);

		this.jvnObjects.setWritingServer(joi, js);
		this.waitingWriters.get(joi).decrementAndGet();
		synchronized (this.waitingWriters.get(joi)) {
			this.waitingWriters.get(joi).notifyAll();
		}

		Serializable o = this.jvnObjects.get(joi).jvnGetObjectState();
		this.objectLocks.get(joi).unlock();
		return o;
	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server 
	 * @param joi : the JVN object identification
	 * @param js  : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	@Override
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException{
		this.waitingWriters.get(joi).incrementAndGet();
		try {
			if(this.objectLocks.get(joi).tryLock(MAX_WAIT_TIME_BEFORE_QUEUEING, TimeUnit.MILLISECONDS)) {
				Serializable o = jvnLockWriteHandler(joi,js);
				this.objectLocks.get(joi).unlock();
				return o;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.threadPool.execute(new JvnCoordNotifyWorker(this, joi, js, false));
		return null;
	}

	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	@Override
	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		this.jvnObjects.cleanUpServer(js);
	}

	@Override
	public void invalidateKey(int key, Serializable o, JvnRemoteServer js) {
		this.jvnObjects.cleanUpKey(key,o,js);
	}

	/**
	 * seulement pour les tests
	 * réinitialise la base du coordinateur
	 */
	@Override
	public void jvnResetCoord() throws java.rmi.RemoteException, JvnException {
		this.currentOjectId.set(0);
		this.waitingWriters = new ConcurrentHashMap<Integer,AtomicInteger>();
		this.jvnObjects 	= new JvnObjectMapCoord();
		this.objectLocks 	= new ConcurrentHashMap<Integer,Lock>();
	}

	public static void main(String argv[]) throws Exception {
		System.out.println("JvnCoordImpl started ");
		new JvnCoordImpl();
	}
}


