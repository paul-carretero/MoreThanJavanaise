/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.io.*;

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
	public void jvnLockRead() throws jvn.JvnException; 

	/**
	 * Get a Write lock on the object 
	 * @throws JvnException
	 **/
	public void jvnLockWrite() throws jvn.JvnException; 

	/**
	 * Unlock  the object 
	 * @return true cas normal, false si verrou invalidé par le coordinateur
	 * @throws jvn.JvnException
	 */
	public boolean jvnUnLock() throws jvn.JvnException; 


	/**
	 * Get the object identification
	 **/
	public int jvnGetObjectId(); 

	/**
	 * Get the object state
	 * @throws JvnException
	 **/
	public Serializable jvnGetObjectState()	throws jvn.JvnException; 


	/**
	 * Invalidate the Read lock of the JVN object 
	 * @throws JvnException
	 **/
	public void jvnInvalidateReader() throws jvn.JvnException;

	/**
	 * Invalidate the Write lock of the JVN object  
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnInvalidateWriter()	throws jvn.JvnException;

	/**
	 * Reduce the Write lock of the JVN object 
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader() throws jvn.JvnException;	
	
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
	
	public boolean isFreeOfLock();
	
	public String getLockStatus();

	public void defaultLock();

	public void notifyWaitingReader(Serializable o);

	public void notifyWaitingWriter(Serializable o);
}
