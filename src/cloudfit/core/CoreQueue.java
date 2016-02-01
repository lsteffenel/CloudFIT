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

import cloudfit.service.ServiceInterface;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Active queue associated to the Protocol-level ORB, receives messages from
 * the network level and deliver them to the service level through the Observer pattern
 * @author steffenel 
 * uses locks/conditions from http://docs.oracle.com/javase/1.5.0/docs/api/java/util/concurrent/locks/Condition.html
 */
public class CoreQueue extends Thread {

    private LinkedBlockingQueue<Message> messageList;
    private HashMap SvcQueues = null;
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    /**
     * Class constructor
     */
    public CoreQueue() {
        //System.err.println(Thread.MIN_PRIORITY + " - "+ Thread.NORM_PRIORITY + " - "+Thread.MAX_PRIORITY);
        this.setPriority(Thread.NORM_PRIORITY + 1);
        this.setName("CoreQueue");
        SvcQueues = new HashMap();
        messageList = new LinkedBlockingQueue();
        //this.start();
    }

    /**
     * Used to subscribe a service to notifications (multiplexed by the service identification)
     * @param svc the Service that wants to subscribe to notifications
     */
    public void subscribe(ServiceInterface svc) {
        SvcQueues.put(svc.getProcessId(), svc);
    }

    /**
     * Used to unsubscribe from the notifications
     * @param svc the service that wants to unsubscribe
     * @return true if the unsubscribe succeeded
     */
    public boolean unsubscribe(ServiceInterface svc) {
        if (SvcQueues.remove(svc.getProcessId()) != null) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Method called by the low-level (network) to add a message to the list. If the queue is empty, wakes-up the attendees
     * @param obj the message to put in the messageList
     * @throws InterruptedException if something breaks the lock
     */
    public void put(Message obj) throws InterruptedException {
        lock.lock();
        try {
            messageList.put(obj);
            notEmpty.signal();
            //System.err.println("OrbQueue: in msg"+obj.content.getClass());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Method used by this thread to get a message to deliver
     * @return the message from the MessageList. If the list is empty, blocks waiting for a message
     * @throws InterruptedException  if something breaks the lock
     */
    private Message get() throws InterruptedException {
        lock.lock();
        try {
            while (messageList.isEmpty()) {
                notEmpty.await();
            }
            try {
                //System.err.println("OrbQueue get size = "+messageList.size());
                //Message toto = messageList.take();
                //System.err.println("OrbQueue: in msg"+toto.content.getClass());
                return messageList.take();
                //return toto;
            } catch (InterruptedException ex) {
                Logger.getLogger(CoreQueue.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        // TODO: use while (running) and a "disable" method    
        while (true) {
            try {
                Message msg = this.get();
                //System.err.println("sending to the right service"+msg.dest);
                if (SvcQueues.containsKey(msg.dest)) {
                    ((ServiceInterface) SvcQueues.get(msg.dest)).put(msg.content);
                    //System.err.println("sending to the right service"+msg.dest);
                }


            } catch (InterruptedException ex) {
                Logger.getLogger(CoreQueue.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
