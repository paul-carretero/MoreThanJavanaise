package jvn.jvnObject;

/**
 * @author Paul Carretero
 * represente l'etat d'un verrou sur un objet Javanaise
 * Il s'agit du type de verrou sur cet objet que possède le serveur et non l'application (synchro applicatif nécessaire)
 */
public enum LockState{
	/**
	 * pas de verrou
	 */
	NOLOCK,
	/**
	 * pas de verrou en cours mais le serveur possède un verrou en lecture
	 */
	READCACHED,
	/**
	 * pas de verrou en cours mais le serveur possède un verrou en ecriture
	 */
	WRITECACHED,
	/**
	 * verrou en lecture
	 */
	READ,
	/**
	 * verrou en ecriture
	 */
	WRITE,
	/**
	 * verrou en lecture mais le serveur possède un verrou en ecriture
	 */
	WRITECACHEDREAD;
}
