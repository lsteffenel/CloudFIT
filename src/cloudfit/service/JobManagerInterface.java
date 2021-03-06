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

import cloudfit.core.WorkData;
import cloudfit.util.Number160;
import java.io.Serializable;
import java.util.Properties;

/**
 * General interface for the JobManager classes (ThreadSolve)
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public interface JobManagerInterface extends Serializable {

    public static int NOTFOUND = -1;
    public static int NEW = 0;
    public static int STARTED = 1;
    public static int COMPLETED = 2;
    public static int NOMATCH = 3; // required resources do not match the node, ignore and go to the next job.

    public boolean waitFinished() throws InterruptedException;

    public Number160 getJobId();

    public int getStatus();

    public void setStatus(int value);

    public void setTaskValue(Serializable obj, boolean local);

    public boolean needData(Number160 jobId, int taskId);

    public boolean hasData(Number160 jobId, int taskId);

    public Serializable getTaskValue(Number160 JobId, int taskId);

    public Serializable getJobMessage();

    public Serializable getResult();

    public void sendAll(Serializable msg, boolean metoo);

    //public Object getTaskList();
    public void setTaskList(Object taskList);

    public void save(Serializable value, String... keys);

    public Serializable read(Serializable... key);

    public boolean contains(Serializable... key);

    public void remove(Serializable... key);

    public void setOriginalMsg(JobMessage obj);

    public JobMessage getOriginalMsg();

    public boolean checkMatching();

    public WorkData getWork();

}
