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

public class JvnObjectMapCoord extends JvnObjectMap{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1440979092572164334L;
	private final Map<Integer,List<JvnRemoteServer>> 	readingServer;
	private final Map<Integer,JvnRemoteServer> 			writingServer;

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

	public List<JvnRemoteServer> getReadingServer(int joi){
		return this.readingServer.get(joi);
	}

	public void resetReadingServer(int joi) {
		this.readingServer.put(joi, new CopyOnWriteArrayList<JvnRemoteServer>());
	}

	public JvnRemoteServer getWritingServer(int joi){
		return this.writingServer.get(joi);
	}

	public void addReadingServer(int joi, JvnRemoteServer js ) {
		this.readingServer.get(joi).add(js);
	}

	public void removeReadingServer(int joi, JvnRemoteServer js ) {
		this.readingServer.get(joi).remove(js);
	}

	public void setWritingServer(int joi, JvnRemoteServer js ) {
		if(js == null) {
			this.writingServer.remove(joi);
		}
		else {
			this.writingServer.put(joi, js);
		}
	}

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

	public void cleanUpKey(int joi, Serializable o, JvnRemoteServer js) {
		List<JvnRemoteServer> readingServerOnKey = this.readingServer.get(joi);
		if(readingServerOnKey != null) {
			readingServerOnKey.remove(js);
		}

		if(this.writingServer.get(joi) == js) {
			this.get(joi).setSerializableObject(o);
			this.writingServer.remove(joi);
		}
	}
}
