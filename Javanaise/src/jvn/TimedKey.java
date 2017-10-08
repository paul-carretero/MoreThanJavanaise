package jvn;

public class TimedKey implements Comparable<TimedKey>{

	private long timestamp;
	private final String key;

	protected TimedKey(String key) {
		this.key = key;
		this.timestamp = System.currentTimeMillis();
	}

	protected String getKey() {
		return this.key;
	}

	public long getTimeStamp() {
		return this.timestamp;
	}

	@Override
	public int compareTo(TimedKey o) {
		if(o.getTimeStamp() == this.timestamp) {
			return 0;
		}
		else if(o.getTimeStamp() > this.timestamp) {
			return 1; // this plus grand (ie plus vieux)
		}
		return -1; // this plus petit (ie plus récent)
	}


	@Override
	public int hashCode() {
		return this.key.hashCode();
	}

	/**
	 * \o/
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		return ((obj instanceof TimedKey) && ((TimedKey) obj).key.equals(this.key) ) || this.key.equals(obj);
	}
}