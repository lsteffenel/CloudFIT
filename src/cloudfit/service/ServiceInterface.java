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

import java.io.Serializable;


/**
 * General interface for Service-level blocks. 
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public interface ServiceInterface {

    /**
     * Method called when there is a new message
     * @param obj 
     */
    public void notify(Serializable obj);

    /**
     * Get this service ID
     * @return 
     */
    public long getProcessId();

    /**
     * Puts a message in the waiting queue, prior to <code>notify(obj)</code>
     * @param obj 
     */
    public void put(Serializable obj);
    
    /**
     * Method to send a message via the protocol level
     * @param msg 
     */
    public void send (Serializable msg);
    
    /**
     * Method to send a broadcast message via the protocol level
     * @param msg 
     */
    public void sendAll (Serializable msg, boolean metoo);
    
    /**
     * Method to write a data entry on the storage
     * @param key
     * @param value
     */
    public void save(Serializable value, String...keys);
    
    /**
     * Method to read a data from the storage
     * @param key
     * @return the data corresponding to the key
     */
    public Serializable read(String...key) ;
    
    /**
     * Removes the value under the key identification from the storage
     *
     * @param key the key that identifies the data to be stored
     */
    public void remove(String...key); 
}
