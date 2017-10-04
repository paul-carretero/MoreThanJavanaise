package jvn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JvnObjectMap {


	private final Map<String, JvnObject> LocalsJvnObject;
	private final Map<Integer,String> assocMap;
	private final Map<Integer,Set<JvnRemoteServer>> readingServer;
	private final Map<Integer,JvnRemoteServer> writingServer;

	public JvnObjectMap() {
		this.LocalsJvnObject	= new HashMap<String, JvnObject>();
		this.assocMap			= new HashMap<Integer, String>();
		this.readingServer		= new HashMap<Integer, Set<JvnRemoteServer>>();
		this.writingServer		= new HashMap<Integer, JvnRemoteServer>();
	}

	public JvnObject get(int joi) {
		return this.LocalsJvnObject.get(this.assocMap.get(joi));
	}

	public JvnObject get(String jon) {
		return this.LocalsJvnObject.get(jon);
	}
	
	public void put(JvnObject jo, String jon, JvnRemoteServer js) throws JvnException {
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
}
