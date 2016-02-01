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

import cloudfit.util.Number160;
import java.io.Serializable;

/**
 * Class used to announce the status of a Task
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class TaskStatusMessage implements Serializable {

    private Number160 jobId = null;
    private int taskId = -1;
    private int status = 0;
    private Serializable TaskValue = null;

    /**
     * Constructor of the class
     * @param jobId the JobID associated to this task
     * @param taskId the task number
     * @param value the result of the task
     */
    public TaskStatusMessage(Number160 jobId, int taskId, int status, Serializable value) {
        this.jobId = jobId;
        this.taskId = taskId;
        this.status = status;
        this.TaskValue = value;
    }

    /**
     * returns the JobId associated to the announced task
     * @return 
     */
    public Number160 getJobId() {
        return jobId;
    }

    /**
     * gets the task number of the annouced Task
     * @return 
     */
    public int getTaskId() {
        return taskId;
    }

    /**
     * gets the value of the announces task
     * @return 
     */
    public Serializable getTaskValue() {
        return TaskValue;
    }
    
    public int getStatus() {
        return status;
    }
}
