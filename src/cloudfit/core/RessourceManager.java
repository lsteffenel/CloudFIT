/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.core;

import cloudfit.service.JobManagerInterface;
import cloudfit.service.JobMessage;
import cloudfit.service.JobsScheduler;
import cloudfit.service.ServiceInterface;
import cloudfit.util.Number160;
import cloudfit.util.PropertiesUtil;
import static java.lang.Math.min;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.permare.context.*;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class RessourceManager implements RessourceManagerInterface {

    int nbWorkers = 1;
    //private final Semaphore available;
    private HashMap<String, Collector<Double>> collectors = new HashMap<>();
    private JobsScheduler js;
    private ThreadPoolExecutor executive;
    
    public JobsScheduler getJobScheduler() {
        return js;
    }
    
    public RessourceManager(JobsScheduler js) {
        this.js = js;
        // first, get the number of available cores
        nbWorkers = Runtime.getRuntime().availableProcessors();
        // now, checks if the user definied max nbworkers
        String prop = PropertiesUtil.getProperty("nbworkers");
        if (prop != null) {
            nbWorkers = min(Integer.parseInt(prop), nbWorkers);
        }
        //available = new Semaphore(nbWorkers, true);

        setCollectors();

        executive = new ThreadPoolExecutor(nbWorkers, nbWorkers,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        executive.setMaximumPoolSize(nbWorkers);

        for (int i = 0; i < nbWorkers; i++) {

            Worker worker = new Worker(this);
            executive.execute(worker);
        }
    }

    public int howManyCores() {
        return nbWorkers;
    }

    public boolean checkRequirements(Properties reqRessources) {
        if (reqRessources == null) {
            System.err.println("no requirements to evaluate");
            return true;
        }
        ReqEvaluator eval = new ReqEvaluator(collectors);
        
        boolean itisok = eval.checkRequirements(reqRessources);
        System.err.println("requirments =" + itisok);
        return itisok;
    }

    public void addCollector(Collector c) {
        this.collectors.put(c.getCollectorName(), c);
    }

    public void removeCollector(Collector c) {
        this.collectors.remove(c.getCollectorName());
    }

    @Override
    public void setCollectors() {

        this.addCollector(new CPUSystemLoad());
        this.addCollector(new CPUSystemLoadAverage());
        this.addCollector(new AvailableDiskSpaceCollector());
        //this.addCollector(new UnallocatedDiskSpaceCollector());
        this.addCollector(new FreePhysicalMemoryCollector());
        this.addCollector(new TotalPhysicalMemoryCollector());
        this.addCollector(new FreeSwapMemoryCollector());
        this.addCollector(new TotalSwapMemoryCollector());
        this.addCollector(new FreeVMMemoryCollector());
        this.addCollector(new TotalVMMemoryCollector());
        this.addCollector(new TotalProcessorsCollector());

    }
    
    public void addJob(JobMessage obj, ServiceInterface comm) {
        js.addJob(obj, comm);
    }
    
    public JobManagerInterface getJob(Number160 jobId) {
        return js.getJob(jobId);
    }
    
    public void remove(JobMessage obj) {
        js.remove(obj);
    }

    @Override
    public WorkData getWork() {
        return js.getWork();
    }

}
