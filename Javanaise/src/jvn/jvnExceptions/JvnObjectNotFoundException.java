package jvn.jvnExceptions;

public class JvnObjectNotFoundException extends Exception {

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
	public JvnObjectNotFoundException() {
		super();
		this.message = "JvnObjectNotFoundException";
	}
	
	/**
	 * Instancie cet Exception avec un message personalisé
	 * @param message un message décrivant le problème
	 */
	public JvnObjectNotFoundException(String message) {
		super();
		this.message = message;
	}	
  
	@Override
	public String getMessage(){
		return "JvnObjectNotFoundException on : " + this.message;
	}

}
