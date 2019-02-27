package org.tms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfiguration {
    final Logger log = LoggerFactory.getLogger(AppConfiguration.class);
    public static String APP_VERSION = "";
    public static String APP_ID = "";
    public static String REST_API_KEY = "";
    public static String IRREVOCABLE_SESSION = "";
    public static String URL_FILE = "";
    public static String URL_CLASSES = "";
    public static String URL_BASE = "";

    public AppConfiguration() throws IOException {
        log.info("loading app config...");

        InputStream appConfigPath = ClassLoader.getSystemClassLoader().getResourceAsStream("application.properties");
//        String appConfigPath = rootPath + "app.properties";
//        String catalogConfigPath = rootPath + "catalog";

        Properties appProps = new Properties();
        appProps.load(appConfigPath);

//        Properties catalogProps = new Properties();
//        catalogProps.load(new FileInputStream(catalogConfigPath));

        this.APP_VERSION = appProps.getProperty("app.version");
        this.APP_ID = appProps.getProperty("app.cloud.appId");
        this.REST_API_KEY = appProps.getProperty("app.cloud.restApiKey");
        this.IRREVOCABLE_SESSION = appProps.getProperty("app.cloud.irrevocableSession");
        this.URL_FILE = appProps.getProperty("app.cloud.urlFile");
        this.URL_CLASSES = appProps.getProperty("app.cloud.urlClasses");
        this.URL_BASE = appProps.getProperty("app.cloud.urlBase");

        log.info("loading app config done");
    }
}
