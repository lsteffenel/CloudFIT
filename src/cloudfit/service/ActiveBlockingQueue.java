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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class associated to a service block (like @see Community), stores incoming
 * messages and notifies the service through an Observer pattern.
 *
 * does not uses locks anymore as the LinkedBlockingQueue provides this already
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class ActiveBlockingQueue extends Thread {

    private LinkedBlockingQueue messageList;
    private String processId = "0";
    private ServiceInterface CApp = null;

    /**
     * Constructor of the ActiveBlockingQueue class.
     *
     * @param processId Id of the corresponding Service (used by CoreOrb to
     * multiplex messages)
     * @param CApp The associated service
     */
    public ActiveBlockingQueue(String processId, ServiceInterface CApp) {
        this.setName("ActiveBlockingQueue proc " + processId);
        this.setPriority(Thread.NORM_PRIORITY + 1);
        messageList = new LinkedBlockingQueue();
        this.processId = processId;
        this.CApp = CApp;
    }

    /**
     * Adds a message to the messageList queue
     *
     * @param obj the message
     */
    public void put(Serializable obj) throws InterruptedException {
        messageList.put(obj);

    }

    /**
     * Returns one message in the MessageList queue. If the queue is empty,
     * blocks waiting for a message
     *
     * @return message from the list
     */
    public Serializable get() throws InterruptedException {
        try {
            return (Serializable) messageList.take();
        } catch (InterruptedException ex) {
            Logger.getLogger(ActiveBlockingQueue.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Returns the size of the MessageList queue (how many messages are waiting)
     *
     * @return the size of the messageList
     */
    public int size() {
        return messageList.size();
    }

    public void run() {
        // TODO: use while (running) and a "disable" method    
        while (true) {
            try {
                CApp.notify(this.get());
                //System.err.println("Queue size = "+messageList.size());
            } catch (InterruptedException ex) {
                Logger.getLogger(ActiveBlockingQueue.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
