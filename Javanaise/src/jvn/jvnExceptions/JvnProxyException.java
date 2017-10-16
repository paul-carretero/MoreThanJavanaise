package jvn.jvnExceptions;

public class JvnProxyException extends Exception {

	private static final long serialVersionUID = -6719554479785446225L;
	
	/**
	 * message associé à l'Exception
	 */
	private final String message;
  
	/**
	 * Instancie cet Exception avec un message par défault
	 */
	public JvnProxyException() {
		super();
		this.message = "JvnProxyException";
	}
	
	/**
	 * Instancie cet Exception avec un message personalisé
	 * @param message un message décrivant le problème
	 */
	public JvnProxyException(String message) {
		this.message = message;
	}	
  
	@Override
	public String getMessage(){
		return this.message;
	}

}
