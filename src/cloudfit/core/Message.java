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
 * Defines a protocol-level message (encapsulates Service-level messages, transmitted inside a NetworkAdapter message)
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class Message implements Serializable {

    public String dest;
    public Serializable content;

    /**
     * Constructor of the class
     * @param dest the identifier of the destination at the Service level 
     * @param content the message to send
     */
    public Message(String dest, Serializable content) {
        this.dest = dest;
        this.content = content;
    }
}
