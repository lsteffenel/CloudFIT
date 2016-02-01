/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.service;

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
public class Scheduler {

    private CopyOnWriteArrayList<TaskStatus> taskList = null;
    private int completed = 0;
    private int backoff = 10000;

    public Scheduler(Number160 jobId, int nbTasks) {
        startTaskList(jobId, nbTasks);

        String prop = PropertiesUtil.getProperty("backoff");
        if (prop != null) {
            backoff = Integer.parseInt(prop);
        }
    }

    private void startTaskList(Number160 jobId, int nbTasks) {
        taskList = new CopyOnWriteArrayList<TaskStatus>();
        for (int i = 0; i < nbTasks; i++) {
            taskList.add(new TaskStatus(jobId, i));
        }
        // We do it 3 times to really "shake" the list and avoid similar list orders
        // TODO this part shold be externalized on a "scheduler" class
        Collections.shuffle(taskList);
        Collections.shuffle(taskList);
        Collections.shuffle(taskList);

        System.err.println("Tasklist ready " + nbTasks);
        for (int i = 0; i < nbTasks; ++i) {
            System.err.print(taskList.get(i).getTaskId() + "[" + taskList.get(i).getStatus() + "] - ");
        }
        System.err.println("");
    }

    public void setTaskList(Object taskList) {
        this.taskList = (CopyOnWriteArrayList<TaskStatus>) taskList;
        System.err.println("Tasklist ready " + this.taskList.size());
        for (int i = 0; i < this.taskList.size(); ++i) {
            System.err.print(this.taskList.get(i).getTaskId() + "[" + this.taskList.get(i).getStatus() + "] - ");
        }
        System.err.println("");
    }

    public int size() {
        return taskList.size();
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
        // or one of the locally started is stuck. But before, let me give it a chance (backoff)

        it = taskList.iterator();
        boolean remaining = false;
        do {
            if (remaining == true) {
                try {
                    Thread.sleep(backoff);
                    System.err.println("sleep backoff");
                } catch (InterruptedException ex) {
                    //Logger.getLogger(Scheduler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            while (it.hasNext()) {
                TaskStatus ts = (TaskStatus) it.next();
                if (ts.getStatus() == TaskStatus.STARTED_DISTANT) {
                    if (ts.getTimeSinceUpdate() > backoff) {
                        return ts;
                    } else {
                        remaining = true;
                    }
                }
            }
        } while (remaining == true);
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
        //boolean weHaveAllResults = true;
        TaskStatus currentTask;
        TaskStatusMessage incomingTask;
        incomingTask = (TaskStatusMessage) obj;
        //if (!Finished) {
        for (int i = 0; i < taskList.size(); ++i) {
            currentTask = taskList.get(i);
            if (currentTask.getTaskId() == ((TaskStatusMessage) obj).getTaskId()) {
                if (incomingTask.getStatus() == TaskStatus.STARTED) {
                    if (currentTask.getStatus() == TaskStatus.NEW) {
                        if (local) {
                            currentTask.setStatus(TaskStatus.STARTED);
                        } else {
                            currentTask.setStatus(TaskStatus.STARTED_DISTANT);
                            System.err.println("Someone is working on this task (" + currentTask.getTaskId() + "), marking it as \"running somewhere else\"");
                        }
                    }
                }

                if (incomingTask.getStatus() == TaskStatus.COMPLETED) {

                    if (currentTask.getStatus() != TaskStatus.COMPLETED) {
                        currentTask.setStatus(TaskStatus.COMPLETED);
                        // 13/01 - only status, not data
                        currentTask.setTaskResult(((TaskStatusMessage) obj).getTaskValue());
                        //System.err.println("New result from others (S): " + currentTask.getTaskId() + "[" + currentTask.getStatus() + "]");
                        completed++;
                        System.err.println(currentTask.getTaskId() + "---> " + completed + "/" + taskList.size());
                    }
                }
            }
//            if (currentTask.getStatus() != TaskStatus.COMPLETED) {
//                weHaveAllResults = false;
//            }
        }
        boolean weHaveAllResults = false;
        weHaveAllResults = (completed == taskList.size());
        

        return weHaveAllResults;
    }

    TaskStatus getTaskStatus(int taskId) {
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
