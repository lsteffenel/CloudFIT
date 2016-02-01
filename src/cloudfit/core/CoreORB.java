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
 * Implementation of the Protocol-level interface. Associates to a 
 * message list in order to multiplex incoming messages and deliver them to the
 * corresponding Service classes
 * 
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class CoreORB implements ORBInterface {

    private CoreQueue cq = null;
    private NetworkAdapterInterface P2P = null;
    private StorageAdapterInterface storage = null;
    private StorageAdapterInterface localStorage = storage;

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
     * Used by the Service layer classes (Community, for example) to subscribe to CoreQueue notifications
     *
     * @param ai the Service layer class.
     */
    public void subscribe(ServiceInterface ai) {
        cq.subscribe(ai);
    }
    
     /**
     * Used by the Service layer classes (Community, for example) to unsubscribe from CoreQueue notifications
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
     * Used to define the local storage adapter
     * if this no local storage, use the default storage
     * @param storage the storage implementation
     */
    public void setLocalStorage(StorageAdapterInterface storage) {
        this.localStorage = storage;
    }
    
    // Storage methods
    
    /**
     * Saves the value under the key identification
     *
     * @param key the key that identifies the data to be stored
     * @param value the value to store
     */
    @Override
    public void save(String key, Serializable value, boolean mutable) {
        save(key, value, mutable, false); 
    }
    
    /**
     * Saves the value under the key identification
     *
     * @param key the key that identifies the data to be stored
     * @param value the value to store
     */
    @Override
    public void blocking_save(String key, Serializable value, boolean mutable) {
        blocking_save(key, value, mutable ,false); 
    }
    
    /**
     * Saves the value under the key identification
     *
     * @param key the key that identifies the data to be stored
     * @param value the value to store
     * @param local identify if using local storage or other (if both are different)
     */
    public void save(String key, Serializable value, boolean mutable, boolean local) {
        if (storage != null) {
            if (local) {
                localStorage.save(key, value, mutable);
            }
            else {
                storage.save(key,value, mutable);
            }
        }
    }
    
    /**
     * Saves the value under the key identification
     *
     * @param key the key that identifies the data to be stored
     * @param value the value to store
     * @param local identify if using local storage or other (if both are different)
     */
    public void blocking_save(String key, Serializable value, boolean mutable, boolean local) {
        if (storage != null) {
            if (local) {
                localStorage.save(key, value, mutable);
            }
            else {
                storage.blocking_save(key,value, mutable);
                //storage.save(key,value, mutable);
            }
        }
    }
    
    
    /**
     * Retrieves the value under the key identification
     *
     * @param key the key that identifies the data to be read
     * @return the value if it exists, or null
     */
    @Override
    public Serializable read(String key) {
        return read(key,false);
    }
    
    public Serializable read (String key, boolean local){
        if (storage != null) {
            if (local)
            {
                return localStorage.read(key);
            }
            else
            {
                return storage.read(key);
            }
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
    public void remove(String key) 
    {
        remove(key,false);
    }
    
    public void remove (String key,boolean local)
    {
        if (storage != null) {
            if (local)
            {
                localStorage.remove(key);
            }
            else
            {
                storage.remove(key);
            }
        }
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendAll(Message obj) {
        //System.err.println("sending all");
        P2P.sendAll(obj);
    }

    @Override
    public void sendLocal(Message obj) {
        try {
            cq.put(obj);
        } catch (InterruptedException ex) {
            Logger.getLogger(CoreORB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
}
