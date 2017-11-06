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

/**
 * @author Paul Carretero
 * Base pour les coordinateurs master et slave.
 * Offre certaines fonction de base communes, notament les représentation des objet contenu. 
 */
public abstract class JvnAbstractCoord extends UnicastRemoteObject implements JvnRemoteCoordExtended{
	

	/**
	 * serialVersionUID
	 */
	protected static final long serialVersionUID 			 = -5906347883903342080L;
	
	/**
	 * temps en milliseconde avant de mettre la demande de verrou 
	 * en asynchrone avec callback (par exemple afin d'éviter un deadlock)
	 */
	protected static final int MAX_WAIT_TIME_BEFORE_QUEUEING = 20;
	
	/**
	 * true si l'on doit également tenter de prendre un verrou pour les demande en lecture
	 * Peut dégrader les performance légèrement
	 */
	protected static final boolean TRYLOCK_ON_READ			 = false;
	
	/**
	 * host rmi
	 */
	protected static final String HOST 						 = "//localhost/";

	/**
	 * nombre de serveur en attente pour obtenir un verrou en ecriture par objet JVN
	 */
	protected Map<Integer,AtomicInteger>	waitingWriters;
	
	/**
	 * Map assiant un verrou à chaque objet JVN
	 */
	protected Map<Integer,Lock> 			objectLocks;

	/**
	 * Ensemble Objets JVN stockés
	 */
	protected JvnObjectMapCoord jvnObjects;
	
	/**
	 * reference vers le LoadBalancer (master)
	 */
	protected JvnLoadBalancer	jvnLoadBalancer;
	
	/**
	 * registre rmi local
	 */
	protected final Registry	rmiRegistry;

	/**
	 * Default constructor
	 * @throws RemoteException 
	 * @throws MalformedURLException 
	 **/
	public JvnAbstractCoord() throws RemoteException, MalformedURLException {
		super();
		this.rmiRegistry		= LocateRegistry.getRegistry();
		try {
			this.jvnLoadBalancer = (JvnLoadBalancer) this.rmiRegistry.lookup("JvnLoadBalancer");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		this.waitingWriters 	= new ConcurrentHashMap<Integer,AtomicInteger>();
		this.objectLocks 		= new ConcurrentHashMap<Integer,Lock>();
	}

	@Override
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws RemoteException{
		return this.jvnObjects.get(jon);
	}
	
	/**
	 * met le thread en attente (1 seule fois) si il y a des demande de verou en écriture sur cet objet
	 * @param joi id d'un objet javanaise
	 */
	public void waitOnWW(int joi) {
		AtomicInteger ww = this.waitingWriters.get(joi);
		if(ww == null) {
			this.waitingWriters.put(joi, new AtomicInteger(0));
			ww = this.waitingWriters.get(joi);
		}
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
	 * @throws RemoteException 
	 **/
	@Override
	public void jvnTerminate(JvnRemoteServer js) throws RemoteException {
		this.jvnObjects.cleanUpServer(js, true);
	}
	
	/**
	 * retire les verrou d'un serveur Jvn inaccessible
	 * @param js un serveur inaccessible
	 */
	protected void jvnRemoveOnFail(JvnRemoteServer js){
		this.jvnObjects.cleanUpServer(js, false);
	}

	@Override
	public void invalidateKey(int joi, Serializable o, JvnRemoteServer js) throws RemoteException {
		this.jvnObjects.cleanUpKey(joi,o,js);
	}
	
	@Override
	public int jvnGetObjectId() throws RemoteException, JvnException {
		throw new JvnException("this is not a loadBalancer");
	}
}
