/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author angelo
 */
public class PropertiesUtil {

    public static String getProperty(String property) {
        Properties prop = new Properties();
        InputStream input = null;
        String value = null;
        try {

            input = new FileInputStream("cloudfit.properties");

            // load a properties file
            prop.load(input);

            // get the property value and print it out
//            System.err.println(prop.getProperty("database"));
//            System.err.println(prop.getProperty("dbuser"));
//            System.err.println(prop.getProperty("dbpassword"));
            value = prop.getProperty(property);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }
        return value;
    }

}
