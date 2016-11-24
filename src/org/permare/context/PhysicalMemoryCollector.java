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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PhysicalMemoryCollector extends AbstractOSCollector {

    public PhysicalMemoryCollector() {
        super.setName("Physival Memory");
        super.setDescription("Total physical memory size and swap size, and VM total memory (in Kb)");
    }

    
	public List<Float> collect() {
            List<Float> results = new ArrayList<>();

        if (this.getBean() instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean bean =
                    (com.sun.management.OperatingSystemMXBean) this.getBean();

            results.add(new Float(bean.getTotalPhysicalMemorySize()/1024));
            results.add(new Float(bean.getTotalSwapSpaceSize()/1024));
        } else {
            Logger.getLogger(PhysicalMemoryCollector.class.getName()).log(Level.INFO,
                    "No current information about physical memory available, getting only the VM total memory");
        }

        results.add(new Float(Runtime.getRuntime().totalMemory()/1024));

        return results;
	}

}