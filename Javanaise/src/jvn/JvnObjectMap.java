package jvn;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jvn.jvnObject.JvnObject;

/**
 * @author Paul Carretero
 * Base pour la gestion d'une collection d'objet javanaise
 */
public abstract class JvnObjectMap implements Serializable {


	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7598625750182926514L;
	/**
	 * Map des objet Javanaise
	 */
	protected Map<String, JvnObject> LocalsJvnObject;
	/**
	 * Map associant un id d'objet à son nom
	 */
	protected Map<Integer,String> assocMap;

	/**
	 * Instancie une instance de map d'objet JvnObject.
	 * Fourni une methode get pour y acceder en fonction de leur nom ou de leur id
	 */
	public JvnObjectMap() {
		this.LocalsJvnObject	= new ConcurrentHashMap<String, JvnObject>();
		this.assocMap			= new ConcurrentHashMap<Integer, String>();
	}

	/**
	 * @param joi l'id de l'objet à rechercher
	 * @return l'objet associé à cet id ou null si il est absent
	 */
	public JvnObject get(final int joi) {
		return this.LocalsJvnObject.get(this.assocMap.get(joi));
	}

	/**
	 * @param jon le nom de l'objet à rechercher
	 * @return l'objet associé à ce nom ou null si il est absent
	 */
	public JvnObject get(final String jon) {
		return this.LocalsJvnObject.get(jon);
	}
}
