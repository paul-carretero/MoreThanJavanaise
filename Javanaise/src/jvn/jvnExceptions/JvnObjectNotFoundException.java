package jvn.jvnExceptions;

public class JvnObjectNotFoundException extends JvnException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5040142103568889147L;
  
	/**
	 * Instancie cet Exception avec un message par défault
	 */
	public JvnObjectNotFoundException() {
		super("JvnObjectNotFoundException");
	}
	
	/**
	 * Instancie cet Exception avec un message personalisé
	 * @param message un message décrivant le problème
	 */
	public JvnObjectNotFoundException(String message) {
		super(message);
	}	
}
