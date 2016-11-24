/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.core;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.permare.context.Collector;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
class ReqEvaluator {

    private List<Collector<Double>> collectors = new ArrayList<>();
    
    public ReqEvaluator(List<Collector<Double>> cols) {
        this.collectors = cols;
    }
    
    public boolean checkRequirements (Properties reqRessources) {
        Enumeration props = reqRessources.propertyNames();
        while (props.hasMoreElements())
        {
            String key = (String) props.nextElement();
            System.out.println(key + " = " + reqRessources.getProperty(key));
        }
        return true;
    }
}
