package jvn.jvnCoord.JvnLogicalCoord;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import jvn.jvnCoord.jvnLoadBalancer.JvnLoadBalancer;
import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

public abstract class JvnAbstractCoord extends UnicastRemoteObject implements JvnRemoteCoord{
	

	/**
	 * serialVersionUID
	 */
	protected static final long serialVersionUID = -5906347883903342080L;
	protected static final int MAX_WAIT_TIME_BEFORE_QUEUEING = 20;
	protected static final boolean TRYLOCK_ON_READ			 = false;
	protected static final String HOST 						 = "//localhost/";

	protected Map<Integer,AtomicInteger> 	waitingWriters;
	protected Map<Integer,Lock> 			objectLocks;

	/**
	 * Ensemble Objets JVN stockés
	 */
	protected JvnObjectMapCoord jvnObjects;
	protected JvnLoadBalancer	jvnLoadBalancer;
	private final Registry		rmiRegistry;

	/**
	 * Default constructor
	 * @throws JvnException
	 * @throws RemoteException 
	 * @throws MalformedURLException 
	 **/
	public JvnAbstractCoord() throws JvnException, RemoteException, MalformedURLException {
		super();
		this.rmiRegistry		= LocateRegistry.getRegistry();
		try {
			this.jvnLoadBalancer	= (JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancer");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		this.waitingWriters 	= new ConcurrentHashMap<Integer,AtomicInteger>();
		this.objectLocks 		= new ConcurrentHashMap<Integer,Lock>();
		this.jvnObjects 		= new JvnObjectMapCoord();
	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server 
	 * @param jon : the JVN object name
	 * @param js : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.jvnExceptions.JvnException{
		return this.jvnObjects.get(jon);
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
	
	@Override
	public int jvnGetObjectId() throws RemoteException, JvnException {
		throw new JvnException("this is not a loadBalancer");
	}
}
