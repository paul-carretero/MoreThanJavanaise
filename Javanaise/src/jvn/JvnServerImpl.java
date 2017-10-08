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
	private JvnServerImpl() throws Exception {
		super();
		this.LocalsJvnObject	= new JvnObjectMap();
		this.rmiRegistry		= LocateRegistry.getRegistry(HOST);
		this.jvnRemoteCoord 	= (JvnRemoteCoord) this.rmiRegistry.lookup("JvnCoord");
	}

	/**
	 * Static method allowing an application to get a reference to 
	 * a JVN server instance
	 * @throws JvnException
	 **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return js;
	}

	/**
	 * The JVN service is not used anymore
	 * @throws JvnException
	 **/
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
	public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException { 
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
	public void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException {
		try {
			this.jvnRemoteCoord.jvnRegisterObject(jon, jo, this);
			this.LocalsJvnObject.put(jo, jon, this);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new JvnException("Erreur lors de l'enregistrement de l'objet");
		}
	}

	/**
	 * Provide the reference of a JVN object beeing given its symbolic name
	 * @param jon : the JVN object name
	 * @return the JVN object 
	 * @throws JvnException
	 **/
	synchronized public JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
		JvnObject jvnObj = this.LocalsJvnObject.get(jon);
		if (jvnObj != null){
			return jvnObj;
		}
		try {
			return this.jvnRemoteCoord.jvnLookupObject(jon, this);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new JvnException("Error in Object Lookup");
		}
	}	

	/**
	 * Get a Read lock on a JVN object 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws  JvnException
	 **/
	public Serializable jvnLockRead(int joi) throws JvnException {
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
	public Serializable jvnLockWrite(int joi) throws JvnException {
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
	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException,jvn.JvnException {
		this.LocalsJvnObject.get(joi).jvnInvalidateReader();
	};

	/**
	 * Invalidate the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		Serializable o = this.LocalsJvnObject.get(joi).jvnInvalidateWriter();
		this.LocalsJvnObject.get(joi).setSerializableObject(o);
		return o;
	};

	/**
	 * Reduce the Write lock of the JVN object identified by id 
	 * @param joi : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException,jvn.JvnException { 
		Serializable o = this.LocalsJvnObject.get(joi).jvnInvalidateWriterForReader();
		this.LocalsJvnObject.get(joi).setSerializableObject(o);
		return o;
	};

}


