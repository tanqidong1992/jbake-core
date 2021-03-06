package org.jbake.app.configuration;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.jbake.app.JBakeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Provides Configuration related functions.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class ConfigUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);
    private static final String LEGACY_CONFIG_FILE = "custom.properties";
    private static final String CONFIG_FILE = "jbake.properties";
    private static final String DEFAULT_CONFIG_FILE = "default.properties";

    private CompositeConfiguration load(File source) throws ConfigurationException {

        if (!source.exists()) {
            throw new JBakeException("The given source folder '" + source.getAbsolutePath() + "' does not exist.");
        }
        if (!source.isDirectory()) {
            throw new JBakeException("The given source folder is not a directory.");
        }

        CompositeConfiguration config = new CompositeConfiguration();
        config.setListDelimiter(',');
        File customConfigFile = new File(source, LEGACY_CONFIG_FILE);
        if (customConfigFile.exists()) {
            displayLegacyConfigFileWarningIfRequired();
            PropertiesConfiguration pc=new PropertiesConfiguration();
            pc.setEncoding("utf-8");
            pc.setFile(customConfigFile);
            pc.load();
            config.addConfiguration(pc);
        }
        customConfigFile = new File(source, CONFIG_FILE);
        if (customConfigFile.exists()) {
        	PropertiesConfiguration pc=new PropertiesConfiguration();
            pc.setEncoding("utf-8");
            pc.setFile(customConfigFile);
            pc.load();
            config.addConfiguration(pc);
        }
        URL defaultPropertiesLocation = this.getClass().getClassLoader().getResource(DEFAULT_CONFIG_FILE);
        
        PropertiesConfiguration pc=new PropertiesConfiguration();
        pc.setEncoding("utf-8");
        pc.setURL(defaultPropertiesLocation);
        pc.load();
        config.addConfiguration(pc);
        config.addConfiguration(new SystemConfiguration());
        return config;
    }

    private void displayLegacyConfigFileWarningIfRequired() {
        LOGGER.warn("You have defined a part of your JBake configuration in {}", LEGACY_CONFIG_FILE);
        LOGGER.warn("Usage of this file is being deprecated, please rename this file to: {} to remove this warning", CONFIG_FILE);
    }

    public JBakeConfiguration loadConfig(File source) throws ConfigurationException {
        CompositeConfiguration configuration = load(source);
        return new DefaultJBakeConfiguration(source, configuration);
    }

}
