package de.langenmaier.u2r3;

public class Settings {
	private static boolean startClean = true;
	
	public static void startClean(boolean sc) {
		startClean = sc;
	}
	
	public static boolean startClean() {
		return startClean;
	}
}
