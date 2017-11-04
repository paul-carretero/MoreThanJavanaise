/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn.jvnServer;
import java.io.Serializable;

import jvn.jvnExceptions.JvnException;
import jvn.jvnExceptions.JvnPreemptiveInvalidationException;
import jvn.jvnExceptions.JvnTransactionException;
import jvn.jvnObject.JvnObject;

/**
 * Local interface of a JVN server  (used by the applications).
 * An application can get the reference of a JVN server through the static
 * method jvnGetServer().
 * @see JvnServerImpl
 */

public interface JvnLocalServer {

	/**
	 * create of a JVN object
	 * @param jos : the JVN object state
	 * @return the JVN object 
	 * @throws JvnException
	 **/
	public  JvnObject jvnCreateObject(Serializable jos) throws jvn.jvnExceptions.JvnException ; 

	/**
	 * Associate a symbolic name with a JVN object
	 * @param jon : the JVN object name
	 * @param jo : the JVN object 
	 * @throws JvnException
	 **/
	public  void jvnRegisterObject(String jon, JvnObject jo) throws jvn.jvnExceptions.JvnException; 

	/**
	 * Get the reference of a JVN object associated to a symbolic name
	 * @param jon : the JVN object symbolic name
	 * @return the JVN object 
	 * @throws JvnException
	 **/
	public  JvnObject jvnLookupObject(String jon) throws jvn.jvnExceptions.JvnException ; 


	/**
	 * Get a Read lock on a JVN object 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws  JvnException
	 **/
	public Serializable jvnLockRead(int joi) throws JvnException;

	/**
	 * Get a Write lock on a JVN object 
	 * @param joi : the JVN object identification
	 * @return the current JVN object state
	 * @throws  JvnException
	 **/
	public Serializable jvnLockWrite(int joi) throws JvnException;


	/**
	 * The JVN service is not used anymore by the application
	 * @throws JvnException
	 **/
	public void jvnTerminate() throws jvn.jvnExceptions.JvnException; 
	
	/**
	 * principalement utile pour les tests pour réinitialiser les serveur
	 * @param hard vrai si l'on doit également réinitialiser le coordinateur
	 * @throws JvnException 
	 */
	public void clearCache(boolean hard) throws JvnException;
	
	public void beginTransaction() throws JvnTransactionException;
	
	public void commitTransaction() throws JvnPreemptiveInvalidationException, JvnException, JvnTransactionException;
	
	public void rollbackTransaction() throws JvnPreemptiveInvalidationException, JvnException, JvnTransactionException;

	public boolean isInTransaction();

	public void writeRegisterInTransaction(JvnObject jvnObject) throws JvnTransactionException, JvnException;

	public void readRegisterInTransaction(JvnObject jo) throws JvnTransactionException;
}


