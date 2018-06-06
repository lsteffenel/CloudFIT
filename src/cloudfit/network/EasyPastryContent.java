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
import easypastry.cast.CastContent;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;

/**
 * Envelope to send messages through EasyPastry->Pastry
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class EasyPastryContent extends CastContent {

    private Message msg;
    private NodeHandle dest;

    /**
     * Constructor of the class.
     *
     * @param subject the "identifier" required by EasyPastry, @see
     * EasyPastryAdapter
     * @param msg the protocol layer message to encapsulate on EasyPastry
     * messages
     */
    public EasyPastryContent(String subject, Message msg) {
        super(subject);
        this.msg = msg;
        this.dest = null;
    }

    /**
     * Obtains destination of the message
     *
     * @return the NodeHandle that identifies the destination
     */
    public NodeHandle getDest() {
        return dest;
    }

    /**
     * Sets the destination of the message
     *
     * @param dest The NodeHandle that corresponds to the destination
     */
    public void setDest(NodeHandle dest) {
        this.dest = dest;
    }

    /**
     * Gets the content of the EasyPastry message
     *
     * @return the protocol-level message
     */
    public Message getContent() {
        return msg;
    }
}
