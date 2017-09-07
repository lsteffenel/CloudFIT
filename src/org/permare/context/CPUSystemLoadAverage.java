/* *************************************************************** *
 * PER-MARE Project (project number 13STIC07)
 * http://cosy.univ-reims.fr/~lsteffenel/per-mare
 * A CAPES/MAEE/ANII STIC-AmSud collaboration program.
 * All rigths reserved to project partners:
 *  - Universite de Reims Champagne-Ardenne, Reims, France 
 *  - Universite Paris 1 Pantheon Sorbonne, Paris, France
 *  - Universidade Federal de Santa Maria, Santa Maria, Brazil
 *  - Universidad de la Republica, Montevideo, Uruguay
 * 
 * *************************************************************** *
 */
package org.permare.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects the CPU System load. 
 * Roughly, system load indicates how many processes are in the execution queue. 
 * If this number is less than the number of available CPU cores, that means all  
 * process are able to run. Otherwise, there are #Load-#Cores processes waiting
 * for execution. 
 * @see http://en.wikipedia.org/wiki/Load_(computing)
 * @author kirsch
 */
public class CPUSystemLoadAverage extends AbstractOSCollector<Double>  {
    
    public static String COLLECTOR_NAME = "Thing.Device.CPU.System.Load.Average";
    public static String COLLECTOR_DESCR = "System load average.";

    public CPUSystemLoadAverage() {
        super.setName(COLLECTOR_NAME);
        super.setDescription(COLLECTOR_DESCR);
    }

    
    @Override
    public List<Double> collect() {
        List<Double> results = new ArrayList<>(1);
        results.add(new Double(getBean().getSystemLoadAverage()));
        return results;
    }
    
    @Override
    public boolean checkValue(Serializable value) {
        List<Double> loads = this.collect();
        // use only global load. For other checks, please include more info
        if (!loads.isEmpty()) {
            Double load = loads.get(loads.size() - 1);
            if ((Double) value >= load) {
                return true;
            }
        }
        return false;
    }
    
}
