package jvn.jvnServer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jvn.jvnObject.JvnObject;

/*
        __
    ___( o)>  <(MEOW)
    \ <_. )
     `---'
*/

/**
 * @author Paul Carretero
 * Classe permettant de gérer une Map de taille maximum avec une politique d'éviction de type LRU
 * Gère "théoriquement" mieux les accès concurent 
 */
public class ConcurrentLinkedHashMap extends LinkedHashMap<String,JvnObject> {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID 	= -6564548684418105003L;
	
	/**
	 * Nombre maximum d'entrée dans cette Map
	 */
	private static final int  MAX_ENTRIES		= 100;
	
	/**
	 * verrou protégeant les modification de structure concurrente de la map (LinkedHashMap n'étant pas thread safe)
	 */
	private final ReentrantReadWriteLock mapLock;

	/**
	 * initialise la linkedHashMap avec ordonnancement en fonction de l'access et non de l'insertion
	 * initialise un verrou en lecture/ecriture afin de garantir l'atomicité des operation get/put
	 */
	public ConcurrentLinkedHashMap() {
		super(16,0.75f,true); // ordered by access!!!
		this.mapLock = new ReentrantReadWriteLock(true);
	}

	@Override
	public JvnObject get(Object jon) {
		this.mapLock.readLock().lock();
		JvnObject res = super.get(jon);
		this.mapLock.readLock().unlock();
		return res;
	}

	@Override
	public JvnObject put(String jon, JvnObject jo ) {
		this.mapLock.writeLock().lock();
		JvnObject res = super.put(jon,jo);
		this.mapLock.writeLock().unlock();
		return res;
	}
	

	/**
	 * Appelé par put, on possède un lockwrite
	 * Si aucun objet n'est libre => boucle jusqu'il y en ai un de libre.
	 */
	@Override
	protected boolean removeEldestEntry(Map.Entry<String,JvnObject> eldest){
		if(this.size() > MAX_ENTRIES){
			if(eldest.getValue().isFreeOfLock()){
				JvnServerImpl.jvnGetServer().invalideKey(eldest.getValue().jvnGetObjectId(), eldest.getValue().jvnGetObjectState());
				return true;
			}
			this.remove(eldest.getKey());
			this.put(eldest.getKey(), eldest.getValue());
		}
		return false;
	}
}
