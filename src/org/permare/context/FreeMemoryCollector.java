package org.permare.context;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FreeMemoryCollector extends AbstractOSCollector {

    public FreeMemoryCollector() {
        super.setName("Free Memory");
        super.setDescription("Free physical memory and free swap space, VM free memory (in Kb)");
    }

    @Override
    public List<Double> collect() {
        List<Double> results = new ArrayList<>();

        if (this.getBean() instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean bean =
                    (com.sun.management.OperatingSystemMXBean) this.getBean();

            results.add(new Double(bean.getFreePhysicalMemorySize()/1024));
            results.add(new Double(bean.getFreeSwapSpaceSize()/1024));
        } else {
            Logger.getLogger(FreeMemoryCollector.class.getName()).log(Level.INFO,
                    "No current physical memory information available, getting VM memory.");
        }

        results.add(new Double(Runtime.getRuntime().freeMemory()/1024));

        return results;
    }
}