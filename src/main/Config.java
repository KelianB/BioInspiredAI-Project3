package main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Handles reading a properties file and parsing values.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class Config {
	private Properties properties;
	
	/**
	 * Create a blank config object
	 */
	public Config() {
		properties = new Properties();
	}
	
	/**
	 * Create a config object from a configuration file with a given name
	 * @param fileName - The name of the configuration file, e.g. config.properties
	 */
	public Config(String fileName) {
		this();
		try {
			this.parseConfigurationFile(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Unable to parse configuration file " + fileName);
			e.printStackTrace();
		}
	}
	
	/**
	 * Get a property with a given key
	 * @param key - A property key
	 * @return the property associated with the given key
	 */
	public String get(String key) {
		return properties.getProperty(key);
	}
	
	/**
	 * Get a float property with a given key
	 * @param key - A property key
	 * @return the property associated with the given key
	 */
	public float getFloat(String key) {
		return Float.parseFloat(get(key));
	}
	
	/**
	 * Get an int property with a given key
	 * @param key - A property key
	 * @return the property associated with the given key
	 */
	public int getInt(String key) {
		return Integer.parseInt(get(key));
	}
	
	/**
	 * Parses a configuration file with a given name
	 * @param fileName - The name of the configuration file, e.g. config.properties  
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void parseConfigurationFile(String fileName) throws FileNotFoundException, IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
		
		if(is == null)
			throw new FileNotFoundException("Unable to locate configuration file " + fileName);
		else
			properties.load(is);
	}
}
