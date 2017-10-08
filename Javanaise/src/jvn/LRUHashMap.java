package jvn;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class LRUHashMap<V> extends ConcurrentHashMap<String,V> {
	
	private static final long serialVersionUID = 1L;
	private final SortedSet<TimedKey> LRUQueue;
	private final int CacheSize;

	public LRUHashMap(int size) {
		super();
		this.LRUQueue 	= new TreeSet<TimedKey>();
		this.CacheSize 	= size;
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public V get(String key) {
		System.out.println("lol?");
		V value = super.get(key);
		if(value != null) {
			this.LRUQueue.remove(key);
			this.LRUQueue.add(new TimedKey(key));
		}
		return value;
	}
	
	@Override
	@SuppressWarnings("unlikely-arg-type")
	public V put(String key, V value ) {
		super.put(key,value);
		System.out.println("lolV2?");
		this.LRUQueue.remove(key);
		this.LRUQueue.add(new TimedKey(key));
		
		if(this.CacheSize < this.size()) {
			String keyToRemove = this.LRUQueue.first().getKey();
			this.remove(keyToRemove);
			this.LRUQueue.remove(keyToRemove);
		}
		return value;
	}

}
