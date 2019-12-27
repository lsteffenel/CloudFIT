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

import cloudfit.network.NetworkAdapterInterface;
import cloudfit.service.ServiceInterface;
import cloudfit.storage.StorageAdapterInterface;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the Protocol-level interface. Associates to a message list
 * in order to multiplex incoming messages and deliver them to the corresponding
 * Service classes
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class CoreORB implements ORBInterface {

    private CoreQueue cq = null;
    private NetworkAdapterInterface P2P = null;
    private StorageAdapterInterface storage = null;

    /**
     * Associates a CoreQueue object to this CoreORB
     *
     * @param core CoreQueue object
     */
    public void setQueue(CoreQueue core) {
        this.cq = core;
    }

    /**
     * Associates a Network implementation (child of NetworkInterface)
     *
     * @param net a Network implementation object
     */
    public void setNetworkAdapter(NetworkAdapterInterface net) {
        this.P2P = net;
    }

    /**
     * Used by the Service layer classes (Community, for example) to subscribe
     * to CoreQueue notifications
     *
     * @param ai the Service layer class.
     */
    public void subscribe(ServiceInterface ai) {
        cq.subscribe(ai);
    }

    /**
     * Used by the Service layer classes (Community, for example) to unsubscribe
     * from CoreQueue notifications
     *
     * @param ai the Service layer class.
     */
    public void unsubscribe(ServiceInterface ai) {
        cq.unsubscribe(ai);
    }

    /**
     * Used to define the storage adapter
     *
     * @param storage the storage implementation
     */
    public void setStorage(StorageAdapterInterface storage) {
        this.storage = storage;
    }

    /**
     * Saves the value under the key identification
     *
     * @param keys the key that identifies the data to be stored
     * @param value the value to store
     */
    @Override
    public void save(Serializable value, Serializable... keys) {
        if (storage != null) {
            storage.save(value, keys);

        }
    }

    /**
     * Saves the value under the key identification
     *
     * @param keys the key that identifies the data to be stored
     * @param value the value to store
     */
    @Override
    public void blocking_save(Serializable value, Serializable... keys) {
        if (storage != null) {
            storage.blocking_save(value, keys);
            //storage.save(key,value, mutable);

        }
    }

    /**
     * Retrieves the value under the key identification
     *
     * @param key the key that identifies the data to be read
     * @return the value if it exists, or null
     */
    @Override
    public Serializable read(Serializable... key) {
        if (storage != null) {
            return storage.read(key);

        } else {
            return null;
        }
    }

    /**
     * Removes the value under the key identification from the storage
     *
     * @param key the key that identifies the data to be stored
     */
    @Override
    public void remove(Serializable... key) {
        if (storage != null) {
            storage.remove(key);

        }
    }

    @Override
    public boolean contains(Serializable... keys) {
        if (storage != null) {
            return storage.contains(keys);

        }
        return false;
    }

    /* ORBInterface methods
     * 
     */
    @Override
    public synchronized void sendNext(Message obj) {
        P2P.sendNext(obj);
    }

    @Override
    public void sendPrev(Message obj) {
        P2P.sendPrev(obj);
    }

    @Override
    public void sendAll(Message obj, boolean metoo) {
        P2P.sendAll(obj, metoo);
    }

    @Override
    public void sendLocal(Message obj) {
        try {
            cq.put(obj);
        } catch (InterruptedException ex) {
            Logger.getLogger(CoreORB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getPeerID() {
        return P2P.getPeerID();
    }

}
