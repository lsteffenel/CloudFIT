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
package cloudfit.application;

import cloudfit.util.Number160;
import java.io.Serializable;

/**
 * Class that storages the status of a task
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class TaskStatus implements Serializable {

    /* general status of the task:
     * 0 - NEW
     * 1 - STARTED
     * 2 - STARTED_DISTANT
     * 3 - FINISHED
     * 4 - DISTANT (state transfer)
     */
    public static int NEW = 0;
    public static int STARTED = 1;
    public static int STARTED_DISTANT = 2; // task started elsewhere, learnt by announce
    public static int COMPLETED = 3;
    public static int DISTANT = 4; // task completed but not locally stored. Used on state transfer (join)
    private Number160 jobId;
    private int taskId;
    private int status = 0;
    private Serializable taskResult = null;
    private long lastUpdateTime = 0;
    private Serializable[] dependencies = null;

    /**
     * Constructor of the class
     *
     * @param jobId the JobId associated to this job
     * @param num this task number
     */
    public TaskStatus(Number160 jobId, int num) {
        this.setJobId(jobId);
        this.setTaskId(num);
        this.dependencies = null;
    }
    
    public TaskStatus(Number160 jobId, int num, Serializable[] deps) {
        this.setJobId(jobId);
        this.setTaskId(num);
        this.dependencies = deps;
    }

    /**
     * Gets the ID of the job associated to this task
     *
     * @return
     */
    public Number160 getJobId() {
        return jobId;
    }

    /**
     * Sets the ID of the job associated to this task
     *
     * @param jobId
     */
    public void setJobId(Number160 jobId) {
        this.jobId = jobId;
    }

    /**
     * Gets the Task number of this task
     *
     * @return
     */
    public int getTaskId() {
        return taskId;
    }

    /**
     * Sets the task number of this task
     *
     * @param taskId
     */
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    /**
     * get the status of this task
     *
     * @return 0 if NEW, 1 if STARTED, 2 if COMPLETED
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the status of this task (0 if NEW, 1 if STARTED, 2 if COMPLETED)
     *
     * @param status
     */
    public synchronized void setStatus(int status) {
        this.lastUpdateTime = System.currentTimeMillis();
        this.status = status;
    }
    
    /** get the dependency array for the tasks
     * 
     * @return 
     */
    public Serializable[] getDeps() {
        return dependencies;
    }

    /**
     * Sets the result value of this task
     *
     * @param taskResult
     */
    public synchronized void setTaskResult(Serializable taskResult) {
        this.lastUpdateTime = System.currentTimeMillis();
        this.taskResult = taskResult;
    }

    /**
     * Returns the result data from this task
     *
     * @return
     */
    public Serializable getTaskResult() {
        return taskResult;
    }

    public long getTimeSinceUpdate() {
        return (System.currentTimeMillis() - this.lastUpdateTime);
    }
}
