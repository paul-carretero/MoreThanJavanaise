package jvn.jvnCoord.jvnLoadBalancer;

/**
 * @author Paul Carretero
 * Défini le rôle actuel d'un Coordinateur
 * Un SLAVE peut devenir MASTER (mais pas l'inverse)
 */
public enum JvnCoordCurrentRole {
	/**
	 * Coordinateur actuellement utilisé
	 * Se synchronize avec un unique SLAVE (si disponible)
	 * s'enregistre avec un nom sur le réseau
	 */
	MASTER,
	/**
	 * Coordinateur de Backup
	 * Est synchronizé avec par unique MASTER
	 * Le slave ne s'enregistre pas sur RMI en temps que coordinateur (mais en temps que slave)
	 * Il ne s'enregistrera que lorsqu'il passera master (avec le nom de son maitre)
	 */
	SLAVE
}
