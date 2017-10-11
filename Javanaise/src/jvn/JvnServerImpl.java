/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;



public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer{

	private static final long serialVersionUID = 1L;

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
	private JvnObjectMap LocalsJvnObject;

	/**
	 * Référence vers le registre RMI
	 */
	private Registry rmiRegistry;

	/**
	 * Default constructor
	 * @throws JvnException
	 **/
	private JvnServerImpl(final boolean localOnly) throws Exception {
		super();
		this.LocalsJvnObject	= new JvnObjectMapServ();
		if(localOnly) {
			this.rmiRegistry		= null;
			this.jvnRemoteCoord 	= null;
		}
		else {
			this.rmiRegistry		= LocateRegistry.getRegistry(HOST);
			this.jvnRemoteCoord 	= (JvnRemoteCoord) this.rmiRegistry.lookup("JvnCoord");
		}
	}

	/**
	 * Static method allowing an application to get a reference to 
	 * a JVN server instance
	 * @throws JvnException
	 **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl(false);
			} catch (Exception e) {
				try {
					js = new JvnServerImpl(true);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
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
		Shared.log("JvnServImpl","jvnTerminate ");
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
		Shared.log("JvnServImpl","jvnCreateObject ");
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
			Shared.log("JvnServImpl","jvnRegisterObject");
			try {
				this.jvnRemoteCoord.jvnRegisterObject(jon, jo, this);
				this.LocalsJvnObject.put(jo, jon, this);
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
			Shared.log("JvnServImpl","jvnLookupObject" + jon);
			JvnObject jvnObj = this.LocalsJvnObject.get(jon);
			if (jvnObj != null){
				return jvnObj;
			}
			try {
				JvnObject jo = this.jvnRemoteCoord.jvnLookupObject(jon, this);
				if(jo != null) {
					this.LocalsJvnObject.put(jo, jon, this);
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
		Shared.log("JvnServImpl","jvnLookupObject" + joi);
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
		Shared.log("JvnServImpl","jvnLockWrite " + joi);
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
	 * @return void
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public void jvnInvalidateReader(final int joi) throws java.rmi.RemoteException,jvn.JvnException {
		Shared.log("JvnServImpl","jvnInvalidateReader " + joi);
		this.LocalsJvnObject.get(joi).jvnInvalidateReader();
	}

	/**
	 * Invalidate the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		Shared.log("JvnServImpl","jvnInvalidateWriter " + joi);
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
		Shared.log("JvnServImpl","jvnInvalidateWriterForReader " + joi);
		Serializable o = this.LocalsJvnObject.get(joi).jvnInvalidateWriterForReader();
		this.LocalsJvnObject.get(joi).setSerializableObject(o);
		return o;
	}

	public void invalideKey(final int intKey) {
		Shared.log("JvnServImpl","invalideKey " + intKey);
		try {
			this.jvnRemoteCoord.invalidateKey(intKey,this);
		} catch (RemoteException | JvnException e) {
			e.printStackTrace();
		}
	}
}


