/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn.jvnServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import jvn.jvnCoord.JvnRemoteCoord;
import jvn.jvnExceptions.JvnException;
import jvn.jvnExceptions.JvnPreemptiveInvalidationException;
import jvn.jvnExceptions.JvnTransactionException;
import jvn.jvnObject.JvnObject;
import jvn.jvnObject.JvnObjectImpl;
import jvn.proxy.CallHandler;

import java.io.*;

/**
 * @author Paul Carretero
 * Implémentation d'un serveur de cache local.
 * Gérer comme un singleton.
 */
public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 710108656689998806L;

	/**
	 * A JVN server is managed as a singleton 
	 */
	private static JvnServerImpl js = null;

	/**
	 * Référence vers le Coordinateur
	 */
	private final JvnRemoteCoord jvnRemoteCoord;

	/**
	 * "Cache" des objets JVN stockés localements
	 */
	private final JvnObjectMapServ LocalsJvnObject;
	
	/**
	 * Map de thread => Map d'objet (id->valeur) des transactions
	 * Choix d'implémentation : deux thread locaux ne peuvent pas obtenir des verrou sur des objets identique
	 */
	private final Map<Thread,Map<Integer,JvnTransactData>> transactMasterMap;

	/**
	 * Default constructor
	 * @param localOnly true si l'on ne doit pas tenter de se connecter à un coordinateur distant
	 * @throws Exception 
	 * @throws JvnException
	 **/
	private JvnServerImpl(final boolean localOnly) throws Exception {
		super();
		this.LocalsJvnObject	= new JvnObjectMapServ();
		this.jvnRemoteCoord 	= new CallHandler();
		this.transactMasterMap	= new ConcurrentHashMap<>();
	}

	/**
	 * Static method allowing an application to get a reference to 
	 * a JVN server instance
	 * @return Une implémentation de JvnServerImpl gérée en singleton
	 **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return js;
	}

	/**
	 * The JVN service is not used anymore
	 * @throws JvnException
	 **/
	@Override
	public void jvnTerminate() throws jvn.jvnExceptions.JvnException {
		try {
			if(js != null) {
				this.jvnRemoteCoord.jvnTerminate(this);
				js = null;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new jvn.jvnExceptions.JvnException("Error during termination of local server");
		}
	} 

	/**
	 * creation of a JVN object
	 * @param o : the JVN object state
	 * @throws JvnException
	 **/
	@Override
	public JvnObject jvnCreateObject(final Serializable o) throws jvn.jvnExceptions.JvnException { 
		try {
			return new JvnObjectImpl(this.jvnRemoteCoord.jvnGetObjectId(), o);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new JvnException("Error during Object creation");
		}
	}

	/**
	 *  Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo : the JVN object 
	 * @throws JvnException
	 **/
	@Override
	public void jvnRegisterObject(final String jon, final JvnObject jo) throws jvn.jvnExceptions.JvnException {
		synchronized (jon.intern()) {
			try {
				this.jvnRemoteCoord.jvnRegisterObject(jon, jo, this);
				this.LocalsJvnObject.put(jo, jon);
			} catch (RemoteException e) {
				e.printStackTrace();
				throw new JvnException("Erreur lors de l'enregistrement de l'objet");
			}
		}
	}

	/**
	 * Provide the reference of a JVN object beeing given its symbolic name
	 * @param jon : the JVN object name
	 * @return the JVN object 
	 * @throws JvnException
	 **/
	@Override
	public JvnObject jvnLookupObject(final String jon) throws jvn.jvnExceptions.JvnException {
		synchronized (jon.intern()) {
			JvnObject jvnObj = this.LocalsJvnObject.get(jon);
			if (jvnObj != null){
				return jvnObj;
			}
			try {
				JvnObject jo = this.jvnRemoteCoord.jvnLookupObject(jon, this);
				if(jo != null) {
					this.LocalsJvnObject.put(jo, jon);
				}
				return jo;
			} catch (RemoteException e) {
				e.printStackTrace();
				throw new JvnException("Error in Object Lookup");
			}
		}
	}	

	/**
	 * Get a Read lock on a JVN object 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws  JvnException
	 **/
	@Override
	public Serializable jvnLockRead(final int joi) throws JvnException {
		try {
			return this.jvnRemoteCoord.jvnLockRead(joi, this);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new JvnException("Error in Object Lock Read");
		}
	}

	/**
	 * Get a Write lock on a JVN object 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws  JvnException
	 **/
	@Override
	public Serializable jvnLockWrite(final int joi) throws JvnException {
		try {
			return this.jvnRemoteCoord.jvnLockWrite(joi, this);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new JvnException("Error in Object Lock Read");
		}
	}	


	/**
	 * Invalidate the Read lock of the JVN object identified by id 
	 * called by the JvnCoord
	 * @param joi : the JVN object id
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public void jvnInvalidateReader(final int joi) throws RemoteException, JvnException {
		this.LocalsJvnObject.get(joi).jvnInvalidateReader();
	}

	/**
	 * Invalidate the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public Serializable jvnInvalidateWriter(final int joi) throws RemoteException, JvnException {
		Serializable o = this.LocalsJvnObject.get(joi).jvnInvalidateWriter();
		this.LocalsJvnObject.get(joi).setSerializableObject(o);
		return o;
	}

	/**
	 * Reduce the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public Serializable jvnInvalidateWriterForReader(final int joi) throws RemoteException, JvnException { 
		Serializable o = this.LocalsJvnObject.get(joi).jvnInvalidateWriterForReader();
		this.LocalsJvnObject.get(joi).setSerializableObject(o);
		return o;
	}

	/**
	 * @param joi identifiant de l'objet supprime du cache local
	 * @param o l'objet applicatif associé
	 */
	public void invalideKey(final int joi, Serializable o) {
		try {
			this.jvnRemoteCoord.invalidateKey(joi,o,this);
			this.LocalsJvnObject.removeFromAssocMap(joi);
		} catch (RemoteException | JvnException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void notifyForReadWriteLock(final int joi, final Serializable o) throws RemoteException {
		this.LocalsJvnObject.get(joi).notifyWaiters(o);
	}
	
	@Override
	public boolean isInTransaction() {
		return this.transactMasterMap.containsKey(Thread.currentThread());
	}

	@Override
	public void beginTransaction() throws JvnTransactionException {
		this.transactMasterMap.put(Thread.currentThread(), new ConcurrentHashMap<>());
	}

	@Override
	public void commitTransaction() throws JvnException, JvnPreemptiveInvalidationException, JvnTransactionException {
		if(!isInTransaction()) {
			throw new JvnTransactionException("pas dans une transaction, commit impossible");
		}
		
		for (Entry<Integer, JvnTransactData> entry : this.transactMasterMap.get(Thread.currentThread()).entrySet())
		{
			for(int i = 0; i < entry.getValue().getTotalLockCount(); i++) {
				this.LocalsJvnObject.get(entry.getKey()).jvnUnLock();
			}
		}
		
		this.transactMasterMap.remove(Thread.currentThread());
	}

	@Override
	public void rollbackTransaction() throws JvnException, JvnPreemptiveInvalidationException, JvnTransactionException {
		if(!isInTransaction()) {
			throw new JvnTransactionException("pas dans une transaction, rollback impossible");
		}
				
		for (Entry<Integer, JvnTransactData> entry : this.transactMasterMap.get(Thread.currentThread()).entrySet())
		{
			if(entry.getValue().haveBackup()) {
				this.LocalsJvnObject.updateJvnObject(entry.getKey(), entry.getValue().getSerializableObject());
			}
		    
		    for(int i = 0; i < entry.getValue().getTotalLockCount(); i++) {
				this.LocalsJvnObject.get(entry.getKey()).jvnUnLock();
			}
		}
		
		this.transactMasterMap.remove(Thread.currentThread());
	}
	
	/**
	 * vérifie que nous somme dans une transaction et ajoute si besoin un objet transactData dans la transactmastermap (pour tenir compte de l'objet)
	 * @param jo un objet JVN
	 * @throws JvnTransactionException
	 */
	private void checkTransactData(final JvnObject jo) throws JvnTransactionException{
		if(!isInTransaction()) {
			throw new JvnTransactionException("pas dans une transaction, register impossible");
		}
		
		if(this.transactMasterMap.get(Thread.currentThread()).get(jo.jvnGetObjectId()) == null) {
			this.transactMasterMap.get(Thread.currentThread()).put(jo.jvnGetObjectId(), new JvnTransactData());
		}
	}
	
	@Override
	synchronized public void readRegisterInTransaction(final JvnObject jo) throws JvnTransactionException {
		checkTransactData(jo);
		this.transactMasterMap.get(Thread.currentThread()).get(jo.jvnGetObjectId()).read();
	}
	
	@Override
	synchronized public void writeRegisterInTransaction(final JvnObject jo) throws JvnTransactionException, JvnException {
		checkTransactData(jo);
		this.transactMasterMap.get(Thread.currentThread()).get(jo.jvnGetObjectId()).write(jo.jvnGetObjectState());
	}
}


