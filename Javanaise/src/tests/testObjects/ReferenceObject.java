package tests.testObjects;

import jvn.proxy.JvnReference;

public class ReferenceObject implements ReferenceObjectItf {
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -1386572913523854588L;

	@JvnReference(objectReference = "intRef", objectClass = IntObject.class)
	public transient IntObjectItf intRefField;
	
	@JvnReference(objectReference = "stringRef", objectClass = StringObject.class)
	public transient StringObjectItf stringRefField;
	
	private final int id;
	
	public ReferenceObject(int id) {
		this.id = id;
	}
	
	@Override
	public IntObjectItf getIntRef() {
		return this.intRefField;
	}

	@Override
	public StringObjectItf getStringRef() {
		return this.stringRefField;
	}

	@Override
	public int getId() {
		return this.id;
	}

	

	

}
