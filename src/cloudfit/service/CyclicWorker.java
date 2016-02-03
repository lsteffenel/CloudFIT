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
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class CyclicWorker extends Thread {

    private ApplicationInterface jobClass;
    private JobManagerInterface thrSolver;
    private Scheduler ts;
    private Logger log;

    public CyclicWorker(JobManagerInterface thrSolver, ApplicationInterface app, Scheduler ts) {
        this.thrSolver = thrSolver;
        this.jobClass = app;
        this.ts = ts;
        log = Logger.getLogger(ThreadSolve.class.getName());
    }

    @Override
    public void run() {

        TaskStatus taskId = ts.getWork();
        while (taskId != null && thrSolver.getStatus() != 2) { // getWork returns null when there is no more tasks to execute or if the Job is finished
//            try {
            if (taskId.getStatus() != TaskStatus.COMPLETED && taskId.getStatus() != TaskStatus.DISTANT) {

                taskId.setStatus(TaskStatus.STARTED);

                // advertises other nodes that I'm working on this task
                TaskStatusMessage tmannonce = new TaskStatusMessage(taskId.getJobId(), taskId.getTaskId(), taskId.getStatus(), null);
                thrSolver.sendAll(tmannonce, false);

                long init = System.currentTimeMillis();
                //log.log(Level.FINE, "task i {0}:{1}", new Object[]{taskId.getJobId(),taskId.getTaskId()});
                Serializable serRes = solve(taskId);
                long end = System.currentTimeMillis();
                log.log(Level.FINE, "task {0}:{1} {2} {3}", new Object[]{taskId.getJobId(), taskId.getTaskId(), Long.toString(init), Long.toString(end)});

                // Only prevents others that the task was finished if there is something to tell
                // on the app, it can decide to return null if there was an error, or something else
                if (serRes != null) {
                    // a final test, as results may have been learned from the network before ending this block
                    if (taskId.getStatus() != TaskStatus.COMPLETED) {
                        //taskId.setStatus(TaskStatus.COMPLETED);

                        Number160 jbId = taskId.getJobId();
                        int tkId = taskId.getTaskId();

                        // the tkResult will be split from the messag in the thrSolver to be stored in the DHT
                        TaskStatusMessage tm = new TaskStatusMessage(jbId, tkId, TaskStatus.COMPLETED, serRes);
                        thrSolver.setTaskValue(tm, true);
                        //System.err.println("Task " + tkId + " done");
                        thrSolver.sendAll(tm, false);

                    }
                } else {
                    if (taskId.getStatus() == TaskStatus.STARTED) {
                        taskId.setStatus(TaskStatus.STARTED_DISTANT);
                    }
                }
            }
            taskId = ts.getWork();
        }
        System.err.println("No more tasks to do, leaving.");
    }

    public Serializable solve(TaskStatus taskId) {

        Serializable serRes = jobClass.executeBlock(taskId.getTaskId(), null);
        return serRes;
    }
}
