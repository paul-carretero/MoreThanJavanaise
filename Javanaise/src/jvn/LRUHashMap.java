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
	
	public JvnObject get(String key) {
		synchronized(key.intern()){
			JvnObject value = super.get(key);
			if(value != null) {
				TimedKey tk = new TimedKey(key);
				this.LRUQueue.remove(tk);
				this.LRUQueue.add(tk);
			}
			return value;
		}
	}
	
	@Override
	public JvnObject put(String key, JvnObject value ) {
		synchronized(key.intern()){
			super.put(key,value);
			TimedKey tk = new TimedKey(key);
			this.LRUQueue.remove(tk);
			this.LRUQueue.add(tk);
			
			while(this.size() > this.CacheSize) {
				String keyToRemove = this.LRUQueue.first().getKey();
				JvnObject toTest = this.get(keyToRemove);
				TimedKey tkremove = new TimedKey(keyToRemove);
				
				this.LRUQueue.remove(tkremove);
				if(toTest == null || !toTest.isFreeOfLock() ) {
					this.LRUQueue.add(tkremove);
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
}
