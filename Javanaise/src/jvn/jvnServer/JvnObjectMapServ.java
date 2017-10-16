package jvn.jvnServer;

import java.util.concurrent.ConcurrentHashMap;

import jvn.JvnObjectMap;
import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;

/**
 * @author Paul Carretero
 * Cache des objet pour une utilisation local (cache serveur)
 * Basé sur une ConcurrentHashMap et une map associative pour associer rapidement un id d'ojet à sa chaine nominative
 * @see ConcurrentHashMap
 */
public class JvnObjectMapServ extends JvnObjectMap {

	/**
	 * intialise le cache serveur
	 * Utilisation d'une ConcurrentLinkedHashMap pour la gestion du cache des objet (LRU eviction)
	 */
	public JvnObjectMapServ() {
		this.assocMap			= new ConcurrentHashMap<Integer, String>();
		this.LocalsJvnObject	= new ConcurrentLinkedHashMap();
	}

	/**
	 * On a besoin de la synchronization car deux serveur peuvent enregistré un meme objet (même nom) en même temps
	 * @param jo un objet JvnObject
	 * @param jon le nom de l'objet
	 * @throws JvnException si une objet de même nom existe déjà
	 */
	public void put(JvnObject jo, String jon) throws JvnException {
		synchronized(jon.intern()){
			if(this.assocMap.containsKey(jo.jvnGetObjectId())) {
				System.out.println(this.assocMap);
				throw new JvnException("Impossible d'ajouter l'objet en cache : clé dupliquée");
			}
			this.LocalsJvnObject.put(jon, jo);
			this.assocMap.put(jo.jvnGetObjectId(),jon);
		}
	}
	
	/**
	 * retire une clé de la map associative
	 * @see ConcurrentLinkedHashMap
	 * @param joi l'id d'une clé d'une JvnObject
	 */
	public void removeFromAssocMap(final int joi) {
		this.assocMap.remove(joi);
	}
}
