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
 * Class associated to a service block (like @see Community), stores incoming messages and 
 * notifies the service through an Observer pattern.
 * 
 * uses locks/conditions from http://docs.oracle.com/javase/1.5.0/docs/api/java/util/concurrent/locks/Condition.html
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class ActiveBlockingQueue extends Thread {

    private LinkedBlockingQueue messageList;
    private long processId = 0;
    private ServiceInterface CApp = null;
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    /**
     * Constructor of the ActiveBlockingQueue class.
     *
     * @param processId Id of the corresponding Service (used by CoreOrb to multiplex messages)
     * @param CApp The associated service
     */
    public ActiveBlockingQueue(long processId, ServiceInterface CApp) {
        this.setName("ActiveBlockingQueue proc "+processId);
        this.setPriority(Thread.NORM_PRIORITY + 1);
        messageList = new LinkedBlockingQueue();
        this.processId = processId;
        this.CApp = CApp;
        //this.start();
    }

    /**
     * Adds a message to the messageList queue
     * 
     * @param obj the message
     */
    public void put(Serializable obj) throws InterruptedException {
        lock.lock();
        try {
            messageList.put(obj);
            notEmpty.signalAll();
            //System.err.println("putting ("+ messageList.size() +") "+obj.getClass());
        } finally {
            lock.unlock();
        }
        //System.err.println("putting"+obj.getClass());
    }

    /**
     * Returns one message in the MessageList queue. If the queue is empty, blocks waiting for a message
     * 
     * @return message from the list
     */
    public Serializable get() throws InterruptedException {
        lock.lock();
        try {
            while (messageList.isEmpty()) {
                notEmpty.await();
                //System.err.println("awakening");
            }
            try {
                return (Serializable) messageList.take();
            } catch (InterruptedException ex) {
                Logger.getLogger(ActiveBlockingQueue.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } finally {
            lock.unlock();
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
