package jvn;

import java.util.HashMap;

public class JvnObjectMapServ extends JvnObjectMap {
	
	private static final int MAX_CACHE_SIZE = 10;

	public JvnObjectMapServ() {
		this.LocalsJvnObject	= new LRUHashMap<JvnObject>(MAX_CACHE_SIZE);
		this.assocMap			= new HashMap<Integer, String>();
	}

}
