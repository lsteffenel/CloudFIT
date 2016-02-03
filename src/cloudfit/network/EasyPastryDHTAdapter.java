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

import cloudfit.core.CoreQueue;
import cloudfit.core.Message;
import cloudfit.service.Community;
import cloudfit.service.JobMessage;
import cloudfit.service.TaskStatusMessage;
import cloudfit.storage.ImmutableContent;
import cloudfit.storage.MutableContent;
import cloudfit.storage.PastReadContinuation;
import cloudfit.storage.PastSaveContinuation;
import cloudfit.storage.StorageAdapterInterface;
import cloudfit.util.HashUtils;
import easypastry.cast.CastContent;
import easypastry.cast.CastHandler;
import easypastry.cast.CastListener;
import easypastry.core.PastryConnection;
import easypastry.core.PastryKernel;
import easypastry.dht.DHTException;
import java.io.Serializable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mpisws.p2p.transport.multiaddress.MultiInetSocketAddress;
import org.mpisws.p2p.transport.priority.PriorityTransportLayer;
import rice.Continuation;
import rice.Executable;
import rice.environment.Environment;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.past.Past;
import rice.p2p.past.PastImpl;
import rice.pastry.PastryNode;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.persistence.LRUCache;
import rice.persistence.MemoryStorage;
import rice.persistence.PersistentStorage;
import rice.persistence.Storage;
import rice.persistence.StorageManagerImpl;

