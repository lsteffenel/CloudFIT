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
import cloudfit.storage.StorageAdapterInterface;
import easypastry.cast.CastContent;
import easypastry.cast.CastHandler;
import easypastry.cast.CastListener;
import easypastry.core.PastryConnection;
import easypastry.core.PastryKernel;
import easypastry.dht.DHTException;
import easypastry.dht.DHTHandler;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.NodeHandle;

/**
 * Implementation of an adapter to the Pastry P2P overlay, using the EasyPastry
 * library EasyPastry also includes a Bunshin DHT adapter, so this class
 * exceptionnally implements both
 *
 * @see NetworkAdapterInterface and
 * @see StorageAdapterInterface
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class EasyPastryAdapter implements NetworkAdapterInterface, StorageAdapterInterface, CastListener {

    private PastryConnection conn;
    private DHTHandler dht;
    private CastHandler cast;
    private String topic = "CloudFIT.univ-reims.fr";
    private String context = "CloudFIT.univ-reims.fr";
    private CoreQueue MsgQueue;
    
    public EasyPastryAdapter(CoreQueue queue, InetSocketAddress add) {
        try {
            if (add == null) {
                LANDiscoveryThread ldt = new LANDiscoveryThread();
                ldt.start();

//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(EasyPastryAdapter.class.getName()).log(Level.SEVERE, null, ex);
//                }
                add = ldt.findPeer(1000);
                //InetAddress bside = ldt.findPeer(1000);
                if (add == null) {
                    InetAddress bside = ldt.getInetAddress();
                    add = new InetSocketAddress(bside, 7777);
                }
                
            }
            this.MsgQueue = queue;
            this.initKBR(add.getHostName(), add.getPort(), "easypastry-config.xml", topic);
            //context = context.concat("/"+add.getHostName()+"_"+add.getPort());
            initDHT(context);
//            this.initCast(subject);
//            this.start();
            
        } catch (Exception ex) {
            Logger.getLogger(EasyPastryAdapter.class.getName()).log(Level.SEVERE, null, ex);
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
        cast.addDeliverListener(subject, this);

        conn.bootNode();

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

    /**
     * Inits the Bunshin DHT layer from EasyPastry
     *
     * @param context the "identifier"
     * @throws DHTException
     */
    public void initDHT(String context) {
        try {
            //System.err.println("ici");

            dht = PastryKernel.getDHTHandler(context);
            //System.err.println("ok");
        } catch (DHTException ex) {
            Logger.getLogger(EasyPastryAdapter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Return the DHTHandler used to save/read data from Bunshin DHT
     *
     * @return
     */
    public DHTHandler getDHTHandler() {
        return dht;
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
            Message element = ((EasyPastryContent) content).getContent();

            
                MsgQueue.put(element);
        } catch (InterruptedException ex) {
            Logger.getLogger(EasyPastryAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
            

    }

    @Override
    public void hostUpdate(NodeHandle nh, boolean joined) {
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
    public void sendNext(Message msg) {

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
    public void sendPrev(Message msg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Sends a message to all the nodes in the Pastry overlay, using Scribe
     *
     * @param msg the message
     */
    @Override
    public synchronized void sendAll(Message msg) {

        NodeHandle next = null;
        CastHandler cast = getCastHandler();
        EasyPastryContent toSend = new EasyPastryContent(topic, msg);

        cast.sendMulticast(topic, toSend);
    }

    // StorageAdapterInterface methods
    /**
     * Method to save the data using the Bunshin DHT
     *
     * @param key the identifier of the data
     * @param value the value
     */
    @Override
    public void save(String key, Serializable value, boolean mutable) {
        try {
            dht.put(key, value);


        } catch (DHTException ex) {
            Logger.getLogger(EasyPastryAdapter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Method to save the data using the Bunshin DHT - does not block, finally
     *
     * @param key the identifier of the data
     * @param value the value
     */
    @Override
    public void blocking_save(String key, Serializable value, boolean mutable) {
        try {
            dht.put(key, value);


        } catch (DHTException ex) {
            Logger.getLogger(EasyPastryAdapter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method to read the data from the Bunshin DHT
     *
     * @param key the data identifier
     * @return the data, or null if nothing under this key
     */
    @Override
    public Serializable read(String key) {
        try {
            return dht.get(key);


        } catch (DHTException ex) {
            Logger.getLogger(EasyPastryAdapter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Method to remove a data from the Bunshin DHT (as it allows it)
     *
     * @param key the data identifier
     */
    @Override
    public void remove(String key) {
        try {
            dht.remove(key);


        } catch (DHTException ex) {
            Logger.getLogger(EasyPastryAdapter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
}