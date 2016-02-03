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

/**
 * Message to insert a new job
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class JobMessage implements Serializable {

    private Number160 njobId = null;
    private String[] args = null;
    private ApplicationInterface jobClass = null;
    private Serializable data = null;
    private String jar = null;
    private boolean delete = false;

    public String getJar() {
        return jar;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }
    private String app = null;

    /**
     * Constructor of this class.
     *
     * @param jobId the ID of the job
     * @param obj the class of the application to run
     * @param args the application arguments
     */
    public JobMessage(Number160 jobId, ApplicationInterface obj, String[] args) {
        this.njobId = jobId;
        this.args = args;
        this.jobClass = obj;
    }

    public JobMessage(Number160 jobId, String jar, String app, String[] args) {
        this.njobId = jobId;
        this.jar = jar;
        this.app = app;
        this.args = args;
    }

    public JobMessage(Number160 jobId, boolean delete) {
        this.njobId = jobId;
        this.delete = delete;
    }

    boolean isDelete() {
        return this.delete;
    }

    /**
     * Gets the Job ID
     *
     * @return the jobId
     */
    public Number160 getJobId() {
        return njobId;
    }

    /**
     * Sets the Job ID
     *
     * @param id
     */
    public void setJobId(Number160 id) {
        this.njobId = id;
    }

    /**
     * Gets the Job arguments
     *
     * @return
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Gets the Job class to run
     *
     * @return
     */
    public ApplicationInterface getJobClass() {
        return this.jobClass;
    }

    public void setJobClass(ApplicationInterface obj) {
        this.jobClass = obj;
    }

    public Serializable getData() {
        return data;
    }

    public void setData(Serializable data) {
        this.data = data;
    }

}
