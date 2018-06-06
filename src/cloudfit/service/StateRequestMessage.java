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
import java.net.InetSocketAddress;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class StateRequestMessage implements Serializable {

    //private InetSocketAddress sin = null;
    private Number160 JobID = null;

    public StateRequestMessage(Number160 JobID) {
        this.JobID = JobID;
    }

//    public StateRequestMessage(InetSocketAddress add, int JobID) {
//        sin = add;
//        this.JobID = JobID;
//    }
    /**
     * @return the sin
     */
//    public InetSocketAddress getSin() {
//        return sin;
//    }
    /**
     * @param sin the sin to set
     */
//    public void setSin(InetSocketAddress sin) {
//        this.sin = sin;
//    }
    /**
     * @return the JobID
     */
    public Number160 getJobID() {
        return JobID;
    }

    /**
     * @param JobID the JobID to set
     */
    public void setJobID(Number160 JobID) {
        this.JobID = JobID;
    }

}
