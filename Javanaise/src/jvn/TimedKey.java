package jvn;

public class TimedKey implements Comparable<TimedKey>{

	private long timestamp;
	private final String key;

	public TimedKey(String key) {
		this.key = key;
		this.timestamp = System.currentTimeMillis();
	}

	public String getKey() {
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

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		return ((obj instanceof TimedKey) && ((TimedKey) obj).key.equals(this.key) );
	}
	
	@Override
	public java.lang.String toString() {
		return "[" + String.valueOf(this.timestamp) + "] : " + this.key;
	}
}