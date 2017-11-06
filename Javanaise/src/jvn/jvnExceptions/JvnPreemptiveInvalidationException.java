package jvn.jvnExceptions;

/**
 * @author Paul Carretero
 * non utilisé dans cette version.
 * permet de signaler que le coordinateur à invalider un verrou de manière préemptive 
 * (par exemple si le délai est trop grand)
 */
public class JvnPreemptiveInvalidationException extends JvnException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 64818977211721697L;

	/**
	 * constructeur par défault
	 */
	public JvnPreemptiveInvalidationException() {
		super("JvnPreemptiveInvalidationException");
	}

}
