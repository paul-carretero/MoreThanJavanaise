package jvn.jvnCoord.JvnLogicalCoord;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import jvn.JvnObjectMap;
import jvn.jvnExceptions.JvnException;
import jvn.jvnObject.JvnObject;
import jvn.jvnServer.JvnRemoteServer;

/**
 * @author Paul Carretero
 * représentation des objets géré par un coordinateur
 * Contient divers informations sur ceux si comme les serveurs disposant de verroux sûr ceux si
 */
public class JvnObjectMapCoord extends JvnObjectMap{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1440979092572164334L;
	
	/**
	 * liste des serveur ayant un verrou en lecture sur un objet (id=>liste de serveur)
	 */
	private final Map<Integer,List<JvnRemoteServer>> 	readingServer;
	
	/**
	 * liste des serveur ayant un verrou en ecriture sur un objet (id=>serveur)
	 */
	private final Map<Integer,JvnRemoteServer> 			writingServer;

	/**
	 * constructeur par défault
	 * initialise les map des verrou
	 */
	public JvnObjectMapCoord() {
		super();
		this.readingServer		= new ConcurrentHashMap<Integer, List<JvnRemoteServer>>();
		this.writingServer		= new ConcurrentHashMap<Integer, JvnRemoteServer>();
	}

	/**
	 * On a besoin de la synchronization car deux serveur peuvent enregistré un meme objet (même nom) en même temps
	 * @param jo
	 * @param jon
	 * @param js
	 * @throws JvnException
	 */
	public void put(JvnObject jo, String jon, JvnRemoteServer js) throws JvnException {
		synchronized(jon.intern()){
			if(this.assocMap.containsKey(jo.jvnGetObjectId())) {
				this.LocalsJvnObject.put(jon, jo);
				this.assocMap.put(jo.jvnGetObjectId(),jon);
				jo.jvnInvalidateWriter();
				this.writingServer.put(jo.jvnGetObjectId(), js);
			}
			else {
				this.LocalsJvnObject.put(jon, jo);
				this.assocMap.put(jo.jvnGetObjectId(),jon);
				this.readingServer.put(jo.jvnGetObjectId(), new CopyOnWriteArrayList<JvnRemoteServer>());
				this.writingServer.put(jo.jvnGetObjectId(), js);
			}
		}
	}

	/**
	 * @param joi un id d'objet JVN
	 * @return la liste des serveur ayant un verrou en lecture sur cet objet
	 */
	public List<JvnRemoteServer> getReadingServer(int joi){
		return this.readingServer.get(joi);
	}

	/**
	 * supprime la liste des serveur ayant un verrou en lecture sur cet objet
	 * @param joi un id d'objet JVN
	 */
	public void resetReadingServer(int joi) {
		this.readingServer.put(joi, new CopyOnWriteArrayList<JvnRemoteServer>());
	}

	/**
	 * @param joi un id d'objet JVN
	 * @return le serveur ayant un verrou en ecriture sur cet objet ou null si il n'y en a pas
	 */
	public JvnRemoteServer getWritingServer(int joi){
		return this.writingServer.get(joi);
	}

	/**
	 * Ajoute un serveur client à la liste des serveur ayant un verrou en lecture sur un objet
	 * @param joi un id d'objet JVN
	 * @param js un serveur client
	 */
	public void addReadingServer(int joi, JvnRemoteServer js ) {
		this.readingServer.get(joi).add(js);
	}

	/**
	 * supprime le serveur ayant l'id spécifié de la liste des serveur ayant un verrou en lecture pour cet objet
	 * @param joi un id d'objet JVN
	 * @param js un serveur client
	 */
	public void removeReadingServer(int joi, JvnRemoteServer js ) {
		this.readingServer.get(joi).remove(js);
	}

	/**
	 * défini un serveur ayant un verrou en ecriture pour un objet JVN
	 * @param joi un identifiant d'objet JVN
	 * @param js un serveur client
	 */
	public void setWritingServer(int joi, JvnRemoteServer js ) {
		if(js == null) {
			this.writingServer.remove(joi);
		}
		else {
			this.writingServer.put(joi, js);
		}
	}

	/**
	 * supprime un serveur client des listes
	 * @param js un serveur client
	 * @param tryUpdate true si l'on doit essayer de récupérer les valeurs du serveur (si verrou en ecriture), false sinon
	 */
	public void cleanUpServer(JvnRemoteServer js, boolean tryUpdate) {
		for(Iterator<Map.Entry<Integer,JvnRemoteServer>> it = this.writingServer.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Integer,JvnRemoteServer> entry = it.next();
			if(entry.getValue().equals(js)) {
				if(tryUpdate) {
					try {
						Serializable o = js.jvnInvalidateWriter(entry.getKey());
						if(o != null) {
							this.LocalsJvnObject.get(this.assocMap.get(entry.getKey())).setSerializableObject(o);
						}
					} catch (RemoteException | JvnException e) {
						System.out.println("erreur dans la finalisation d'un serveur" + e.getMessage());
					}
				}
				it.remove();
			}
		}

		for (List<JvnRemoteServer> server : this.readingServer.values()) {
			server.remove(js);
		}
	}

	/**
	 * invalide les verroux pour un objet JVN d'un serveur client
	 * @param joi un id d'objet JVN
	 * @param o un objet applicatif associé à cet id
	 * @param js un serveur client
	 */
	public void cleanUpKey(int joi, Serializable o, JvnRemoteServer js) {
		List<JvnRemoteServer> readingServerOnKey = this.readingServer.get(joi);
		if(readingServerOnKey != null) {
			readingServerOnKey.remove(js);
		}

		if(js.equals(this.writingServer.get(joi))) {
			this.get(joi).setSerializableObject(o);
			this.writingServer.remove(joi);
		}
	}
}
