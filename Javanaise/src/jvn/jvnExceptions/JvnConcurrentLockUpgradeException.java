package jvn.jvnExceptions;

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
