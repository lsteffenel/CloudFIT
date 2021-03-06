/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.application;

import cloudfit.service.JobManager;
import cloudfit.util.Number160;
import cloudfit.util.PropertiesUtil;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author angelo
 */
public class TaskScheduler {

    private CopyOnWriteArrayList<TaskStatus> taskList = null;
    //private int completed = 0;
    private int backoff = 10000;
    private boolean weHaveAllResults = false;
    private JobManager jm = null;

    public TaskScheduler(JobManager jm, Number160 jobId, int nbTasks, Serializable[][] depMatrix) {
        this.jm = jm;
        startTaskList(jobId, nbTasks, depMatrix);

        String prop = PropertiesUtil.getProperty("backoff");
        if (prop != null) {
            backoff = Integer.parseInt(prop);
        }
    }

    private void startTaskList(Number160 jobId, int nbTasks, Serializable[][] depMatrix) {
        taskList = new CopyOnWriteArrayList<TaskStatus>();
        for (int i = 0; i < nbTasks; i++) {
            if (depMatrix == null) {
                taskList.add(new TaskStatus(jobId, i));
            } else {
                taskList.add(new TaskStatus(jobId, i, depMatrix[i]));
            }
        }
        // We do it 3 times to really "shake" the list and avoid similar list orders
        // TODO this part shold be externalized on a "context scheduler" class
        Collections.shuffle(taskList);
        Collections.shuffle(taskList);
        Collections.shuffle(taskList);

        System.err.println("Tasklist ready " + nbTasks);
        for (int i = 0; i < nbTasks; ++i) {
            System.err.print(taskList.get(i).getTaskId() + "[" + taskList.get(i).getStatus() + "] - ");
        }
        System.err.println("");
    }

    /**
     * Receives a tasklist from another node (like in a job state transfer must
     * set the data without changing the previous order
     *
     * @param taskList
     */
    public void setTaskList(Object taskList) {

        CopyOnWriteArrayList<TaskStatus> incoming = (CopyOnWriteArrayList<TaskStatus>) taskList;
        int done = 0;
        for (int j = 0; j < this.taskList.size(); j++) {
            for (int i = 0; i < incoming.size(); i++) {
                if (incoming.get(i).getTaskId() == this.taskList.get(j).getTaskId()) {
                    if (incoming.get(i).getStatus() > this.taskList.get(j).getStatus()) {
                        if (incoming.get(i).getStatus() == TaskStatus.STARTED) {
                            this.taskList.get(j).setStatus(TaskStatus.STARTED_DISTANT);
                        } else {
                            this.taskList.get(j).setStatus(incoming.get(i).getStatus());
                        }
                        if (incoming.get(i).getStatus() == TaskStatus.COMPLETED) {
                            this.taskList.get(j).setTaskResult(incoming.get(i).getTaskResult());
                            done++;
                        }
//                          if (this.taskList.get(j).getStatus() == TaskStatus.COMPLETED) {
//                            completed++;
//                        }
                        //j = this.taskList.size(); // break
                    }
                    System.err.print(this.taskList.get(j).getTaskId() + "[" + this.taskList.get(j).getStatus() + "] - ");
//                      
                }
            }
        }
        weHaveAllResults = (done == this.taskList.size());

    }

    public int size() {
        return taskList.size();
    }

    public boolean haveAllResults() {
        return weHaveAllResults;
    }

