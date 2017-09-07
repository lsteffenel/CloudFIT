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
 * Collects CPU system load. This value seems to be percentual of usage (user +
 * system) observed by the VM.
 * <b>Note:</b> This value use to be 0 in its first lecture (or at begining of
 * the process execution).
 *
 * @author kirsch
 */
public class CPUSystemLoad extends AbstractOSCollector<Double> {

    public static String COLLECTOR_NAME = "Thing.Device.CPU.System.Load";
    public static String COLLECTOR_DESCR = "System percentual load.";

    public CPUSystemLoad() {
        super.setName(COLLECTOR_NAME);
        super.setDescription(COLLECTOR_DESCR);
    }

    @Override
    public List<Double> collect() {
        List<Double> results = new ArrayList<>(1);

        if (this.getBean() instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean bean
                    = (com.sun.management.OperatingSystemMXBean) this.getBean();
            results.add(bean.getSystemCpuLoad());
        } else {
            Logger.getLogger(getClass().getName()).log(Level.INFO,
                    "No current CPU load information available.");
        }

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
