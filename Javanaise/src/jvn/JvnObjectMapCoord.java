package jvn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JvnObjectMapCoord extends JvnObjectMap{

	private final Map<Integer,Set<JvnRemoteServer>> readingServer;
	private final Map<Integer,JvnRemoteServer> writingServer;

	public JvnObjectMapCoord() {
		super();
		this.readingServer		= new HashMap<Integer, Set<JvnRemoteServer>>();
		this.writingServer		= new HashMap<Integer, JvnRemoteServer>();
	}

	/**
	 * On a besoin de la synchronization car deux serveur peuvent enregistré un meme objet (même nom) en même temps
	 * @param jo
	 * @param jon
	 * @param js
	 * @throws JvnException
	 */
	@Override
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
				this.readingServer.put(jo.jvnGetObjectId(), new HashSet<JvnRemoteServer>());
				this.writingServer.put(jo.jvnGetObjectId(), js);
			}
		}
	}

	public Set<JvnRemoteServer> getReadingServer(int joi){
		return this.readingServer.get(joi);
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
		this.writingServer.put(joi, js);
	}

	public void cleanUpServer(JvnRemoteServer js) {
		for(Iterator<Map.Entry<Integer,JvnRemoteServer>> it = this.writingServer.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<Integer,JvnRemoteServer> entry = it.next();
			if(entry.getValue().equals(js)) {
				it.remove();
			}
		}
		
		for (Set<JvnRemoteServer> server : this.readingServer.values()) {
		    server.remove(js);
		}
	}
}
