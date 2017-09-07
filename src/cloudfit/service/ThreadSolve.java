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
import cloudfit.core.RessourceManager;
import cloudfit.util.Number160;
import cloudfit.util.SingleLineFormatter;
import java.io.IOException;
import java.io.Serializable;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private int status = 0; // -1 = stateTransfer, 0 = new, 1 = running, 2 = finished
    private boolean Finished = false;
    private Serializable Accumulator = null;
    private ExecutorService executor;
    private ApplicationInterface jobClass;
    private ServiceInterface service;
    private Scheduler scheduler;
    private static final Logger log = Logger.getLogger(ThreadSolve.class.getName());
    private static FileHandler fh = null;
    private JobMessage obj = null;
    private RessourceManager RM = null;
    private Properties reqRessources = null;

    public ThreadSolve(ServiceInterface service, Number160 jobId, ApplicationInterface jobClass, String[] args, Properties requirements) {
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

        this.reqRessources = requirements;

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
//
//        // first, get the number of available cores
//        nbWorkers = Runtime.getRuntime().availableProcessors();
//        // now, checks if the user definied max nbworkers
//        String prop = PropertiesUtil.getProperty("nbworkers");
//        if (prop != null) {
//            nbWorkers = min(Integer.parseInt(prop), nbWorkers);
//        }

        RM = service.getRessourceManager();

        boolean resourcesMatch = RM.checkRequirements(reqRessources); // check if the requirements match with the CPU/memory/etc 

        if (resourcesMatch) {

            do {
                nbWorkers = RM.howManyCores();
                if (nbWorkers == 0) {
                    nbWorkers = 1; // try 1 even if there is none available, will only get when available
                }

                // adapt the number of the workers to the size of the job
                nbWorkers = min(jobClass.getNumberOfBlocks(), nbWorkers);
            } while (!RM.tryAcquire(nbWorkers));

            //threads = new CyclicWorker[nbWorkers];
            executor = Executors.newFixedThreadPool(nbWorkers);
        } else {
            setStatus(NOMATCH);
        }

    }

    @SuppressWarnings("empty-statement")
    public void run() {
        Thread.currentThread().setName("ThreadSolve " + jobId);

        setFinished(false);
        setStatus(STARTED);
        System.err.println("Tasklist.size() = " + scheduler.size());
        for (int i = 0; i < nbWorkers; i++) {

            String[] args = jobClass.getArgs();
            ApplicationInterface jobInstance = SerializationUtils.clone(jobClass);
            jobInstance.setArgs(args, this);
            CyclicWorker worker = new CyclicWorker(this, jobInstance, scheduler);
            executor.execute(worker);
        }

        try {
            while (!this.isFinished()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ThreadSolve.class.getName()).log(Level.SEVERE, null, ex);
        }
        //RM.release(nbWorkers);
    }

    @Override
    public void sendAll(Serializable msg, boolean metoo) {
        service.sendAll(msg, metoo);
    }

    @Override
    public void setTaskValue(Serializable obj, boolean local) {

        if (!Finished) {
            scheduler.setTaskValue(obj, local);

            if (scheduler.haveAllResults()) {
                RM.release(nbWorkers);
                setStatus(COMPLETED);
                setFinished(true);

                executor.shutdownNow();
            }
        }
    }

    @Override
    public Serializable getResult() {
        return Accumulator;
    }

    private void finalizeResult() {
//        if (Accumulator == null) {
//            Accumulator = new MultiMap<String, Integer>();
//            // TODO: call ApplicationInterface.finalizeApplication() to run the Accumulator code.
//            // call Scheduler to retrive all task results and compose a single accumulator
//        }

        ArrayList<Serializable> al = new ArrayList();

        for (int i = 0; i < scheduler.size(); ++i) {
            TaskStatusMessage tm = (TaskStatusMessage) scheduler.getTaskValue(jobId, i);
            al.add(tm.getTaskValue());
        }

        Accumulator = jobClass.finalizeApplication(al);

        System.err.println("&&&&&&&&&&&&&&& Finalizing " + scheduler.size());
    }

    // Getters and Setters
    public ServiceInterface getService() {
        return service;
    }

    public void setService(ServiceInterface service) {
        this.service = service;
    }

    public boolean isFinished() {
        scheduler.haveAllResults();
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
        while (!this.isFinished()) {
            Thread.sleep(500);
        }
        finalizeResult();

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

        JobMessage stateTransfer = new JobMessage(this.jobId, obj.getJobClass(), obj.getArgs(), obj.getReqs());
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
    public void save(Serializable value, String... keys) {
        service.save(value, keys);
    }

    @Override
    public Serializable read(String... key) {
        return service.read(key);
    }

    @Override
    public void remove(String... key) {
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
