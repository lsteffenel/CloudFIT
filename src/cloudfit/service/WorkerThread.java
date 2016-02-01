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
import cloudfit.util.Number160;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class WorkerThread implements Runnable {

    private ApplicationInterface jobClass;
    private JobManagerInterface thrSolver;
    private TaskStatus taskId;
    private static final Logger log = Logger.getLogger(WorkerThread.class.getName());
    private static FileHandler fh=null;

    public WorkerThread(JobManagerInterface thrSolver, ApplicationInterface app, TaskStatus ts) {
        this.thrSolver = thrSolver;
        this.jobClass = app;
        this.taskId = ts;
        
        try {
            fh = new FileHandler("TestLogging.log", true);
            log.addHandler(fh);
        } catch (IOException e) {
        }

    }

    @Override
    public void run() {
        if (taskId.getStatus() != TaskStatus.COMPLETED && taskId.getStatus() != TaskStatus.DISTANT) {
            taskId.setStatus(TaskStatus.STARTED);
            long init = System.currentTimeMillis();
            Serializable serRes = solve();
            long end = System.currentTimeMillis();
            log.log(Level.FINE, "task " + taskId.getTaskId() + " - " + (end - init));

            // a final test, as results may have been learned from the network before ending this block
            if (taskId.getStatus() != TaskStatus.COMPLETED) {
                taskId.setStatus(TaskStatus.COMPLETED);

                Number160 jbId = taskId.getJobId();
                int tkId = taskId.getTaskId();

                // Only prevents others that the task was finished if there is something to tell
                // on the app, it can decide to return null if the task was already made by someone
                if (serRes != null) {
                    //Serializable tkResult = null;
                    taskId.setTaskResult(serRes);
                    //tkResult = taskId.getTaskResult();
                    // the tkResult will be split from the messag in the thrSolver to be stored in the DHT
                    TaskStatusMessage tm = new TaskStatusMessage(taskId.getJobId(), taskId.getTaskId(), taskId.getStatus(), serRes);
                    //System.err.println("Task " + tkId + " done");
                    thrSolver.sendAll(tm);
                }

            }
        }
//        System.err.println("Task "+taskId.getTaskId()+" done.");
    }

    public Serializable solve() {

        Serializable serRes = jobClass.executeBlock(taskId.getTaskId(), null);
        return serRes;
    }
}
