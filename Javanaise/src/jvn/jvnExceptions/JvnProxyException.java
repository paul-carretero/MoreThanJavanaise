package jvn.jvnExceptions;

public class JvnProxyException extends JvnException {

	private static final long serialVersionUID = -6719554479785446225L;
  
	/**
	 * Instancie cet Exception avec un message par défault
	 */
	public JvnProxyException() {
		super("JvnProxyException");
	}
	
	/**
	 * Instancie cet Exception avec un message personalisé
	 * @param message un message décrivant le problème
	 */
	public JvnProxyException(String message) {
		super(message);
	}

}
