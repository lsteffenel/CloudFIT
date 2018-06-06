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
 * collects information about the free memory on the Java VM.
 *
 * @author kirsch
 */
public class TotalVMMemoryCollector extends AbstractCollector<Double> {

    public static String COLLECTOR_NAME = "Thing.VM.Memory.Total";
    public static String COLLECTOR_DESCR = "VM max heap memory (in Kb)";

    public TotalVMMemoryCollector() {
        super.setName(COLLECTOR_NAME);
        super.setDescription(COLLECTOR_DESCR);
    }

    @Override
    public List<Double> collect() {
        List<Double> results = new ArrayList<>(1);
        results.add(new Double(Runtime.getRuntime().maxMemory() / 1024));
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
