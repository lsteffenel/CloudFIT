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
import cloudfit.core.Message;
import cloudfit.core.ORBInterface;
import cloudfit.util.Number160;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service-layer implementation. Connected to the protocol (ORB) layer below,
 * this class starts a
 *
 * @see ThreadSolvetoexecutethe job application
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class Community implements ServiceInterface {

    private long processId = 1;
    private ActiveBlockingQueue appQueue = null;
    private ORBInterface router = null;
    protected JobsContainer JobsC = null;

    /**
     * Constructor of the class
     *
     * @param pid the identifier of this service, used by the ORB to deliver
     * message to the right service
     * @param na a reference to the ORB
     */
    public Community(long pid, ORBInterface na) {
        this.processId = pid;
        this.appQueue = new ActiveBlockingQueue(this.processId, this);
        this.appQueue.start();
        this.router = na;
        this.JobsC = new JobsContainer();
        JobsC.start();
    }

    /**
     * Method used to submit a job
     *
     * @param app the class of the application to run on the submitted job
     * @param args the application parameters
     * @return the id of the submission
     */
    public Number160 plug(String jar, String app, String[] args) {

        JobMessage jm;

        jm = new JobMessage(null, jar, app, args);

        return this.plug(jm);
    }

    /**
     * Method used to submit a job
     *
     * @param app the class of the application to run on the submitted job
     * @param args the application parameters
     * @return the id of the submission
     */
    public Number160 plug(ApplicationInterface app, String[] args) {

        //++jobId;
        JobMessage jm;

        jm = new JobMessage(null, app, args);

        return this.plug(jm);
    }

    /**
     * Method used to submit a job through an external service (submit, for
     * example)
     *
     * @param jm a JobMessage containing the class and the parameters
     * @return the id of the submission
     */
    public Number160 plug(JobMessage jm) {
        long time = System.currentTimeMillis();
        Number160 njob = new Number160(time);
        // sets a jobId as the submit interface does not knows the current one
        jm.setJobId(njob);
        //router.sendAll(new Message(processId, jm), true);
        this.sendAll(jm, true);
        return njob;
    }

    /**
     * Method that blocks waiting for the job completion
     *
     * @param waitingJobId the id of the job
     * @return the result of the job
     * @throws InterruptedException
     */
    public Serializable waitJob(Number160 waitingJobId) throws InterruptedException {
        //TODO - replace the sleep by a synchro blocking
        boolean started = false;
        JobManagerInterface element = null;
        while (element == null) {

            element = JobsC.getJob(waitingJobId);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Community.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //System.err.println("status = " + element.getStatus());
        element.waitFinished();
        Serializable result = element.getResult();

        return result;
    }

    public int probeJob(Number160 waitingJob) {
        JobManagerInterface element = null;
        element = JobsC.getJob(waitingJob);
        if (element != null) {
            return element.getStatus();
        }
        return JobManagerInterface.NOTFOUND;
    }

    public void removeJob(Number160 jobid) {
        JobMessage jmdelete = new JobMessage(jobid, true);
        this.sendAll(jmdelete, true);
        //router.sendAll(new Message(processId, jmdelete), true);

    }

    // ServiceInterface methods
    /**
     * Returns this service ID
     *
     * @return
     */
    @Override
    public long getProcessId() {
        return processId;
    }

    /**
     * Method called by the ActiveQueue to notify the arrival of a new message
     *
     * @param obj the message
     */
    @Override
    public void notify(Serializable obj) {
        //System.err.println("a new msg has arrived!" + obj.getClass());
        if (obj.getClass() == JobMessage.class) {
            JobMessageHandler((JobMessage) obj);
        } else {
            if (obj.getClass() == TaskStatusMessage.class) {
                TaskMessageStatusHandler((TaskStatusMessage) obj);

            } else {
                if (obj.getClass() == StateRequestMessage.class) {
                    StatusRequestHandler((StateRequestMessage) obj);
                } else {
                    System.err.println("Unexplained msg " + obj.getClass());
                }
            }
        }
        //System.out.println("");
        Runtime.getRuntime().gc();
    }

    private void JobMessageHandler(JobMessage obj) {
        if (obj.getJobId() == null) {
            System.err.println("JobId = null");
        } else {
            JobManagerInterface element = null;
            element = JobsC.getJob(obj.getJobId());

            if (element == null) {
                // new version (it's the container that instantiate the solver)
                System.err.println("Adding job" + obj.getJobId());
                JobsC.addJob(obj, this);

            } else {
                //System.out.println("known element");
                if (obj.isDelete()) {
                    JobsC.remove(obj);
                    System.err.println("Removing job");
                }
            }
        }
    }

    protected void TaskMessageStatusHandler(TaskStatusMessage obj) {
        JobManagerInterface element = null;
        element = JobsC.getJob(obj.getJobId());

        if (element == null) { // message from a Job I don't know -> join!!!!
            System.err.println("UNKNOWN JOB " + obj.getJobId() + "-->  JOIN!!!");
            StateRequestMessage srm = new StateRequestMessage(obj.getJobId());

            this.sendAll(srm, false);
        } else {
            if (element.getStatus() == element.STARTED) { // the job is still running
                element.setTaskValue(obj, false);
            } // else ignore old message
        }
    }

    private void StatusRequestHandler(StateRequestMessage obj) {
        Number160 jid = obj.getJobID();
        if (JobsC.getJob(jid) != null) {
            //if (JobsC.getJob(jid).getStatus() > JobManagerInterface.NEW) {
            System.err.println("####### receiving a state transfer request");
            //StateReplyMessage reply = new StateReplyMessage(JobsC.getJob(obj.getJobID()).getJobMessage());
            JobManagerInterface delayed = JobsC.getJob(obj.getJobID());
            JobMessage jm = (JobMessage) delayed.getJobMessage();

            this.sendAll(jm, false);
            System.err.println("####### state transfer sent");

        }
    }

    /**
     * Method used by the ORB thread to deliver a message to this service
     *
     * @param obj the message
     */
    @Override
    public void put(Serializable obj) {
        try {
            this.appQueue.put(obj);
        } catch (InterruptedException ex) {
            Logger.getLogger(Community.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method to send a message via the protocol level (ORB)
     *
     * @param msg the message
     */
    @Override
    public void send(Serializable msg) {
        router.sendNext(new Message(new Long(processId), msg));
    }

    /**
     * Method to send a broadcast message via the protocol level (ORB)
     *
     * @param msg the message
     */
    @Override
    public void sendAll(Serializable msg, boolean metoo) {
        //System.err.println("Sending"+msg.getClass());
        router.sendAll(new Message(processId, msg), metoo);
    }

    // Storage methods
    @Override
    public void save(Serializable value, String...keys) {
        //router.save(key, value);
        router.blocking_save(value, keys);
    }

    @Override
    public Serializable read(String...key) {
        return router.read(key);
    }

    @Override
    public void remove(String...key) {
        router.remove(key);
    }

    /**
     * Checks if a given job/task has already data or it can benefit from
     * another node data.
     *
     * @param jobId
     * @param taskId
     * @return true if the task has not been completed (no data yet) or false
     * otherwise
     */
    public boolean needData(Number160 jobId, int taskId) {
        JobManagerInterface job = null;
        job = JobsC.getJob(jobId);
        if (job != null) {
            return job.needData(jobId, taskId);
        } else {
            return false;
        }
    }

    public boolean hasJob(Number160 jobId) {
        if (JobsC.getJob(jobId) == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks if a job/task has already a result data (i.e., the task has been
     * completed)
     *
     * @param jobId
     * @param taskId
     * @return true if task has data, false otherwise
     */
    public boolean hasData(Number160 jobId, int taskId) {
        JobManagerInterface job = null;
        job = JobsC.getJob(jobId);
        if (job != null) {
            return job.hasData(jobId, taskId);
        } else {
            return false;
        }

    }

    /**
     * Returns the task result from a given job/task
     *
     * @param jobId
     * @param taskId
     * @return the task result in the form of a Serializable object
     */
    public Serializable getData(Number160 jobId, int taskId) {
        JobManagerInterface job = null;
        job = JobsC.getJob(jobId);
        if (job != null) {
            return job.getTaskValue(jobId, taskId);
        } else {
            return null;
        }
    }

    /**
     * Method to return the JobsContainer object representing the current state
     * of jobs/tasks. Used to feed joining nodes.
     *
     * @return JobsContainer
     */
    public JobsContainer getCurrentState() {
        return JobsC;
    }

}
