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
package cloudfit.core;

import java.io.Serializable;

/**
 * Generic definition of protocol-layer (ORB) methods.
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public interface ORBInterface {

    /**
     * Method to send a message to the next node in the P2P overlay (ring, tree,
     * etc)
     *
     * @param obj the message
     */
    public void sendNext(Message obj);

    /**
     * Method to send a message to the previous node in the P2P overlay (ring,
     * tree, etc)
     *
     * @param obj the message
     */
    public void sendPrev(Message obj);

    /**
     * Method to send a message to all nodes in the P2P overlay
     *
     * @param obj the message
     */
    public void sendAll(Message obj, boolean metoo);

    /**
     * Method to send a message to another Service in the same machine (inner
     * communication)
     *
     * @param obj the message
     */
    public void sendLocal(Message obj);

    /**
     * Method to write a data entry on the storage
     *
     * @param key
     * @param value
     */
    public void save(Serializable value, Serializable... keys);

    /**
     * Method to write a data entry on the storage, waiting to return
     *
     * @param keys
     * @param value
     */
    public void blocking_save(Serializable value, Serializable... keys);

    /**
     * Method to read a data from the storage
     *
     * @param keys
     * @return the data corresponding to the key
     */
    public Serializable read(Serializable... keys);

    /**
     * Method to check if a data is on local storage
     *
     * @param keys
     * @return boolean (True if local)
     */
    public boolean contains(Serializable... keys);

    /**
     * Removes the value under the key identification from the storage
     *
     * @param keys the key that identifies the data to be stored
     */
    public void remove(Serializable... keys);

    public String getPeerID();
}
