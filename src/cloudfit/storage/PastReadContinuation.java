/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.storage;

import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import rice.Continuation;
import rice.p2p.past.ContentHashPastContent;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class PastReadContinuation implements Continuation<DHTContent, Exception> {

    //private MutableContent res = null; //new DHTPastContent(HashUtils.generateHash("fail"),new String(""));;
    private DHTContent res = null;
    private boolean answered = false;
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    @Override
    public void receiveResult(DHTContent result) {
        lock.lock();
        try {
            res = result;
            answered = true;
            if (res != null) {
                System.err.println("sucessfully read from DHT" + res);
            } else {
                System.err.println("found no entry on the DHT");
            }
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void receiveException(Exception result) {

        lock.lock();
        try {
            //res = new DHTPastContent(HashUtils.generateHash("fail"),new String(""));
            System.err.println("failed read from DHT");
            answered = true;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public Serializable get() throws InterruptedException {
        lock.lock();
        try {

            while (answered == false) {
                notEmpty.await();
            }

        } finally {
            lock.unlock();
        }
        if (res != null) {

            return ((DHTContent) res).getContent();

        } else {
            return null;
        }
    }

    /**
     * hasKey - performs key lookup to identify if the key already exists, but
     * returns no data
     *
     * @return boolean
     * @throws InterruptedException
     */
    public boolean hasKey() throws InterruptedException {
        lock.lock();
        try {

            while (answered == false) {
                notEmpty.await();
            }

        } finally {
            lock.unlock();
        }
        if (res != null) {
            return true;
        } else {
            System.err.println("has no key");
            return false;
        }
    }
}
