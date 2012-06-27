package com.linuxbox.enkive.statistics.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractService {
	public static Map<String, Object> createMap() {
		return new HashMap<String, Object>();
	}

	public static Set<Map<String, Object>> createSet() {
		return new HashSet<Map<String, Object>>();
	}
}
