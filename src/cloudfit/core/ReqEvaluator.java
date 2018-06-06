/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.core;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import org.permare.context.Collector;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
class ReqEvaluator {

    private HashMap<String, Collector<Double>> collectors = new HashMap<>();

    public ReqEvaluator(HashMap<String, Collector<Double>> cols) {
        this.collectors = cols;
    }

    public boolean checkRequirements(Properties reqRessources) {
        boolean checkPassed = true;
        Enumeration props = reqRessources.propertyNames();
        while (props.hasMoreElements()) {
            String key = (String) props.nextElement();

            if (collectors.containsKey(key)) {
                Double value = Double.valueOf(reqRessources.getProperty(key));
                boolean test = collectors.get(key).checkValue(value);
                //System.out.println(key + " = " + reqRessources.getProperty(key) + " (" + test + ")");
                if (test == false) {
                    checkPassed = false;
                }
            }

        }
        return checkPassed;
    }
}
