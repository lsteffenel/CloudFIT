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

//import com.sun.istack.internal.logging.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Process and System CPU load. 
 * it seems to read only VM CPU load. :( 
 * @author kirsch
 */
public class CPULoadCollector extends AbstractOSCollector {
    
    public CPULoadCollector() {
        super.setName("CPULoad");
        super.setDescription("Processor load, System load, System average.");
    }

    
    public List<Double> collect() {
        List<Double> results = new ArrayList<>();

        if (this.getBean() instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean bean = 
                    (com.sun.management.OperatingSystemMXBean) this.getBean();
            
            results.add(new Double(bean.getProcessCpuLoad()));
            results.add(new Double(bean.getSystemCpuLoad()));
        }
        else {
            Logger.getLogger(CPULoadCollector.class.getName()).log(Level.INFO, 
                    "No current Process and CPU load information available, getting the average.");
        }
        
        results.add(new Double(getBean().getSystemLoadAverage()));
        
        return results;
    }
}