    /**
     *
     * @return
     */
    public synchronized TaskStatus getWork() {
        // if there is a free task
        Iterator it = taskList.iterator();
        while (it.hasNext()) {
            TaskStatus ts = (TaskStatus) it.next();
            if (ts.getStatus() == TaskStatus.NEW) {
                return ts;
            }
        }
        // ok, no free tasks anymore. Look for a distant task (speculative execution)
        // but before, let me give it a chance (backoff)

        //it = taskList.iterator();
        boolean remaining = false;
        do {
            if (remaining == true) {
                try {
                    Thread.sleep(backoff);
                    //System.err.println("sleep backoff ");
                } catch (InterruptedException ex) {
                    //Logger.getLogger(TaskScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            it = taskList.iterator();

            while (it.hasNext()) {
                TaskStatus ts = (TaskStatus) it.next();
                if (ts.getStatus() == TaskStatus.STARTED_DISTANT) {
                    //System.out.println("distant remaining " + ts.getTimeSinceUpdate());
                    if (ts.getTimeSinceUpdate() > backoff) {
                        ts.setStatus(TaskStatus.NEW);
                        return ts;
                    } else {
                        remaining = true;
                    }
                }
            }
        } while (remaining == true && !weHaveAllResults);

        // ok, no free tasks anymore. Looking for a locally started (stuck ?). But before, let me give it a chance (backoff)
        remaining = false;
        do {
            if (remaining == true) {
                try {
                    Thread.sleep(backoff);
                    //System.err.println("sleep backoff ");
                } catch (InterruptedException ex) {
                    //Logger.getLogger(TaskScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            it = taskList.iterator();

            while (it.hasNext()) {
                TaskStatus ts = (TaskStatus) it.next();
                if (ts.getStatus() == TaskStatus.STARTED) {
                    //System.out.println("local speculative " + ts.getTimeSinceUpdate());
                    if (ts.getTimeSinceUpdate() > backoff) {
                        ts.setStatus(TaskStatus.NEW);
                        return ts;
                    } else {
                        remaining = true;
                    }
                }
            }
        } while (remaining == true && !weHaveAllResults);

        // nothing else to run, distant or local. Return null to stop workers
        return null;
    }

    public boolean needData(Number160 jobId, int taskId) {
        TaskStatus currentTask = null;
//        if (status == JobManagerInterface.JOIN)
//        {
//            return false;
//        }
        for (int i = 0; i < taskList.size(); ++i) {
            currentTask = taskList.get(i);
            if (currentTask.getTaskId() == taskId) {
                if (currentTask.getStatus() != TaskStatus.COMPLETED) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasData(Number160 jobId, int taskId) {
        TaskStatus currentTask = null;
        for (int i = 0; i < taskList.size(); ++i) {
            currentTask = taskList.get(i);
            if (currentTask.getTaskId() == taskId) {
                if (currentTask.getStatus() == TaskStatus.COMPLETED) {
                    return true;
                }
            }
        }
        return false;
    }

    public Serializable getTaskValue(Number160 jobId, int taskId) {
        TaskStatus currentTask = null;
        for (int i = 0; i < taskList.size(); ++i) {
            currentTask = taskList.get(i);
            if (currentTask.getTaskId() == taskId) {
                if (currentTask.getStatus() == TaskStatus.COMPLETED) {
                    TaskStatusMessage tm = new TaskStatusMessage(jobId, taskId, currentTask.getStatus(), currentTask.getTaskResult());
                    return tm;
                }
            }
        }
        return null;
    }

    public boolean setTaskValue(Serializable obj, boolean local) {
        // TODO: recalculate the "completed" at each pass. The current global variable is a possible source of probs

        TaskStatus currentTask;
        TaskStatusMessage incomingTask;
        incomingTask = (TaskStatusMessage) obj;
        int done = 0;
        //if (!Finished) {
        for (int i = 0; i < taskList.size(); ++i) {

            currentTask = taskList.get(i);
            if (currentTask.getTaskId() == incomingTask.getTaskId()) {
                if (incomingTask.getStatus() == TaskStatus.STARTED) {
                    if (currentTask.getStatus() == TaskStatus.NEW) {
                        if (local) {
                            currentTask.setStatus(TaskStatus.STARTED);
                        } else {
                            currentTask.setStatus(TaskStatus.STARTED_DISTANT);
                            System.err.println("Someone is working on this task (" + currentTask.getTaskId() + "), marking it as \"running somewhere else\"");
                        }
                    } else {
                        if (currentTask.getStatus() == TaskStatus.COMPLETED) {
                            TaskStatusMessage tm = new TaskStatusMessage(currentTask.getJobId(), currentTask.getTaskId(), currentTask.getStatus(), currentTask.getTaskResult());
                            System.err.println("I got a msg from a lost guy but can't answer back (yet)" + currentTask.getJobId() + " " + currentTask.getTaskId());
                            jm.sendAll(tm, false);

                        }
                    }
                } else {
                    if (incomingTask.getStatus() == TaskStatus.COMPLETED) {

                        if (currentTask.getStatus() < TaskStatus.COMPLETED) {
                            System.err.println("task " + currentTask.getTaskId() + " completed elsewhere");

                            currentTask.setStatus(TaskStatus.COMPLETED);
                            // 13/01 - only status, not data
                            currentTask.setTaskResult(incomingTask.getTaskValue());

                            //System.err.println("New result from others (S): " + currentTask.getTaskId() + "[" + currentTask.getStatus() + "]");
                            //completed++;
                            //System.err.println(incomingTask.getTaskId() + "---> " + done + "/" + taskList.size());
                        }
                    }
                }
            }
            if (currentTask.getStatus() == TaskStatus.COMPLETED) {
                done++;
            }
        }
        System.err.println("---> " + done + "/" + taskList.size());
        weHaveAllResults = (done == taskList.size());
        for (int j = 0; j < taskList.size(); ++j) {
            if (this.taskList.get(j).getStatus() < TaskStatus.COMPLETED) {
                System.err.print(this.taskList.get(j).getTaskId() + "[" + this.taskList.get(j).getStatus() + "] - ");
            }
        }
        System.err.println("");
        if (weHaveAllResults) {
            System.err.println("we have all results");
        }

        //weHaveAllResults = (completed == taskList.size());
        return weHaveAllResults;
    }

    public TaskStatus getTaskStatus(int taskId) {
        TaskStatus currentTask = null;
        for (int i = 0; i < taskList.size(); ++i) {
            currentTask = taskList.get(i);
            if (currentTask.getTaskId() == taskId) {

                return currentTask;

            }
        }
        return null;
    }

}
