package jvn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * replace String.Intern (trylock etc.)
 * @author carretero
 *
 */
public class LockStringPool {

	private static final Map<String,Lock> LockPool = new ConcurrentHashMap<String,Lock>();
	
	public static Lock get(String s) {
		if(LockPool.get(s) != null) {
			return LockPool.get(s);
		}
		synchronized(s.intern()) {
			LockPool.put(s, new ReentrantLock());
			return LockPool.get(s);
		}
	}

}
