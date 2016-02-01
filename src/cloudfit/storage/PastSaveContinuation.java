/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.storage;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import rice.Continuation;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class PastSaveContinuation implements Continuation<Boolean[], Exception> {

    private boolean finished = false;
    private boolean success = false;
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    
    @Override
    public void receiveResult(Boolean[] results) {
        
        lock.lock();
        try {
           
            int numSuccessfulStores = 0;
                for (int ctr = 0; ctr < results.length; ctr++) {
                    if (results[ctr].booleanValue()) {
                        numSuccessfulStores++;
                    }
                }
                System.err.println(" successfully stored at "+ numSuccessfulStores + " locations.");
            
            finished = true;
            success = true;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void receiveException(Exception result) {
        
        
        lock.lock();
        try {
            finished = true;
            success = false;
            System.err.println("Error storing... a big problem ");
            result.printStackTrace();
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    
    public boolean wait_save() throws InterruptedException {
        lock.lock();
        try {
        
            while (finished == false) {
                //System.err.println("waiting");
                notEmpty.await();
            }
                System.err.println("done "+success);
//      
        } catch (InterruptedException ex) {
            // interrupted, probably because the thread was "terminated"
        } finally {
            lock.unlock();
        }
        return success;
    }
}