/**
 * Implementation of an adapter to the Pastry P2P overlay, using the EasyPastry
 * library and AppSockets. EasyPastry also includes a Bunshin DHT adapter, so
 * this class exceptionnally implements both
 *
 * @see NetworkAdapterInterfaceand
 * @see StorageAdapterInterface
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class EasyPastryDHTAdapter implements NetworkAdapterInterface, StorageAdapterInterface, CastListener {

    private PastryConnection conn;
    private Environment environment;
    private Past dht;
    private CastHandler cast;
    private String topic = "CloudFIT.univ-reims.fr";
    private String jobs = "CloudFIT-JOBS";
    private String context = topic;
    private CoreQueue MsgQueue;
    //private ReqServer rs = null;
    private Community community = null;
    InetSocketAddress add = null;
    //private Serialization serialTool;

    public EasyPastryDHTAdapter(CoreQueue queue, InetSocketAddress add, Community comm) {
        try {
            if (add == null) {
                LANDiscoveryThread ldt = new LANDiscoveryThread();
                ldt.start();

//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(EasyPastryDHTAdapter.class.getName()).log(Level.SEVERE, null, ex);
//                }
                add = ldt.findPeer(1000);
                //InetAddress bside = ldt.findPeer(1000);
                if (add == null) {
                    InetAddress bside = ldt.getInetAddress();
                    add = new InetSocketAddress(bside, 7777);
                }
            }
            this.community = comm;
            //serialTool = new Serialization();
            this.add = add;
            this.MsgQueue = queue;
            this.initKBR(this.add.getHostName(), this.add.getPort(), "easypastry-config.xml", topic);
            this.initDHT(context);
            conn.bootNode();

        } catch (Exception ex) {
            Logger.getLogger(EasyPastryDHTAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Inits the Key-Based-Routing overlay from FreePastry
     *
     * @param hostname a node to connect to
     * @param port the port of the node
     * @param filename the EasyPastry properties file
     * @param topic the "identifier" of the Scribe topic
     * @throws Exception
     */
    public void initKBR(String hostname, int port, String filename, String subject) throws Exception {
        PastryKernel.init(hostname, port, filename);
        conn = PastryKernel.getPastryConnection();

        cast = PastryKernel.getCastHandler();
        cast.subscribe(subject);
        cast.subscribe(jobs);
        cast.addDeliverListener(subject, this);
        cast.addDeliverListener(jobs, this);

    }

    /**
     * Returns the CastHandler used to send messages
     *
     * @return CastHandler
     */
    public CastHandler getCastHandler() {
        return cast;
    }

    /**
     * return the Endpoint used to open AppSockets (Pastry private channels)
     *
     * @return EndPoint
     */
    public Endpoint getEndpoint() {
        return cast.getEndpoint();
    }

    // CastListener methods
    @Override
    public boolean contentAnycasting(CastContent content) {
        System.err.println("Anycasting : " + content);
        return true;
    }

    @Override
    public void contentDelivery(CastContent content) {
        try {
            //System.err.println("Delivery : " + content.getSource());
            Message element = ((EasyPastryContent) content).getContent();

            // only TaskStatusMessages are intercepted here
            if (element.content.getClass() == TaskStatusMessage.class) {

                if (community.hasJob(((TaskStatusMessage) element.content).getJobId())) {
                    if (community.needData(((TaskStatusMessage) element.content).getJobId(), ((TaskStatusMessage) element.content).getTaskId())) {
                        //System.err.println("Sending it up!!!");
                        MsgQueue.put(element);
                    }
                } else { // unknown job, let the "Join" part work
                    MsgQueue.put(element);
                }
            } else {
                //System.err.println("Another msg incoming " + element.content.getClass());
                MsgQueue.put(element);
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(EasyPastryDHTAdapter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void hostUpdate(NodeHandle nh, boolean joined
    ) {
        if (joined) {
            System.err.println("Node join : " + nh);
        } else {
            System.err.println("Node leave : " + nh);
        }
    }

    // NetworkAdapterInterface methods
    /**
     * Method to send a message to the next node in the Pastry network Uses
     * EasyPastry to get a list of neighbors nodeHandlers, and send to the first
     * one If no neighbor is available, send to itself
     *
     * @param msg the message to send
     */
    @Override
    public void sendNext(Message msg
    ) {

        NodeHandle next;
        if (cast == null) {
            cast = getCastHandler();
        }
        // gets only one neighbor, with the list unordered (false)
        Collection<NodeHandle> nhs = cast.getNeighbours(1, false);
        if (!nhs.isEmpty()) {
            next = nhs.iterator().next();
        } else {
            next = cast.getLocalNodeHandle();
        }
        //System.err.println("start sending");
        cast.sendDirect(next, new EasyPastryContent(topic, msg));
        //System.err.println("sent");

    }

    /**
     * Sends a message to the previous node. Not implemented
     *
     * @param msg the message
     */
    @Override
    public void sendPrev(Message msg
    ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Sends a message to all the nodes in the Pastry overlay, using Scribe
     *
     * @param msg the message
     */
    @Override
    public void sendAll(Message msg, boolean meetoo) {

        NodeHandle next = null;
        CastHandler cast = getCastHandler();
        EasyPastryContent toSend = null;

        if (msg.content.getClass() == JobMessage.class) {
            //System.err.println("sending" + msg.content.getClass());
            toSend = new EasyPastryContent(jobs, msg);

            cast.sendMulticast(jobs, toSend);
        } else {
            toSend = new EasyPastryContent(topic, msg);

            cast.sendMulticast(topic, toSend);
        }
    }

//    public boolean needData(int jobId, int taskId) {
//        return community.needData(jobId, taskId);
//    }
//
//    public boolean hasData(int jobId, int taskId) {
//        return community.hasData(jobId, taskId);
//    }
//
//    public Serializable getData(int jobId, int taskId) {
//        return community.getData(jobId, taskId);
//    }
    /**
     * Inits the Past DHT layer from Pastry
     *
     * @param context the "identifier"
     * @throws DHTException
     */
    public void initDHT(String context) {
        try {
        //        
//       // used for generating DHTPastContent object Ids.
            // this implements the "hash function" for our DHT
            environment = conn.getEnvironment();
            PastryIdFactory idf = new rice.pastry.commonapi.PastryIdFactory(environment);
            Node node = conn.getNode();
            String storageDirectory = environment.getParameters().getString("DHT.storagedir");
            // create a different storage root for each node
            if (storageDirectory == null) {
                storageDirectory = "./storage-" + node.getId().hashCode();
            } else {
                storageDirectory = storageDirectory.concat("/storage-" + node.getId().hashCode());
            }

            // create the persistent part
            Storage stor = new PersistentStorage(idf, storageDirectory, 4 * 1024 * 1024 * 1024, node.getEnvironment());
//            Storage stor = new MemoryStorage(idf);
            dht = new PastImpl(node, new StorageManagerImpl(idf, stor, new LRUCache(
                    new MemoryStorage(idf), 100 * 1024 * 1024, node.getEnvironment())), 2, "CloudFIT-DHT");
//        dht = new GCPastImpl(node, new StorageManagerImpl(idf, stor, new LRUCache(
//                new MemoryStorage(idf), 100 * 1024 * 1024, node.getEnvironment())), 0, "CloudFIT-DHT",new PastPolicy.DefaultPastPolicy(),2000);
//      
//        
        } catch (IOException ex) {
            Logger.getLogger(EasyPastryDHTAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Return the DHTHandler used to save/read data from Past DHT
     *
     * @return
     */
    public Past getDHTHandler() {
        return dht;
    }

    private final Lock lock = new ReentrantLock();

    // StorageAdapterInterface methods
    /**
     * Method to save the data using the Past DHT
     *
     * @param key the identifier of the data
     * @param value the value
     */
    @Override
    public void save(String key, Serializable value, boolean mutable) {

        final String fkey = key;
        final Serializable fvalue = value;

        if (mutable) {
            environment.getProcessor().process(new Executable() {

                public Object execute() {
                    // Now you are on the processing thread, do processor intensive task, and return the result
                    save_mutable(fkey, fvalue);
                    //save_normal(key,value); 
                    return null;
                }

            }, new Continuation<Object, Exception>() {

                public void receiveResult(Object result) {
                // now you are on the freepastry thread, send the message or whatever, the result is what
                    // execute() returned
                }

                public void receiveException(Exception exception) {
                    // There was a problem
                }

            }, environment.getSelectorManager(), environment.getTimeSource(), environment.getLogManager());
        }
        else
        {
                        environment.getProcessor().process(new Executable() {

                public Object execute() {
                    // Now you are on the processing thread, do processor intensive task, and return the result
                    //save_mutable(fkey, fvalue);
                    save_normal(fkey,fvalue); 
                    return null;
                }

            }, new Continuation<Object, Exception>() {

                public void receiveResult(Object result) {
                // now you are on the freepastry thread, send the message or whatever, the result is what
                    // execute() returned
                }

                public void receiveException(Exception exception) {
                    // There was a problem
                }

            }, environment.getSelectorManager(), environment.getTimeSource(), environment.getLogManager());
        }

    }

    public synchronized void save_normal(String key, Serializable value) {
        if (dht == null) {
            initDHT(context);
        }

        Id keyId = HashUtils.generateHash(key);
        final ImmutableContent myContent = new ImmutableContent(keyId, value);

        dht.insert(myContent, new Continuation<Boolean[], Exception>() {
            // the result is an Array of Booleans for each insert

            public void receiveResult(Boolean[] results) {
                int numSuccessfulStores = 0;
                for (int ctr = 0; ctr < results.length; ctr++) {
                    if (results[ctr].booleanValue()) {
                        numSuccessfulStores++;
                    }
                }
                //System.err.println(myContent + " successfully distributed to "
                //        + numSuccessfulStores + " locations.");
            }

            public void receiveException(Exception result) {
                System.err.println("Error storing " + myContent);
                //result.printStackTrace();
            }

            //dht.put(key, value);
        });

    }

    public synchronized void save_mutable(String key, Serializable value) {
        if (dht == null) {
            initDHT(context);
        }

        Id keyId = HashUtils.generateHash(key);
        final MutableContent myContent = new MutableContent(keyId, value);

        Continuation cont = new PastSaveContinuation();

        //try {
        dht.insert(myContent, cont);
        //((PastSaveContinuation) cont).wait_save();

//        } catch (InterruptedException ex) {
//            Logger.getLogger(EasyPastryDHTAdapter.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    /**
     * Method to save the data using the Past DHT
     *
     * @param key the identifier of the data
     * @param value the value
     */
    @Override
    public void blocking_save(String key, Serializable value, boolean mutable) {

        final String fkey = key;
        final Serializable fvalue = value;
        lock.lock();
        //blocking_save_normal(key, value);
        //blocking_save_version(key, value); 
        if (mutable)
            blocking_save_mutable(fkey, fvalue);
        else
            blocking_save_normal(key, value);
        

        lock.unlock();
    }

    public void blocking_save_mutable(String key, Serializable value) {
        if (dht == null) {
            initDHT(context);
        }

        PastryNode pn = (PastryNode) conn.getNode();

        PriorityTransportLayer priority = (PriorityTransportLayer) pn.getVars().get(SocketPastryNodeFactory.PRIORITY_TL);

        Collection<MultiInetSocketAddress> coll = priority.nodesWithPendingMessages();

        Iterator<MultiInetSocketAddress> it = coll.iterator();
        while (it.hasNext()) {
            System.err.println(priority.queueLength(it.next()));
        }

        Id keyId = HashUtils.generateHash(key);
        final MutableContent myContent = new MutableContent(keyId, value);

        Continuation cont = new PastSaveContinuation();

        try {
            dht.insert(myContent, cont);
            ((PastSaveContinuation) cont).wait_save();

        } catch (InterruptedException ex) {
            Logger.getLogger(EasyPastryDHTAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void blocking_save_normal(String key, Serializable value) {
        if (dht == null) {
            initDHT(context);
        }

        Id keyId = HashUtils.generateHash(key);
        final ImmutableContent myContent = new ImmutableContent(keyId, value);

        Continuation cont = new PastSaveContinuation();

        try {
            dht.insert(myContent, cont);
            while (!((PastSaveContinuation) cont).wait_save()) {
                System.err.println("insert lost, retrying");
                cont = new PastSaveContinuation();
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(EasyPastryDHTAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Method to read the data from the Past DHT
     *
     * @param key the data identifier
     * @return the data, or null if nothing under this key
     */
    @Override
    public Serializable read(String key) {
        return read_normal(key);
        //return read_version(key);
    }

    public Serializable read_normal(String key) {
        try {
            if (dht == null) {
                initDHT(context);
            }
            Id lookupKey = HashUtils.generateHash(key);
            Continuation cont = new PastReadContinuation();
            dht.lookup(lookupKey, cont);

            return ((PastReadContinuation) cont).get();
        } catch (InterruptedException ex) {
            Logger.getLogger(EasyPastryDHTAdapter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Method to remove a data from the Past DHT (not allowed)
     *
     * @param key the data identifier
     */
    @Override
    public void remove(String key) {
//        try {
//            if (dht == null) {
//                initDHT(context);
//            }
//            dht.remove(key);
//
//        } catch (DHTException ex) {
//            Logger.getLogger(PastrySocketAdapter.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
