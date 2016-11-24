/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.core;

import cloudfit.util.PropertiesUtil;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.permare.context.AvailableDiskSpaceCollector;
import org.permare.context.Collector;
import org.permare.context.FreeMemoryCollector;
import org.permare.context.PhysicalMemoryCollector;
 
/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class RessourceManager {

    int nbWorkers = 1;
    private final Semaphore available;
    private List<Collector<Double>> collectors = new ArrayList<>();
    

    public RessourceManager() {
        // first, get the number of available cores
        nbWorkers = Runtime.getRuntime().availableProcessors();
        // now, checks if the user definied max nbworkers
        String prop = PropertiesUtil.getProperty("nbworkers");
        if (prop != null) {
            nbWorkers = min(Integer.parseInt(prop), nbWorkers);
        }
        available = new Semaphore(nbWorkers, true);
    }

    public int howManyCores() {
        return available.availablePermits();
    }

    /**
     * Tries to acquire a give number of cores. If unavailable, wait up to one
     * second, otherwise return with false
     *
     * @param permits
     * @return
     */
    public boolean tryAcquire(int permits) {
        try {
            System.out.println("trying to acquire " + permits);
            return available.tryAcquire(permits, 1, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            return false;
        }
    }

    public void release(int permits) {
        System.out.println("Releasing!!! " + permits);
        available.release(permits);
    }

    public boolean checkRequirements(Properties reqRessources) {
        if (reqRessources == null) {
            return true;
        }
        ReqEvaluator eval = new ReqEvaluator(collectors);
        return eval.checkRequirements(reqRessources);
    }
    
    public void addCollector(Collector c) {
        this.collectors.add(c);
    }
    
    public void removeCollector(Collector c) {
        this.collectors.remove(c);
    } 
    
    public List<Collector<Double>> getCollectors() {
        return this.collectors;
    }
    
    public void setCollectors() {
        //this.addCollector(new CPULoadCollector());
        //this.addCollector(new CPUAverageLoadCollector());
        this.addCollector(new FreeMemoryCollector());
        this.addCollector(new PhysicalMemoryCollector());
        //this.addCollector(new TotalProcessorsCollector());
        this.addCollector(new AvailableDiskSpaceCollector());
        
    }

}
