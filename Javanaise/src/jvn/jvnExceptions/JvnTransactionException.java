package jvn.jvnExceptions;

/**
 * @author Paul Carretero
 * Exception levé lorsqu'une erreur avec une transaction est rencontrée
 */
public class JvnTransactionException extends JvnException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 3520591689366148892L;
  
	/**
	 * Instancie cet Exception avec un message par défault
	 */
	public JvnTransactionException() {
		super("JvnTransactionException");
	}
	
	/**
	 * Instancie cet Exception avec un message personalisé
	 * @param message un message décrivant le problème
	 */
	public JvnTransactionException(String message) {
		super(message);
	}
	
}
