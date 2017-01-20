package org.apache.river.bootstrap.service.smart;

import java.util.List;

public class SmartServiceDescriptor {

	private String id;
	private String name;
	private String description;
	private List<String> jars;
	private List<String> codebase;
	private String serviceImplClass;
	private long parentBundleID;

	public SmartServiceDescriptor(String id, String name,  List<String> jars, List<String> codebase,
			String serviceImplClass, long parentBundleID) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.jars = jars;
		this.codebase = codebase;
		this.serviceImplClass = serviceImplClass;
		this.parentBundleID = parentBundleID;

	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<String> getJars() {
		return jars;
	}

	public List<String> getCodebase() {
		return codebase;
	}

	public String getServiceImplClass() {
		return serviceImplClass;
	}

	public long getParentBundleID() {
		return parentBundleID;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setJars(List<String> jars) {
		this.jars = jars;
	}

	public void setCodebase(List<String> codebase) {
		this.codebase = codebase;
	}

	public void setServiceImplClass(String serviceImplClass) {
		this.serviceImplClass = serviceImplClass;
	}

	public void setParentBundleID(long parentBundleID) {
		this.parentBundleID = parentBundleID;
	}
	
	

}
