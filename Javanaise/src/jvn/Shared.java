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
 * gestion des erreurs
 * supprimé les synchro inutile...
 * remplacer boolean return avec une exception si lock invalidé
 * récupérer la valeur des objets ayant un verrou en écriture sur le serveur lors de terminate
 * 
 * implémenter transaction:
 * begin transaction
 * -> aquisition des verrou au fur à mesure des demande sans relachement
 * end transaction : unlock des verrou utilisés
 * transaction monothreadée (aucune aquisition de verrou par d'autre thread)
 * 
 * multithreading serveur:
 * chaque thread peut demander des verrous de manière indépendante
 * le serveur tentera d'obtenir le meilleur verrou
 * 
 * gestion panne serveur:
 * prévoir rollback coordinateur sur exception rmi/timeout
 * clear serveur après
 * 
 * gestion panne coordinateur
 * aucune aquisition de verrou supplémentaire pour les serveurs
 * le serveur doit enregistré la map des objet sur txt
 * en cas de redémarrage impromptu recharge cette map
 * timeout des serveur si plus possible de contacter coord
 * 
 * coordinateur distribué en charge d'une partie des objets en fonction de leur id attribué par un coordinateur main
 * coordinateur redondant master/slave
 * synchro du slave par thread numéroté(par objet) de la part du master
 * en cas de fail du master les serveurs passent sur le slave
 * le slave devient master et tente de se connecter à un slave (nouveau basé sur le nom rmi)
 */