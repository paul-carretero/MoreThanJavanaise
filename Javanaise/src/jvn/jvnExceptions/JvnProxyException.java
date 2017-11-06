package jvn.jvnExceptions;

/**
 * @author Paul Carretero
 * signale une exception lié au proxy (par exemple un problème d'initialisation)
 */
public class JvnProxyException extends JvnException {

	/**
	 * serialVersionUID
	 */
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
