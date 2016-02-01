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
import cloudfit.util.Serialization;
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

    private Number160 jobId = null;
    private long processId = 1;
    private ActiveBlockingQueue appQueue = null;
    private ORBInterface router = null;
    protected JobsContainer JobsC = null;
    private Serialization serTool = null;
    //private ArrayList<JobManagerInterface> Jobs = null;

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
        //this.Jobs = new ArrayList<JobManagerInterface>();
        //requestStateOnJoin();
        serTool = new Serialization();

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

        System.err.println("new plug " + jobId);
        jm = new JobMessage(null,jar, app, args);
        

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

        System.err.println("new plug " + jobId);
        //try {
        //jm = new JobMessage(jobId, app.getClass().newInstance(), args);
        // ALERT : we suspect that the "app" instance created a serialization problem when transmitted
        // by Pastry (which uses a different serialization system than Java's
        jm = new JobMessage(null, app, args);

        //router.sendAll(new Message(new Long(processId), jm));

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
        router.sendAll(new Message(processId, jm));
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
//            Iterator<JobManagerInterface> it = Jobs.iterator();
//            while (it.hasNext()) {
//                JobManagerInterface element = it.next();
//                if (element.getJobId() == waitingJobId) {
//                    started = true;
//                }
//            }
            element = JobsC.getJob(waitingJobId);
//            if (element!=null)
//            {
//                started = true;
//            }
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
        JobMessage jmdelete = new JobMessage(jobid,true);
        router.sendAll(new Message(processId, jmdelete));
        
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
        //System.err.println("a new msg has arrived!");
        if (obj.getClass() == JobMessage.class) {
            JobMessageHandler((JobMessage) obj);
        }
        if (obj.getClass() == TaskStatusMessage.class) {
            TaskMessageStatusHandler((TaskStatusMessage) obj);

        }
        if (obj.getClass() == StateRequestMessage.class) {
            StatusRequestHandler((StateRequestMessage) obj);
        }

        Runtime.getRuntime().gc();
    }

    private void JobMessageHandler(JobMessage obj) {
        if (obj.getJobId() == null) {
        } else {
            JobManagerInterface element = null;
            element = JobsC.getJob(obj.getJobId());

            if (element == null) {
                // new version (it's the container that instantiate the solver)
                JobsC.addJob(obj, this);

            }
            else 
            {
                if (obj.isDelete())
                {
                    JobsC.remove(obj);
                }
            }
        }
    }

    protected void TaskMessageStatusHandler(TaskStatusMessage obj) {
        JobManagerInterface element = null;
        element = JobsC.getJob(obj.getJobId());
        //System.err.println("STATUS MESSAGE FROM uknown JOB " + obj.getJobId() + " do I know it ? " + element);
        //element.setStatus(2);

        if (element == null) { // message from a Job I don't know -> join!!!!
            System.err.println("STATUS MESSAGE FROM uknown JOB " + obj.getJobId() + " join!!!");
            StateRequestMessage srm = new StateRequestMessage(obj.getJobId());
            
            this.sendAll(srm);
            //requestState(obj);
        } else {
//        if (element != null) {
            if (element.getStatus() == element.STARTED) { // the job is still running
                element.setTaskValue(obj,false);
            } // else ignore old message
        }
    }

    private void StatusRequestHandler(StateRequestMessage obj) {
        Number160 jid = obj.getJobID();
        if (JobsC.getJob(jid) != null) {
            if (JobsC.getJob(jid).getStatus() > JobManagerInterface.NEW) {
                System.err.println("####### receiving a state transfer request");
                    //StateReplyMessage reply = new StateReplyMessage(JobsC.getJob(obj.getJobID()).getJobMessage());
                    sendAll(JobsC.getJob(obj.getJobID()).getJobMessage());
                    System.err.println("####### state transfer sent");
                
            }
        }
    }
    
//    private void StatusRequestHandler(StateRequestMessage obj) {
//        int jid = obj.getJobID();
//        if (JobsC.getJob(jid) != null) {
//            if (JobsC.getJob(jid).getStatus() > JobManagerInterface.NEW) {
//                System.err.println("####### receiving a state transfer request");
//                try {
//                    //Socket s = new Socket(obj.getSin().getAddress(), obj.getSin().getPort());
//                    Socket s = new Socket();
//                    System.err.println("connecting to "+obj.getSin().getAddress()+":"+obj.getSin().getPort());
//                    s.connect(new InetSocketAddress(obj.getSin().getAddress(), obj.getSin().getPort()), 500);
//                    OutputStream outs = s.getOutputStream();
//                    //ByteArrayOutputStream bOut = new ByteArrayOutputStream();
//                    //serTool.writeObject(bOut, JobsC.getJob(obj.getJobID()).getJobMessage());
//                    //System.err.println("The size of the object is: " + bOut.toByteArray().length);
//
//                    serTool.writeObject(outs, JobsC.getJob(obj.getJobID()).getJobMessage());
//                    outs.close();
//                    s.close();
//                    System.err.println("####### state transfer sent");
//                } catch (ConnectException ex3) {
//
//                } catch (SocketTimeoutException ex2) {
//                    //Logger.getLogger(Community.class.getName()).log(Level.SEVERE, null, ex2);
//                } catch (IOException ex) {
//                    Logger.getLogger(Community.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//    }

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
    public void sendAll(Serializable msg) {
        //System.err.println("Sending"+msg.getClass());
        router.sendAll(new Message(new Long(processId), msg));
    }

    // Storage methods
    @Override
    public void save(String key, Serializable value, boolean mutable) {
        //router.save(key, value);
        router.blocking_save(key, value, mutable);
    }

    @Override
    public Serializable read(String key) {
        return router.read(key);
    }

    @Override
    public void remove(String key) {
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
//        for (int i = 0; i < Jobs.size(); i++) {
//                    if (Jobs.get(i).getJobId() == jobId) {
//                        JobManagerInterface job = Jobs.get(i);
//                        return job.needData(jobId, taskId);
//                    }
//        }
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
//        for (int i = 0; i < Jobs.size(); i++) {
//            if (Jobs.get(i).getJobId() == jobId) {
//                JobManagerInterface job = Jobs.get(i);
//                return job.hasData(jobId, taskId);
//            }
//        }
//        return false;
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
//        for (int i = 0; i < Jobs.size(); i++) {
//            if (Jobs.get(i).getJobId() == jobId) {
//                JobManagerInterface job = Jobs.get(i);
//                return job.getTaskValue(jobId, taskId);
//            }
//        }
//        return null;
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

//    private synchronized void requestState(TaskStatusMessage msg) {
//
//        ServerSocket server = null;
//        InetSocketAddress address = null;
//
//        try {
//            server = new ServerSocket(0, 1); // uses a backlog of 1 connection only
//            //server.setSoTimeout(3000);
//            System.err.println("Join State transfer listening on "+ server.getInetAddress() +"on port: " + server.getLocalPort());
//            address = new InetSocketAddress(server.getInetAddress(), server.getLocalPort());
//
//            StateRequestMessage srm = new StateRequestMessage(address, msg.getJobId());
//            //System.err.println(srm.getJobID());
//
//            this.sendAll(srm);
//
//            Socket s = server.accept();
//
//            System.err.println("Incoming answer !!!!");
//            InputStream ins = s.getInputStream();
//
//            JobMessage jobMessage = (JobMessage) serTool.readObject(ins);
//            JobMessageHandler(jobMessage);
//
//            // now extract the current state from content
//        } catch (SocketTimeoutException ex) {
//            Logger.getLogger(Community.class.getName()).log(Level.INFO, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(Community.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(Community.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//    }

}