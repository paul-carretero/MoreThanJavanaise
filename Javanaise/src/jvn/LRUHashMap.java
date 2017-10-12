package jvn;

import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class LRUHashMap extends ConcurrentHashMap<String,JvnObject> {
	
	private static final long serialVersionUID 	= -6564548684418105003L;
	private static final int  MAX_CACHE_SIZE 	= 5;
	private final NavigableSet<TimedKey> LRUQueue;

	public LRUHashMap() {
		super();
		this.LRUQueue 	= new ConcurrentSkipListSet<TimedKey>();
	}
	
	public JvnObject get(String jon) {
		synchronized(jon.intern()){
			JvnObject value = super.get(jon);
			if(value != null) {
				TimedKey tk = new TimedKey(jon);
				this.LRUQueue.remove(tk);
				this.LRUQueue.add(tk);
			}
			return value;
		}
	}
	
/*
        __
    ___( o)>  <(MEOW)
    \ <_. )
     `---'
 
*/
	private void debug() {
		System.out.println();
		for(TimedKey tk : this.LRUQueue) {
			System.out.print(tk + " ; ");
		}
		System.out.println();
	}
	
	@Override
	public JvnObject put(String jon, JvnObject jo ) {
		synchronized(jon.intern()){
			super.put(jon,jo);
			TimedKey tk = new TimedKey(jon);
			this.LRUQueue.remove(tk);
			this.LRUQueue.add(tk);
			int mapSize = this.size();
			int i = 0;
			debug();
			while(this.size() > MAX_CACHE_SIZE && i < mapSize) {
				i++;
				System.out.println("<1>");
				String keyToRemove	= this.LRUQueue.pollFirst().getKey();
				JvnObject toTest 	= this.get(keyToRemove);
				System.out.println("<2>");
				if(toTest == null){
					System.err.println("warning : null value in hashmap associated with " + keyToRemove);
				}
				else if(!toTest.isFreeOfLock()){
					System.out.println(toTest.toString());
					this.LRUQueue.add(new TimedKey(keyToRemove));
				}
				else {
					int intKey = toTest.jvnGetObjectId();
					System.out.println("<3>");
					JvnServerImpl.jvnGetServer().invalideKey(intKey);
					System.out.println("<4>");
					this.remove(keyToRemove);
				}
			}
			return jo;
		}
	}
}
