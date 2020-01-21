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
import cloudfit.application.Distributed;
import cloudfit.core.TheBigFactory;
import cloudfit.network.JarLoader;
import cloudfit.core.WorkData;
import cloudfit.storage.DHTStorageUnit;
import cloudfit.storage.FileContainer;
import cloudfit.util.Number160;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basically a container for Jobs array (and subsequent Tasks status/results).
 * This container is used to simplify serialization and transfer to new nodes
 * that join the community (state transfer)
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class JobsScheduler implements Serializable {

    private CopyOnWriteArrayList<JobManagerInterface> Jobs = null;

    /**
     * Constructor. Initializes the jobs array
     */
    public JobsScheduler() {
        //this.setName("JobsContainer");
        Jobs = new CopyOnWriteArrayList<JobManagerInterface>();
    }

    /**
     *
     * @return
     */
    public JobManagerInterface getJob(Number160 jobId) {
        Iterator<JobManagerInterface> it = Jobs.iterator();
        //System.err.println("getting "+jobId);
        while (it.hasNext()) {
            JobManagerInterface element = it.next();
            if (element.getJobId().compareTo(jobId) == 0) {
                return element;
            }
        }
        return null;
    }

    public void remove(JobMessage obj) {
        JobManagerInterface js = this.getJob(obj.getJobId());
        Jobs.remove(js);
    }

    public void addJob(JobMessage obj, ServiceInterface comm) {
        System.err.println("new job " + ((JobMessage) obj).getJobId());
        Number160 jobId = obj.getJobId();

        if (obj.getJar() == null) { // regular submission without a jar
            ApplicationInterface jobClass = obj.getJobClass();
            String[] jobargs = obj.getArgs();
            Serializable[][] deps = obj.getDepMatrix();
            
            Properties reqs = obj.getReqs();

            JobManagerInterface JM = TheBigFactory.getJobManager(comm, jobId, jobClass, jobargs, deps, reqs);
            if (obj.getData() != null) {
                //System.err.println("---->>>>> Accepting "+((CopyOnWriteArrayList<TaskStatus>)obj.getData()).size());
                JM.setTaskList(obj.getData());
            }
            JM.setOriginalMsg(obj);

            Jobs.add(JM);
            System.err.println("new job inserted (inner class)");
        } else { // submission through an external jobClass (jar)
            try {
                String jarFile = obj.getJar();
                DHTStorageUnit titi;
                titi = (DHTStorageUnit) comm.read(jarFile);
                if (titi != null) {

                    FileContainer fr = (FileContainer) titi.getContent();

                    ClassLoader loader = new JarLoader(fr.getContent());

                    Class<?> clazz = Class.forName(obj.getApp(), true, loader);

                    Class<? extends ApplicationInterface> runClass = clazz.asSubclass(Distributed.class);
                    // Avoid Class.newInstance, for it is evil.
                    Constructor<? extends ApplicationInterface> ctor = runClass.getConstructor();
                    ApplicationInterface jobClass = ctor.newInstance();

                    String[] jobargs = obj.getArgs();
                    Properties reqs = obj.getReqs();
                    Serializable[][] deps = obj.getDepMatrix();
                    

                    JobManagerInterface JM = TheBigFactory.getJobManager(comm, jobId, jobClass, jobargs, deps, reqs);
                    if (obj.getData() != null) {
                        JM.setTaskList(obj.getData());

                    }
                    JM.setOriginalMsg(obj);
                    Jobs.add(JM);
                    System.err.println("new job inserted (jar)");

                } else {
                    System.out.println("Jar file not found");
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(JobsScheduler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(JobsScheduler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(JobsScheduler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(JobsScheduler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(JobsScheduler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(JobsScheduler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(JobsScheduler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(JobsScheduler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //System.err.println(Jobs.size());

    }

    public synchronized WorkData getWork() {

        System.err.println("getting job ");
        //System.err.println("queue size"+ Jobs.size());
        WorkData res = null;
        while (res == null) {
            Iterator<JobManagerInterface> it = Jobs.iterator();

            while (it.hasNext()) {
                JobManagerInterface element = it.next();
                //System.err.println("trying this " + element.getJobId());
                int status = element.getStatus();
                if (status != element.COMPLETED) {
                    res = element.getWork();
                    if (res != null) {
                        //System.err.println("new job " + res.thrSolver.getJobId());
                        return res;
                    }
                }

            }
            try {
                Thread.sleep(1000);
                //System.err.println("no job, waiting");
            } catch (InterruptedException ex) {
                Logger.getLogger(JobsScheduler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return null;
    }

}
