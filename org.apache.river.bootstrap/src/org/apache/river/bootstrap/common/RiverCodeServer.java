package org.apache.river.bootstrap.common;

import java.io.IOException;
import java.io.InputStream;

public interface RiverCodeServer extends RiverService{

	String addJars(String id, InputStream stream, String targetFileName) throws IOException;

	int getPort();

	String getDirectory();

	String getBaseLocation();

}