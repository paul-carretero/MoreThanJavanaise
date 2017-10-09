package jvn;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class LRUHashMap extends ConcurrentHashMap<String,JvnObject> {
	
	private static final long serialVersionUID = 1L;
	private final SortedSet<TimedKey> LRUQueue;
	private final int CacheSize; 

	public LRUHashMap(int size) {
		super();
		this.LRUQueue 	= new TreeSet<TimedKey>();
		this.CacheSize 	= size;
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public JvnObject get(String key) {
		JvnObject value = super.get(key);
		if(value != null) {
			this.LRUQueue.remove(key);
			this.LRUQueue.add(new TimedKey(key));
		}
		return value;
	}
	
	@Override
	@SuppressWarnings("unlikely-arg-type")
	public JvnObject put(String key, JvnObject value ) {
		super.put(key,value);
		this.LRUQueue.remove(key);
		this.LRUQueue.add(new TimedKey(key));
		
		while(this.size() > this.CacheSize) {
			String keyToRemove = this.LRUQueue.first().getKey();
			JvnObject toTest = this.get(keyToRemove);
			this.LRUQueue.remove(keyToRemove);
			if(toTest == null || !toTest.isFreeOfLock() ) {
				this.LRUQueue.add(new TimedKey(keyToRemove));
			}
			else {
				int intKey = -1;
				try {
					intKey = toTest.jvnGetObjectId();
				} catch (JvnException e) {
					e.printStackTrace();
				}
				this.remove(keyToRemove);
				JvnServerImpl.jvnGetServer().invalideKey(intKey);
			}
		}
		return value;
	}

}
