package de.langenmaier.u2r3.util;

public class TableId {
	private static int id = 0;

	public static String getId() {
		++id;
		return "t"+id;
	}
	
	
}
