/* *************************************************************** *
 * PER-MARE Project (project number 13STIC07)
 * http://cosy.univ-reims.fr/PER-MARE
 * A CAPES/MAEE/ANII STIC-AmSud collaboration program.
 * All rights reserved to project partners:
 *  - Universite de Reims Champagne-Ardenne, Reims, France 
 *  - Universite Paris 1 Pantheon Sorbonne, Paris, France
 *  - Universidade Federal de Santa Maria, Santa Maria, Brazil
 *  - Universidad de la Republica, Montevideo, Uruguay
 * 
 * *************************************************************** *
 */
package cloudfit.service;

import cloudfit.application.ApplicationInterface;
import cloudfit.util.MultiMap;
import cloudfit.util.Number160;
import cloudfit.util.PropertiesUtil;
import cloudfit.util.SingleLineFormatter;
import java.io.IOException;
import java.io.Serializable;
import static java.lang.Math.min;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.SerializationUtils;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class ThreadSolve implements JobManagerInterface {

    private Number160 jobId;
    private int nbWorkers = 4;
    //private CyclicWorker[] threads = null;
    private int status = 0; // -1 = stateTransfer, 0 = new, 1 = running, 2 = finished
    private boolean Finished = false;
    private Serializable Accumulator = null;
    private Semaphore available = new Semaphore(1, true);
    //private CopyOnWriteArrayList<TaskStatus> taskList = null;
    private ExecutorService executor;
    private ApplicationInterface jobClass;
    private ServiceInterface service;
    private Scheduler scheduler;
    private static final Logger log = Logger.getLogger(ThreadSolve.class.getName());
    private static FileHandler fh = null;
    private JobMessage obj = null;

    public ThreadSolve(ServiceInterface service, Number160 jobId, ApplicationInterface jobClass, String[] args) {
        try {
            getAvailable().acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(ThreadSolve.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            fh = new FileHandler("TestLogging.log", true);
            //fh.setFormatter(new SimpleFormatter());
            fh.setFormatter(new SingleLineFormatter());
            log.addHandler(fh);
            log.setLevel(Level.ALL);
            ConsoleHandler ch = new ConsoleHandler();
            log.addHandler(ch);

        } catch (IOException e) {
        }

        this.service = service;
        this.jobId = jobId;

        this.jobClass = jobClass;

        this.setStatus(NEW);

        jobClass.setArgs(args);
        if (args != null) {
            System.err.print("Args = ");
            for (String i : args) {
                System.err.print(" " + i + " ");
            }
            System.err.println("");
        }
        //jobClass.initializeApplication();
        jobClass.numberOfBlocks();
        System.err.println("InitNumberofBlocks = " + jobClass.getNumberOfBlocks());
        scheduler = new Scheduler(jobId, jobClass.getNumberOfBlocks());

        // first, get the number of available cores
        nbWorkers = Runtime.getRuntime().availableProcessors();
        // now, checks if the user definied max nbworkers
        String prop = PropertiesUtil.getProperty("nbworkers");
        if (prop != null) {
            nbWorkers = min(Integer.parseInt(prop), nbWorkers);
        }

        // adapt the number of the workers to the size of the job
        nbWorkers = min(jobClass.getNumberOfBlocks(), nbWorkers);
        //threads = new CyclicWorker[nbWorkers];
        executor = Executors.newFixedThreadPool(nbWorkers);

    }

    public void run() {
        //this.setName("CoreQueue");
        Thread.currentThread().setName("ThreadSolve " + jobId);

        //getAvailable().acquire();
        setFinished(false);
        setStatus(STARTED);
        //status = 1;
        System.err.println("Tasklist.size() = " + scheduler.size());
        for (int i = 0; i < nbWorkers; i++) {

            String[] args = jobClass.getArgs();
            ApplicationInterface jobInstance = SerializationUtils.clone(jobClass);
            jobInstance.setArgs(args, this);
            CyclicWorker worker = new CyclicWorker(this, jobInstance, scheduler);
            executor.execute(worker);
        }

        try {
            while (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS));
        } catch (InterruptedException ex) {
            Logger.getLogger(ThreadSolve.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.err.println("");
        this.setFinished(true);
        setStatus(COMPLETED);
        //status = 2;
        getAvailable().release();

    }

    @Override
    public void sendAll(Serializable msg, boolean metoo) {
        service.sendAll(msg, metoo);
    }

    @Override
    public void setTaskValue(Serializable obj, boolean local) {

        if (!Finished) {
            boolean weHaveAllResults = scheduler.setTaskValue(obj, local);

            if (weHaveAllResults) {
                System.err.println("We have all results, no need to wait the Workers");
                //executor.shutdownNow();
                executor.shutdown();
                getAvailable().release();
            }
        }
    }

    @Override
    public Serializable getResult() {
        return Accumulator;
    }

    private void finalizeResult() {
        if (Accumulator == null) {
            Accumulator = new MultiMap<String, Integer>();
            // TODO: call ApplicationInterface.finalizeApplication() to run the Accumulator code.
            // call Scheduler to retrive all task results and compose a single accumulator
        }
        System.err.println("&&&&&&&&&&&&&&& Finalizing " + scheduler.size());

        System.err.println(" &&&&& Finalized");
    }

    // Getters and Setters
    public ServiceInterface getService() {
        return service;
    }

    public void setService(ServiceInterface service) {
        this.service = service;
    }

    public Semaphore getAvailable() {
        return available;
    }

    public void setAvailable(Semaphore available) {
        this.available = available;
    }

    public boolean isFinished() {
        return Finished;
    }

    public void setFinished(boolean Finished) {
        this.Finished = Finished;
    }

    @Override
    public void setTaskList(Object taskList) {
        scheduler.setTaskList(taskList);
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public synchronized boolean needData(Number160 jobId, int taskId) {
        return scheduler.needData(jobId, taskId);
    }

    @Override
    public synchronized boolean hasData(Number160 jobId, int taskId) {
        return scheduler.hasData(jobId, taskId);
    }

    @Override
    public synchronized Serializable getTaskValue(Number160 jobId, int taskId) {
        return scheduler.getTaskValue(jobId, taskId);
    }

    @Override
    public boolean waitFinished() throws InterruptedException {
        //System.err.println("Semaphore = " + getAvailable().availablePermits());
        getAvailable().acquire();
        finalizeResult();
        Finished = true;

        return Finished;
    }

    @Override
    public Number160 getJobId() {
        return jobId;
    }

    @Override
    public void setStatus(int value) {
        status = value;
    }

    @Override
    public Serializable getJobMessage() {

        JobMessage stateTransfer = new JobMessage(this.jobId, obj.getJobClass(), obj.getArgs());
        stateTransfer.setJar(obj.getJar());
        stateTransfer.setApp(obj.getApp());
        //stateTransfer.setTaskValue(taskList);
        CopyOnWriteArrayList remoteList = new CopyOnWriteArrayList<TaskStatus>();

        TaskStatus currentTask = null;

        TaskStatus remoteTask = null;
        for (int i = 0; i < scheduler.size(); ++i) {
            currentTask = scheduler.getTaskStatus(i);
            remoteTask = new TaskStatus(this.jobId, currentTask.getTaskId());
            if (currentTask.getStatus() == TaskStatus.COMPLETED) {
                remoteTask.setStatus(TaskStatus.COMPLETED);
            } else {
                remoteTask.setStatus(currentTask.getStatus());
            }
            remoteList.add(remoteTask);
        }
        stateTransfer.setData(remoteList);
        return stateTransfer;
    }

    // Storage methods
    @Override
    public void save(String key, Serializable value, boolean mutable) {
        service.save(key, value, mutable);
    }

    @Override
    public Serializable read(String key) {
        return service.read(key);
    }

    @Override
    public void remove(String key) {
        service.remove(key);
    }

    @Override
    public void setOriginalMsg(JobMessage obj) {
        this.obj = obj;
    }

    @Override
    public JobMessage getOriginalMsg() {

        return this.obj;
    }

}
