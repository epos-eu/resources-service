package org.epos.api.utility;

import java.util.HashMap;

public class Paths {
	
	public static HashMap<String,String[]> paths = new HashMap<String, String[]>();
	static {
		paths.put("discovery", new String[]{"fetch", "frontend_return"});
		paths.put("workspace", new String[]{"workspace", "frontend_return"});
		paths.put("execute", new String[]{"fetch", "access", "frontend_return"});
		paths.put("execute_map", new String[]{"fetch", "access", "map", "frontend_return"});
	}

}
