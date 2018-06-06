package org.permare.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FreeMemoryCollector extends AbstractOSCollector {

    public FreeMemoryCollector() {
        super.setName("Free_Memory");
        super.setDescription("Free physical memory and free swap space, VM free memory (in Kb)");
    }

    @Override
    public List<Double> collect() {
        List<Double> results = new ArrayList<>();

        if (this.getBean() instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean bean
                    = (com.sun.management.OperatingSystemMXBean) this.getBean();

            results.add(new Double(bean.getFreePhysicalMemorySize() / 1024));
            results.add(new Double(bean.getFreeSwapSpaceSize() / 1024));
        } else {
            Logger.getLogger(FreeMemoryCollector.class.getName()).log(Level.INFO,
                    "No current physical memory information available, getting VM memory.");
        }
        results.add(new Double(Runtime.getRuntime().freeMemory() / 1024)); // current free (based on current heap)
        results.add(new Double(Runtime.getRuntime().maxMemory() / 1024)); // max heap (Xmx)
        //results.add(new Double(Runtime.getRuntime().totalMemory()/1024)); // current heap max

        return results;
    }

    @Override
    public boolean checkValue(Serializable value) {
        List<Double> mems = this.collect();
        // use only VM memory. For other checks, please include more info
        Double VMmem = mems.get(mems.size() - 1);
        if ((Double) value <= VMmem) {
            return true;
        } else {
            return false;
        }
    }
}
