package jvn;

import java.util.concurrent.ConcurrentHashMap;

public class JvnObjectMapServ extends JvnObjectMap {

	public JvnObjectMapServ() {
		this.LocalsJvnObject	= new ConcurrentLinkedHashMap();
		this.assocMap			= new ConcurrentHashMap<Integer, String>();
	}

}
