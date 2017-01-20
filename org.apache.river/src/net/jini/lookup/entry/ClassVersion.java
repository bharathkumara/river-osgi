package net.jini.lookup.entry;

import net.jini.entry.AbstractEntry;

public class ClassVersion extends AbstractEntry {

	private static final long serialVersionUID = -2615274449144270819L;
	public String className;
	public Integer major, minor, micro;
	public String qualifier;

	public ClassVersion() {

	}

	public ClassVersion(String className, Integer major, Integer minor, Integer micro, String qualifier) {
		this.className = className;
		this.major = major;
		this.minor = minor;
		this.micro = micro;
		this.qualifier = qualifier;

	}
}
