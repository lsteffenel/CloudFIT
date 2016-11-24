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
import cloudfit.storage.DHTStorageUnit;
import cloudfit.storage.FileContainer;
import cloudfit.util.Number160;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basically a container for Jobs array (and subsequent Tasks status/results).
 * This container is used to simplify serialization and transfer to new nodes
 * that join the community (state transfer)
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class JobsContainer extends Thread implements Serializable {

    private ArrayList<JobManagerInterface> Jobs = null;
    private int jobCounter = 0;

    /**
     * Constructor. Initializes the jobs array
     */
    public JobsContainer() {
        this.setName("JobsContainer");
        this.Jobs = new ArrayList<JobManagerInterface>();
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

    public int getJobCounter() {
        return jobCounter;
    }

    public void remove(JobMessage obj) {
        JobManagerInterface js = this.getJob(obj.getJobId());
        this.Jobs.remove(js);
    }

    public void addJob(JobMessage obj, ServiceInterface comm) {
        System.err.println("new job " + ((JobMessage) obj).getJobId());
        Number160 jobId = obj.getJobId();

        if (obj.getJar() == null) { // regular submission without a jar
            ApplicationInterface jobClass = obj.getJobClass();
            String[] jobargs = obj.getArgs();
            Properties reqs = obj.getReqs();

            JobManagerInterface TS = TheBigFactory.getThreadSolve(comm, jobId, jobClass, jobargs, reqs);
            if (obj.getData() != null) {
                //System.err.println("---->>>>> Accepting "+((CopyOnWriteArrayList<TaskStatus>)obj.getData()).size());
                TS.setTaskList(obj.getData());
            }
            TS.setOriginalMsg(obj);

            this.Jobs.add(TS);
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
                    
                    JobManagerInterface TS = TheBigFactory.getThreadSolve(comm, jobId, jobClass, jobargs, reqs);
                    if (obj.getData() != null) {
                        TS.setTaskList(obj.getData());

                    }
                    TS.setOriginalMsg(obj);
                    this.Jobs.add(TS);
                    
                }
                else {System.out.println("Jar file not found");}
           } catch (ClassNotFoundException ex) {
                Logger.getLogger(JobsContainer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(JobsContainer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(JobsContainer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(JobsContainer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(JobsContainer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(JobsContainer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(JobsContainer.class.getName()).log(Level.SEVERE, null, ex);
           } catch (IOException ex) {
                Logger.getLogger(JobsContainer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void run() {
        while (true) {
            Iterator<JobManagerInterface> it = Jobs.iterator();
            while (it.hasNext()) {
                JobManagerInterface element = it.next();
                if (element.getStatus() == element.NEW) {
                    Thread toto = new Thread(element);
                    toto.start();
                    try {
                        // wait until completion or just jump out if the requirements don't match
                        while (element.getStatus() != element.COMPLETED && element.getStatus() != element.NOMATCH) {
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(JobsContainer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                }
            }
            try {

                Thread.sleep(1000);

            } catch (InterruptedException ex) {
                Logger.getLogger(JobsContainer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
