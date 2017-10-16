/***
 * JAVANAISE API
 * Contact: 
 *
 * Authors: 
 */

package jvn.jvnExceptions;

/**
 * Interface of a JVN Exception. 
 */
public class JvnException extends Exception {
	

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5040142103568889147L;
	/**
	 * message associé à l'Exception
	 */
	private final String message;
  
	/**
	 * Instancie cet Exception avec un message par défault
	 */
	public JvnException() {
		super();
		this.message = "JvnException";
	}
	
	/**
	 * Instancie cet Exception avec un message personalisé
	 * @param message un message décrivant le problème
	 */
	public JvnException(String message) {
		this.message = message;
	}	
  
	@Override
	public String getMessage(){
		return this.message;
	}
}
