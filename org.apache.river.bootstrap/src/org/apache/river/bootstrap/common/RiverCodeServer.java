package org.apache.river.bootstrap.common;

import java.io.IOException;
import java.io.InputStream;

public interface RiverCodeServer extends RiverService{

	public String addJars(String id, InputStream stream, String targetFileName) throws IOException;

	public int getPort();

	public String getDirectory();

	public String getBaseLocation();

}