/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.Serializable;
import java.net.MalformedURLException;


public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord{

	private static final long 	serialVersionUID = 1L;
	private static final String HOST = "//localhost/";
	private final AtomicInteger currentOjectId;
	private final Map<Integer,AtomicInteger> waitingWriters;
	
	/**
	 * Ensemble Objets JVN stockés
	 */
	private JvnObjectMapCoord jvnObject;


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
		this.waitingWriters = new HashMap<Integer,AtomicInteger>();
		this.jvnObject 		= new JvnObjectMapCoord();
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
		this.jvnObject.put(jo, jon, js);
	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server 
	 * @param jon : the JVN object name
	 * @param js : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	@Override
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException,jvn.JvnException{
		return this.jvnObject.get(jon);
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
		AtomicInteger ww = this.waitingWriters.get(joi);
		if(ww != null && ww.get() > 1) {
			synchronized (ww) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(!this.jvnObject.getWritingServer(joi).equals(js)) {
			this.jvnObject.get(joi).setSerializableObject(this.jvnObject.getWritingServer(joi).jvnInvalidateWriterForReader(joi));
		}
		this.jvnObject.addReadingServer(joi, js);
		return this.jvnObject.get(joi).jvnGetObjectState();
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
		
		if(this.waitingWriters.get(joi) == null) {
			this.waitingWriters.put(joi, new AtomicInteger(1));
		}
		else {
			this.waitingWriters.get(joi).incrementAndGet();
		}
		
		
		if(!this.jvnObject.getWritingServer(joi).equals(js)) {
			this.jvnObject.get(joi).setSerializableObject(this.jvnObject.getWritingServer(joi).jvnInvalidateWriter(joi));
		}
		for(JvnRemoteServer server : this.jvnObject.getReadingServer(joi)) {
			if(!server.equals(js)) {
				server.jvnInvalidateReader(joi);
			}
		}
		this.jvnObject.setWritingServer(joi, js);
		
		synchronized (this.waitingWriters.get(joi)) {
			this.waitingWriters.get(joi).decrementAndGet();
			this.notifyAll();
		}
		
		return this.jvnObject.get(joi).jvnGetObjectState();
	}

	/**
	 * A JVN server terminates
	 * @param js  : the remote reference of the server
	 * @throws java.rmi.RemoteException, JvnException
	 **/
	@Override
	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		this.jvnObject.cleanUpServer(js);
	}

	//@Override
	public void invalidateKey(int key, JvnServerImpl js) {
		this.jvnObject.cleanUpKey(key,js);
	}
}


