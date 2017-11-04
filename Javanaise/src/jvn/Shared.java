package jvn;

/**
 * @author Paul Carretero
 * methodes static partagées entres les objets
 */
public class Shared {

	/**
	 * permet de loger des messages en provenance d'objet.
	 * centralize les demande de log
	 * @param what quel objet ecrit le log
	 * @param msg un message de log
	 */
	public static void log(final String what, final String msg) {
		System.out.println("["+what+"] : "+msg);
	}
}


/**
 * TODO
 * refactoring de jvn exception...
 * supprimé les synchros inutile...
 */