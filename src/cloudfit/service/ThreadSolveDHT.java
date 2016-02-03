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
 * This subclass of ThreadSolve uses the DHT as intermediate data storage. 
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class ThreadSolveDHT extends ThreadSolve {
    

    
    public ThreadSolveDHT(ServiceInterface service, Number160 jobId, ApplicationInterface jobClass, String[] args) {
        super(service, jobId, jobClass, args);
    }
    
    /**
     * This method splits the original result message in two parts: a notification message sent to the peers
     * and the data that is stored into the storage (DHT or distributed storage, not local)
     * the data is stored under the id "jobId-taskId"
     * @param msg the taskresult message
     */
    @Override
    public void sendAll(Serializable msg, boolean metoo) {
        TaskStatusMessage fullMsg = (TaskStatusMessage)msg;
        Serializable tkResult = new String("DHT");
        TaskStatusMessage notifyMsg = new TaskStatusMessage(fullMsg.getJobId(), fullMsg.getTaskId(), fullMsg.getStatus(), tkResult);

        super.sendAll(notifyMsg, metoo);
    }
    
 
}
