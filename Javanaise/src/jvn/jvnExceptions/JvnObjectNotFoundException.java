package jvn.jvnExceptions;

/**
 * @author Paul Carretero
 * Excpetion levée lorsqu'un objet n'est pas trouvé par un coordinateur (nom d'objet absent de la base)
 */
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
