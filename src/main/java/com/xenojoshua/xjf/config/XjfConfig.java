package com.xenojoshua.xjf.config;

import com.xenojoshua.xjf.config.handlers.XjfConfigJsonHandler;
import com.xenojoshua.xjf.config.handlers.XjfConfigPropertiesHandler;
import com.xenojoshua.xjf.config.interfaces.XjfConfigHandlerInterface;
import com.xenojoshua.xjf.constant.XjfConst;
import com.xenojoshua.xjf.log.XjfLogger;
import com.xenojoshua.xjf.util.XjfFile;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Properties;

public class XjfConfig {

    private static XjfConfig instance = null;

    private HashMap<String, XjfConfigPropertiesHandler> propertyHandlers;
    private HashMap<String, XjfConfigJsonHandler> jsonHandlers;

    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    //-* FACTORY FUNCTIONS
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    /**
     * Initialize XjfConfig.
     */
    public static void init() {
        XjfConfig.instance = new XjfConfig();
    }

    /**
     * Get instance of XjfConfig.
     * @return instance
     */
    public static XjfConfig get() {
        return XjfConfig.instance;
    }

    /**
     * Initialize XjfConfig HashMaps.
     */
    public XjfConfig() {
        this.propertyHandlers = new HashMap<String, XjfConfigPropertiesHandler>();
        this.jsonHandlers = new HashMap<String, XjfConfigJsonHandler>();
    }

    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    //-* APIs
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    /**
     * Get config file handler. Default type is 'json'.
     * @param fileName
     * @return handler
     */
    public XjfConfigHandlerInterface getHandler(String fileName) {
        return getHandler(fileName, XjfConst.XJF_CONFIG_TYPE_JSON);
    }

    /**
     * Get config file handler.
     * @param fileName
     * @param type
     * @return handler
     */
    public XjfConfigHandlerInterface getHandler(String fileName, String type) {
        XjfConfigHandlerInterface handler = null;

        if (type.equals(XjfConst.XJF_CONFIG_TYPE_JSON)) {
            if (!jsonHandlers.containsKey(fileName)) {
                loadConfigFile(fileName, type);
            }
            handler = jsonHandlers.get(fileName);
        } else if (type.equals(XjfConst.XJF_CONFIG_TYPE_PROPERTIES)) {
            if (!propertyHandlers.containsKey(fileName)) {
                loadConfigFile(fileName, type);
            }
            handler = propertyHandlers.get(fileName);
        } else {
            XjfLogger.get().error(
                String.format(
                    "[Xjf System] Invalid config file type, fileName: %s, type: %s",
                        fileName,
                        type
                )
            );
        }

        return handler;
    }

    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    //-* RESOURCE LOADER FUNCTIONS
    //-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    /**
     * Load config file content into String.
     * @param fileName
     * @param type
     */
    private void loadConfigFile(String fileName, String type) {
        String filePath = String.format("%s/%s.%s", XjfConst.XJF_CONF_ROOT, fileName, type);

        if (type.equals(XjfConst.XJF_CONFIG_TYPE_JSON)) {
            propertyHandlers.put(
                fileName,
                    new XjfConfigPropertiesHandler(
                        parseProperties(XjfFile.readWholeFile(filePath))
                    )
            );
        } else if (type.equals(XjfConst.XJF_CONFIG_TYPE_PROPERTIES)) {
            jsonHandlers.put(
                fileName,
                    new XjfConfigJsonHandler(
                        parseJson(XjfFile.readWholeFile(filePath))
                    )
            );
        } else {
            XjfLogger.get().error(
                String.format(
                    "[Xjf System] Invalid config file type, fileName: %s, type: %s",
                        fileName,
                        type
                )
            );
        }
    }

    /**
     * Load config file content into Properties.
     * @param configContent String of config file content
     * @return properties
     */
    private Properties parseProperties(String configContent) {
        Properties properties = new Properties();

        try {
            properties.load(new StringReader(configContent));
        } catch (IOException ioe) {
            XjfLogger.get().error(
                String.format(
                    "[Xjf System] Error in parsing properties: \n%s",
                        ExceptionUtils.getStackTrace(ioe)
                )
            );
        }

        return properties;
    }

    /**
     * Load config file content into JSONObject.
     * @param configContent String of config file content
     * @return json
     */
    private JSONObject parseJson(String configContent) {
        return (JSONObject) JSONSerializer.toJSON(configContent);
    }
}