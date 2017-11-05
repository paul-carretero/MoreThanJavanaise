/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn.jvnObject;

import java.io.*;

import jvn.jvnExceptions.JvnConcurrentLockUpgradeException;
import jvn.jvnExceptions.JvnException;
import jvn.jvnExceptions.JvnPreemptiveInvalidationException;

/**
 * Interface of a JVN object. 
 * The serializable property is required in order to be able to transfer 
 * a reference to a JVN object remotely
 */

public interface JvnObject extends Serializable {

	/**
	 * Get a Read lock on the object 
	 * @throws JvnException
	 **/
	public void jvnLockRead() throws jvn.jvnExceptions.JvnException; 

	/**
	 * Get a Write lock on the object 
	 * @throws JvnException
	 * @throws JvnConcurrentLockUpgradeException 
	 **/
	public void jvnLockWrite() throws JvnException, JvnConcurrentLockUpgradeException; 

	/**
	 * Unlock  the object 
	 * @return true cas normal, false si verrou invalidé par le coordinateur
	 * @throws jvn.jvnExceptions.JvnException
	 * @throws JvnPreemptiveInvalidationException 
	 */
	public void jvnUnLock() throws JvnException, JvnPreemptiveInvalidationException; 


	/**
	 * Get the object identification
	 * @return l'id de l'objet
	 **/
	public int jvnGetObjectId(); 

	/**
	 * Get the object state
	 * @return l'objet applicatif encapsuler dans cet objet javanaise
	 * @throws JvnException
	 **/
	public Serializable jvnGetObjectState(); 


	/**
	 * Invalidate the Read lock of the JVN object 
	 * @throws JvnException
	 **/
	public void jvnInvalidateReader() throws jvn.jvnExceptions.JvnException;

	/**
	 * Invalidate the Write lock of the JVN object  
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnInvalidateWriter()	throws jvn.jvnExceptions.JvnException;

	/**
	 * Reduce the Write lock of the JVN object 
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader() throws jvn.jvnExceptions.JvnException;	
	
	/**
	 * met à jour un l'objet application encapsule dans un JvnObject
	 * @param o un objet serializable
	 */
	public void setSerializableObject(Serializable o);
	
	/**
	 * Invalide tous les verrou sur cet objet de manière préemptive \0/
	 * La valeur de l'objet sur le coordinateur est conservé
	 * @throws JvnException 
	 */
	public void jvnInvalidatePremptively() throws JvnException;
	
	/**
	 * vérifie si un objet est utilisé par une application (possède un verrou actif)
	 * @return vrai si l'objet n'a pas de verrou en lecture/ecriture (hors ceux en cache), faux sinon
	 */
	public boolean isFreeOfLock();
	
	/**
	 * Réveil les eventuelle thread en attente de verrou en lecture sur cet objet et met à jour la valeur de l'objet encapsulé
	 * @param o un objet serializable encapsuler dans ce JvnObject
	 */
	public void notifyWaiters(Serializable o);

	/**
	 * @return le type de verrou sur l'objet atuellement détenu localement
	 */
	public LockState jvngetLock();

}
