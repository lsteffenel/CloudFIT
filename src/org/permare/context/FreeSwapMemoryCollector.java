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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * collects information about free physical memory available.
 *
 * @author kirsch
 */
public class FreeSwapMemoryCollector extends AbstractOSCollector<Double> {

    public static String COLLECTOR_NAME = "Thing.Device.Memory.Swap.Available";
    public static String COLLECTOR_DESCR = "Free swap memory availanle (in Kb)";

    public FreeSwapMemoryCollector() {
        super.setName(COLLECTOR_NAME);
        super.setDescription(COLLECTOR_DESCR);
    }

    @Override
    public List<Double> collect() {
        List<Double> results = new ArrayList<>(1);

        if (this.getBean() instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean bean
                    = (com.sun.management.OperatingSystemMXBean) this.getBean();
            results.add(new Double(bean.getFreeSwapSpaceSize() / 1024));
        } else {
            Logger.getLogger(getClass().getName()).log(Level.INFO,
                    "No current physical memory information available, getting VM memory.");
        }

        return results;
    }

    @Override
    public boolean checkValue(Serializable value) {
        List<Double> mems = this.collect();
        if (!mems.isEmpty()) {
            Double VMmem = mems.get(mems.size() - 1);
            if ((Double) value <= VMmem) {
                return true;
            }
        }
        return false;
    }

}
