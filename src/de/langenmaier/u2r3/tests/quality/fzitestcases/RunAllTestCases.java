package de.langenmaier.u2r3.tests.quality.fzitestcases;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class RunAllTestCases {
	
	static Logger logger = Logger.getLogger(RunAllTestCases.class);
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		
		String folder = null; // = "file:///home/stefan/workspace/u2r3/ontologien/tests/fzi/owl2rl/";
		String name;
		
		File dir = new File("/home/stefan/.workspace/u2r2/ontologien/tests/fzi/owl2rl/");
	
		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		File[] files = dir.listFiles(fileFilter);
		
		for (File f : files) {
			name = f.getName();
			folder = f.getAbsolutePath();
			
			Properties prop = new Properties();
			prop.loadFromXML(new FileInputStream(folder + "/" + name + ".metadata.properties"));
			if (prop.getProperty("testcase.type").equals("POSITIVE_ENTAILMENT")) {
				RunTestCase.runTestCase(name, folder, CheckType.entailment_check);
			} else {
				RunTestCase.runTestCase(name, folder, CheckType.consistency_check);
			}	
		}
	}
	
}