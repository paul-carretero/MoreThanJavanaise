package jvn.jvnExceptions;

/**
 * @author Paul Carretero
 * Toutes les methods en LockAsked=Write devrait throw cette exception
 * Levée par un serveur lorsque deux serveurs tentent de conserver leur verrou en lecture et d'obtenir un verrou en ecriture sur un objet
 * 
 */
public class JvnConcurrentLockUpgradeException extends JvnException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -9197882963464683413L;

	public JvnConcurrentLockUpgradeException() {
		super();
		this.message = "JvnConcurrentLockUpgradeException";
	}

}
