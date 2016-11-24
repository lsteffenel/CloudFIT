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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public abstract class AbstractOSCollector implements Collector {

    private String name;
    private String description;
    private OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCollectorName() {
        return this.name;
    }
    
    public String getCollectorDescription() {
        return this.description;
    }

    public OperatingSystemMXBean getBean() {
        return this.bean;
    }

    public void setBean(OperatingSystemMXBean bean) {
        this.bean = bean;
    }
}