package jvn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JvnObjectMap {


	protected Map<String, JvnObject> LocalsJvnObject;
	protected Map<Integer,String> assocMap;

	public JvnObjectMap() {
		this.LocalsJvnObject	= new ConcurrentHashMap<String, JvnObject>();
		this.assocMap			= new ConcurrentHashMap<Integer, String>();
	}

	public JvnObject get(int joi) {
		Shared.log("JvnObjectMap","get joi : "+ joi);
		Shared.log("JvnObjectMap","get assocMap : "+ this.assocMap.get(joi));
		Shared.log("JvnObjectMap","get assocMap : "+ this.assocMap.toString());
		Shared.log("JvnObjectMap","get LocalsJvnObject : "+ this.LocalsJvnObject.get(this.assocMap.get(joi)));
		return this.LocalsJvnObject.get(this.assocMap.get(joi));
	}

	public JvnObject get(String jon) {
		synchronized(jon.intern()){
			return this.LocalsJvnObject.get(jon);
		}
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
			}
			else {
				this.LocalsJvnObject.put(jon, jo);
				this.assocMap.put(jo.jvnGetObjectId(),jon);
			}
		}
	}

}
