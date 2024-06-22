package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading " + CONFIG_FILE, ex);
        }
    }

    public static String getUrlCompany() {
        return properties.getProperty("urlCompany");
    }

    public static String getUrlEmployee() {
        return properties.getProperty("urlEmployee");
    }

    public static String getUrlAuth() {
        return properties.getProperty("urlAuth");
    }

    public static String getConnectionString() {
        return properties.getProperty("connectionString");
    }

    public static String getUserDB() {
        return properties.getProperty("userDB");
    }

    public static String getPasswordDB() {
        return properties.getProperty("passwordDB");
    }

}
