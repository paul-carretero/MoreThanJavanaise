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
	 * Demande au serveur la création d'une transaction pour le thread courrant
	 * @throws JvnTransactionException si une transaction est déjà en cours
	 */
	public void beginTransaction() throws JvnTransactionException;
	
	/**
	 * demande au serveur de valider la transaction pour le thread courrant
	 * @throws JvnPreemptiveInvalidationException
	 * @throws JvnException
	 * @throws JvnTransactionException si il n'y a pas de transaction à commiter
	 */
	public void commitTransaction() throws JvnPreemptiveInvalidationException, JvnException, JvnTransactionException;
	
	/**
	 * demande au serveur d'annuler la transaction pour le thread courrant
	 * @throws JvnPreemptiveInvalidationException
	 * @throws JvnException
	 * @throws JvnTransactionException si il n'y a pas de transaction à rollback
	 */
	public void rollbackTransaction() throws JvnPreemptiveInvalidationException, JvnException, JvnTransactionException;

	/**
	 * @return true si le thread courrant est dans une transaction, false sinon
	 */
	public boolean isInTransaction();

	/**
	 * @param jo un objet JVN à ajouter à la transaction avec un verrou en ecriture
	 * @throws JvnTransactionException
	 * @throws JvnException
	 */
	public void writeRegisterInTransaction(JvnObject jo) throws JvnTransactionException, JvnException;

	/**
	 * @param jo un objet JVN à ajouter à la transaction avec un verrou en lecture
	 * @throws JvnTransactionException
	 */
	public void readRegisterInTransaction(JvnObject jo) throws JvnTransactionException;
}


