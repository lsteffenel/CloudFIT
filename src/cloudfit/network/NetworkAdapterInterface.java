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
package cloudfit.network;

import cloudfit.core.Message;

/**
 * Generic definition of Network layer (P2P) access methods
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public interface NetworkAdapterInterface {

    /**
     * Method to send a message to the next node in the P2P overlay (ring, tree,
     * etc)
     *
     * @param msg the message
     */
    public void sendNext(Message msg);

    /**
     * Method to send a message to the previous node in the P2P overlay (ring,
     * tree, etc)
     *
     * @param msg the message
     */
    public void sendPrev(Message msg);

    /**
     * Method to send a message to all nodes in the P2P overlay
     *
     * @param msg the message
     */
    public void sendAll(Message msg, boolean metoo);

}
