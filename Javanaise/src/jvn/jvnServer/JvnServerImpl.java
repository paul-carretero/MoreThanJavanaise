/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn.jvnServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import jvn.JvnException;
import jvn.jvnCoord.JvnRemoteCoord;
import jvn.jvnObject.JvnObject;
import jvn.jvnObject.JvnObjectImpl;

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
	 * Emplacement du registre RMI
	 */
	private static final String HOST = "localhost";

	/**
	 * A JVN server is managed as a singleton 
	 */
	private static JvnServerImpl js = null;

	/**
	 * Référence vers le Coordinateur
	 */
	private JvnRemoteCoord jvnRemoteCoord;

	/**
	 * "Cache" des objets JVN stockés localements
	 */
	private JvnObjectMapServ LocalsJvnObject;

	/**
	 * Référence vers le registre RMI
	 */
	private Registry rmiRegistry;

	/**
	 * Default constructor
	 * @param localOnly true si l'on ne doit pas tenter de se connecter à un coordinateur distant
	 * @throws Exception 
	 * @throws JvnException
	 **/
	private JvnServerImpl(final boolean localOnly) throws Exception {
		super();
		this.LocalsJvnObject	= new JvnObjectMapServ();
		if(localOnly) {
			this.rmiRegistry	= null;
			this.jvnRemoteCoord = null;
		}
		else {
			this.rmiRegistry	= LocateRegistry.getRegistry(HOST);
			this.jvnRemoteCoord = (JvnRemoteCoord) this.rmiRegistry.lookup("JvnCoord");
		}
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
	public void jvnTerminate() throws jvn.JvnException {
		try {
			this.jvnRemoteCoord.jvnTerminate(this);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new jvn.JvnException("Error during termination of local server");
		}
	} 

	/**
	 * creation of a JVN object
	 * @param o : the JVN object state
	 * @throws JvnException
	 **/
	@Override
	public JvnObject jvnCreateObject(final Serializable o) throws jvn.JvnException { 
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
	public void jvnRegisterObject(final String jon, final JvnObject jo) throws jvn.JvnException {
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
	public JvnObject jvnLookupObject(final String jon) throws jvn.JvnException {
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
	public void jvnInvalidateReader(final int joi) throws java.rmi.RemoteException,jvn.JvnException {
		this.LocalsJvnObject.get(joi).jvnInvalidateReader();
	}

	/**
	 * Invalidate the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public Serializable jvnInvalidateWriter(final int joi) throws java.rmi.RemoteException,jvn.JvnException { 
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
	public Serializable jvnInvalidateWriterForReader(final int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		Serializable o = this.LocalsJvnObject.get(joi).jvnInvalidateWriterForReader();
		this.LocalsJvnObject.get(joi).setSerializableObject(o);
		return o;
	}

	/**
	 * @param joi identifiant de l'objet supprime du cache local
	 */
	public void invalideKey(final int joi) {
		try {
			this.jvnRemoteCoord.invalidateKey(joi,this.LocalsJvnObject.get(joi),this);
			this.LocalsJvnObject.removeFromAssocMap(joi);
		} catch (RemoteException | JvnException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void clearCache(final boolean hard) throws JvnException {
		this.LocalsJvnObject = new JvnObjectMapServ();
		if(hard && this.jvnRemoteCoord != null) {
			try {
				this.jvnRemoteCoord.jvnResetCoord();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void notifyForReadWriteLock(final int joi, final Serializable o) throws RemoteException {
		this.LocalsJvnObject.get(joi).notifyWaiters(o);
	}
}


