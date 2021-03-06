/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.network;

import cloudfit.core.CoreQueue;
import cloudfit.core.Message;
import cloudfit.storage.StorageAdapterInterface;
import cloudfit.util.PropertiesUtil;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.DSASignatureFactory;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.GetBuilder;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.PutBuilder;
import net.tomp2p.dht.RemoveBuilder;
import net.tomp2p.dht.StorageLayer;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.p2p.PostRoutingFilter;
import net.tomp2p.p2p.RequestP2PConfiguration;
import net.tomp2p.p2p.SlowPeerFilter;
import net.tomp2p.p2p.StructuredBroadcastHandler;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.replication.SlowReplicationFilter;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;
import net.tomp2p.storage.StorageDisk;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class TomP2PAdapter implements NetworkAdapterInterface, StorageAdapterInterface {

    private CoreQueue MsgQueue;
    //private ReqServer rs = null;
    //private Community community = null;
    //InetSocketAddress add = null;

    //final private Peer peer;
    private PeerDHT peer = null;
    private DB DBase = null;
    //private long seqnum = 0;
    //private Number160 id;

    static HashMap<Number160, TreeSet> history;

    PostRoutingFilter spf = null;

    List<PeerAddress> neighs = null;

    public Number160 getId() {
        return peer.peerID();
    }

    @Override
    public String getPeerID() {
        //return peer.peerID().toString(true);
        return peer.peerID().toString(false);
    }

//    public TomP2PAdapter(CoreQueue queue, InetSocketAddress add, Community comm) {
    public TomP2PAdapter(CoreQueue queue, InetSocketAddress add) {
        try {
            int port = 7777;
            String boundPort = PropertiesUtil.getProperty("port");
            if (boundPort != null) {

                if (boundPort.endsWith("auto")) {
                    //InetAddress address = Inet4Address.getByName("127.0.0.1");
                    System.err.println(Inet4Address.getByName("localhost").getHostName());
                    port = changeBoundPort(Inet4Address.getByName("localhost"), port);
                    System.err.println("new port = " + port);
                    Bindings b = new Bindings();
//            b.addInterface(iface.getDisplayName());
                } else {
                    try {
                        port = Integer.parseInt(boundPort);
                    } catch (NumberFormatException ex) {
                        port = 7777;
                    }
                }
            }
            //this.community = comm;
            //serialTool = new Serialization();
            //this.add = add;
            this.MsgQueue = queue;

//            if (add == null) {
//                System.err.println("add == null");
//                LANDiscoveryThread ldt = new LANDiscoveryThread();
//                ldt.start();
//
////                try {
////                    Thread.sleep(1000);
////                } catch (InterruptedException ex) {
////                    Logger.getLogger(EasyPastryDHTAdapter.class.getName()).log(Level.SEVERE, null, ex);
////                }
//                add = ldt.findPeer(1000);
//                //InetAddress bside = ldt.findPeer(1000);
//                if (add == null) {
//                    InetAddress bside = ldt.getInetAddress();
//                    System.err.println("add = null");
//                    add = new InetSocketAddress(bside, port);
//                }
//            }
            history = new HashMap<Number160, TreeSet>();

            //ConfigurationStore cs = Configurations.defaultStoreConfiguration();
            //Number160 id = Number160.createHash(((Double) Math.random()).toString());
            String sid = null;

            NetworkInterface iface = ifDetect();
            // if the code cannot find a suitable interface to contact Internet (like when offline)
            // we have iface=null. Therefore, the ID will be set without MAC and inetaddress
            if (iface != null) {
                //System.err.println(iface.getHardwareAddress() + " " + iface.getInetAddresses().nextElement().getHostAddress() + " ");
                //String sid =   iface.getHardwareAddress().toString() + " ";
                byte[] mac = iface.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    //System.err.println(iface.getDisplayName() + " " + sb.toString() );
                    sid = sb.toString() + " " + iface.getInetAddresses().nextElement().getHostAddress() + " " + port;
                } else {
                    sid = "noiface 00:00:00:00:00:00 " + port;

                }

            } else {
                sid = "noiface 00:00:00:00:00:00 " + port;

            }
            // Here we define our unique NumberID. If there is a way to "control" this to make it location-aware...
            // #TODO
            Number160 id = Number160.createHash(sid);
            System.err.println(sid + "\n" + id);

            String path = "/tmp/tom-" + id;

            String prop = PropertiesUtil.getProperty("DHTDir");
            if (prop != null) {
                path = prop.concat("/tom-" + id);
            }

            String maxsize = PropertiesUtil.getProperty("DHTSize");
            if (maxsize != null) {

            }
            //String path = "/tmp/tom";

            Bindings b = new Bindings().listenAny();

            PeerBuilderDHT pbd = new PeerBuilderDHT(new PeerBuilder(id).ports(port).bindings(b).start());

            String storageType = PropertiesUtil.getProperty("storage_method");
            if (storageType == null) {
                storageType = "disk";
            }

            if (storageType.equals("disk")) {

                File storagePath = new File(path);
                //System.err.println("Exists or not ? "+ storagePath.exists());
                if (!storagePath.exists()) {
                    //System.err.println("creating dir");
                    storagePath.mkdirs();
                }
                File base = new File(storagePath, "tomp2p_" + id);
                //DBase = DBMaker.newFileDB(base).transactionDisable().cacheSoftRefEnable().closeOnJvmShutdown().make();
                //DBase = DBMaker.newFileDB(base).transactionDisable().cacheSoftRefEnable().make();
                //DBase = DBMaker.newFileDB(base).cacheSoftRefEnable().make();
                //precedent 
                //DBase = DBMaker.newFileDB(base).mmapFileEnableIfSupported().cacheSoftRefEnable().make();
                DBase = DBMaker.newFileDB(base).mmapFileEnableIfSupported().cacheSoftRefEnable().closeOnJvmShutdown().make();
                // TomP2P test
                //DBase = DBMaker.newFileDB(base).transactionDisable().closeOnJvmShutdown().cacheDisable().make();
                //DBase = DBMaker.newFileDB(base).transactionDisable().cacheSoftRefEnable().compressionEnable().make();

                StorageDisk sd = new StorageDisk(DBase, id, storagePath, new DSASignatureFactory(), 1 * 1000);

                //StorageDisk sd = new StorageDisk(DBase, id, storagePath, new DSASignatureFactory(), 10 * 1000);
                pbd.storage(sd);

            } else {
                //Memory storage
                // no need to initialize
                // this is the default method when pbd.storage = null

            }
            // node start

            peer = pbd.start();

            String slow = PropertiesUtil.getProperty("slow");
            spf = new SlowPeerFilter();
            //slow="true";
            if (slow != null) {
                if (slow.equals("true")) {

                    System.err.println("changing slow to true");
                    PeerAddress pa = peer.peerAddress().changeSlow(true);
                    peer.peer().peerBean().serverPeerAddress(pa);

                }
            }

            // Explicit indirect replication
            System.err.println("Activating Indirect Replication");
            IndirectReplication IR = new IndirectReplication(peer);
            IR.addReplicationFilter(new SlowReplicationFilter());
            // Option : choose between a fixed replication factor or autoreplication
            // DEFAULT = replicationFactor(6)
            IR.replicationFactor(3);
            //IR.autoReplication();
            IR.start();

            //System.err.println("Slow = " + peer.peerAddress().isSlow());
            if (add != null) { // one cannot bootstrap to itself !!!
                System.err.println("discovery to " + add.getAddress() + " " + add.getPort());
                //FutureDiscover futureDiscover = peer.peer().discover().inetSocketAddress(add.getAddress(), add.getPort()).start();
                FutureDiscover futureDiscover = peer.peer().discover().expectManualForwarding().inetAddress(add.getAddress()).ports(add.getPort()).start();
                // Try to set up port forwarding with UPNP and NATPMP if peer is not reachable `
                //            PeerNAT peerNAT = new PeerBuilderNAT(peer.peer()).start();
                //            FutureNAT fn = peerNAT.startSetupPortforwarding(futureDiscover);
                //            //if port forwarding failed, this will set up relay peers
                //            FutureRelayNAT frn = peerNAT.startRelay(new TCPRelayClientConfig(), futureDiscover, fn);
                //            frn.awaitUninterruptibly();

                futureDiscover.awaitUninterruptibly(10000);
                if (futureDiscover.isSuccess()) {
                    System.err.println("found that my outside address is " + futureDiscover.peerAddress());
                } else {
                    //System.err.println(futureDiscover.failedReason());
                }

                // bootstrap
                System.err.println("trying to bootstrap to " + add.getAddress() + " " + add.getPort());
                FutureBootstrap futureBootstrap = this.peer.peer().bootstrap().inetAddress(add.getAddress()).ports(add.getPort()).start();
                futureBootstrap.awaitUninterruptibly();
                //System.err.println(futureBootstrap.isSuccess() + " " + futureBootstrap.isCompleted());
                if (futureBootstrap.isFailed()) {
                    //System.err.println(futureBootstrap.failedReason());
                }

                System.err.println("wait for maintenace ping");
                Thread.sleep(3000);
                System.err.println("peer knows: " + peer.peerBean().peerMap().all() + " unverified: "
                        + peer.peerBean().peerMap().allOverflow());

            }
            // delivery method

            peer.peer().objectDataReply(new ObjectDataReply() {
                @Override
                public Object reply(final PeerAddress sender, final Object request) throws Exception {

                    TomP2PMessage P2Pmsg = (TomP2PMessage) request;

                    /*
                     if (P2Pmsg.getType() == TomP2PMessage.BCAST) {
                     if (history.containsKey(P2Pmsg.getSenderId())) {
                     TreeSet ts = history.get(P2Pmsg.getSenderId());
                     if (ts.contains(P2Pmsg.getSequenceNumber())) {
                     // do nothing, already know
                     //System.err.println("do nothing, already have it" + P2Pmsg.getSenderId() + " (" + P2Pmsg.getSequenceNumber() + ")");
                     } else {
                     //System.err.println("unknown seq");
                     ts.add(P2Pmsg.getSequenceNumber());

                     bbcast(P2Pmsg);
                     }
                     } else {
                     //System.err.println("unknown unknown sender");
                     TreeSet ts = new TreeSet();
                     ts.add(P2Pmsg.getSequenceNumber());
                     history.put(P2Pmsg.getSenderId(), ts);
                     bbcast(P2Pmsg);

                     }
                     }*/
                    //System.err.println("msg arrived --> "+((Message)P2Pmsg.getContent()).content.getClass());
                    contentDelivery((Message) P2Pmsg.getContent());

                    return "ack";
                }

//                private void bbcast(TomP2PMessage P2Pmsg) {
//                    List<PeerAddress> neighs = peer.peerBean().peerMap().all();
//                    Iterator it = neighs.iterator();
//                    while (it.hasNext()) {
//                        PeerAddress p1 = (PeerAddress) it.next();
//                        //System.err.println(p1.toString());
//                        //peer.peer().sendDirect(p1).object("test").start();
//
//                        // send direct
//                        FutureDirect futureData;
//                        futureData = peer.peer().sendDirect(p1).object(P2Pmsg).start();
//                        // blocking send one by one... what about a listener after all sends were sent ??
////            futureData.awaitUninterruptibly();
//
//                    }
//                }
            });

            // Added hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    System.err.println("Shutting down peer");
                    if (DBase != null) {
                        if (!DBase.isClosed()) {
                            DBase.commit();
                            //DBase.getEngine().compact();
                            //DBase.commit();
                            //DBase.getEngine().clearCache();
                            DBase.close();
                        }

                    }
                    peer.shutdown();
                    System.err.println("Bye!");
                }
            });

        

        save(peer.peerAddress(), "vlan0", "peers-realm", "vlan0", getPeerID());
//    
        } catch (UnknownHostException ex) {
            Logger.getLogger(TomP2PAdapter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TomP2PAdapter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(TomP2PAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private NetworkInterface ifDetect() throws IOException {

        //System.setProperty("java.net.preferIPv4Stack", "true");
        NetworkInterface theOne = null;
        // iterate over the network interfaces known to java
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        OUTER:
        for (NetworkInterface interface_ : Collections.list(interfaces)) {
            // we shouldn't care about loopback addresses
            if (interface_.isLoopback()) {
                continue;
            }

            // if you don't expect the interface to be up you can skip this
            // though it would question the usability of the rest of the code
            if (!interface_.isUp()) {
                continue;
            }

            //System.err.println(interface_.getDisplayName());
            // iterate over the addresses associated with the interface
            Enumeration<InetAddress> addresses = interface_.getInetAddresses();
            for (InetAddress address : Collections.list(addresses)) {

                System.err.println(address.getHostAddress());
                // look only for ipv4 addresses
                if (address instanceof Inet6Address) {
                    continue;
                }

                // use a timeout big enough for your needs
//                    if (!address.isReachable(3000)) {
//                        System.err.println("problem reacheable");
//                        continue;
//                    }
                // java 7's try-with-resources statement, so that
                // we close the socket immediately after use
                SocketChannel socket = null;
                try {
                    socket = SocketChannel.open();
                    // again, use a big enough timeout
                    int timeout = 3000;
                    socket.socket().setSoTimeout(timeout);

                    // bind the socket to your local interface
                    socket.bind(new InetSocketAddress(address, 0));
                    System.err.println(socket.socket().getLocalPort());

                    // try to connect to *somewhere*
                    //socket.connect(new InetSocketAddress("cosy.univ-reims.fr", 80));
                    socket.socket().connect(new InetSocketAddress("cosy.univ-reims.fr", 80), timeout);
                } catch (IOException ex) {
                    //ex.printStackTrace();
                    continue;
                } catch (java.nio.channels.UnresolvedAddressException ex) {
                    //ex.printStackTrace();
                    continue;
                } finally {
                    socket.close();
                }

                //System.err.format("ni: %s, ia: %s\n", interface_, address);
                theOne = interface_;

                // stops at the first *working* solution
                break OUTER;
            }
        }
        return theOne;
    }
//

    private int changeBoundPort(InetAddress boothost, int port) {
        System.err.println("--------------Testing port " + port);
        ServerSocket s = null;
        boolean available = false;
        while (available == false) {
            try {
                s = new ServerSocket(port);
                //s.bind(port);
//                s = new ServerSocket(port);

                // If the code makes it this far without an exception it means
                // something is using the port and has responded.
                System.err.println("--------------Port " + port + " is available");
                available = true;

            } catch (IOException e) {
                System.err.println("--------------Port " + port + " is not available");
                available = false;
                port += 10;
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (IOException e) {
                        throw new RuntimeException("You should handle this error.", e);
                    }
                }
            }
        }
        return port;

    }

//    private int changeBoundPort(InetAddress boothost, int bootport) throws IOException {
//
//        int newPort = bootport;
//
//        // If working in remote mode, check if port is already bound 
//        while (true) {
//            ServerSocketChannel channel = null;
//            try {
//                // Create a new non-blocking server socket channel
//                channel = ServerSocketChannel.open();
//                //channel.configureBlocking(false);
//                InetSocketAddress isa = new InetSocketAddress("localhost", newPort);
//                channel.socket().bind(isa);
//                System.err.println("bind succedded to "+isa + " - " +channel.socket().isBound());
//                channel.socket().close();
//                channel.close();
//                break;
//            } catch (BindException e) {
//                //if (e.getMessage().contains("Address already in use")) {
//                newPort += 10;
//                System.err.println("Port " + (newPort - 10) + " already bound. Trying " + newPort + "...");
////                } else {
////                    System.err.println("Port " + (newPort - 10) + " is free ");
////                    break;
////                }
//            } finally {
//                try {
//                    channel.socket().close();
//                    channel.close();
//                } catch (IOException ex) {
//                }
//            }
//        }
//        if (bootport != newPort) {
//            System.err.println("Port changed: " + bootport + " --> " + newPort);
//        }
//
//        return newPort;
//    }
    public void contentDelivery(Message element) {
        try {

            MsgQueue.put(element);

            /*
             //System.err.println("Delivery : "+element.content.getClass());
             //Message element = ((EasyPastryContent) content).getContent();

             // only TaskStatusMessages are intercepted here
             if (element.content.getClass() == TaskStatusMessage.class) {

             if (community.hasJob(((TaskStatusMessage) element.content).getJobId())) {
             MsgQueue.put(element);
                    
             //                    if (community.needData(((TaskStatusMessage) element.content).getJobId(), ((TaskStatusMessage) element.content).getTaskId())) {
             //                        //System.err.println("Sending it up!!!");
             //                        MsgQueue.put(element);
             //                    } else {
             //                        //probably my own taskstatusmessage sent to all
             //                        //System.out.println("ignoring it");
             //                    }
             } else { // unknown job, let the "Join" part work
             MsgQueue.put(element);
             //       System.out.println("a job to go?");
             }
             } else {
             //System.err.println("Another msg incoming " + element.content.getClass());
             MsgQueue.put(element);

             }*/
        } catch (InterruptedException ex) {
            Logger.getLogger(TomP2PAdapter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void sendNext(Message msg) {

        List<PeerAddress> neighs = peer.peerBean().peerMap().all();

        if (!neighs.isEmpty()) {

            TomP2PMessage P2Pmsg = new TomP2PMessage(TomP2PMessage.UNICAST, peer.peerID(), -1, msg);

            PeerAddress p1 = (PeerAddress) neighs.get(0);
            //System.err.println(p1.toString());
            FutureDirect futureData;
            futureData = peer.peer().sendDirect(p1).object(P2Pmsg).start();
            // blocking send... what about a listener ?
            futureData.awaitUninterruptibly();

        }
    }

    @Override
    public void sendPrev(Message msg) {

        List<PeerAddress> neighs = peer.peerBean().peerMap().all();

        if (!neighs.isEmpty()) {

            TomP2PMessage P2Pmsg = new TomP2PMessage(TomP2PMessage.UNICAST, peer.peerID(), -1, msg);

            PeerAddress p1 = (PeerAddress) neighs.get(neighs.size() - 1);
            //System.err.println(p1.toString());
            FutureDirect futureData;
            futureData = peer.peer().sendDirect(p1).object(P2Pmsg).start();
            // blocking send... what about a listener ?
            futureData.awaitUninterruptibly();

//            try {
//                System.err.println("reply [" + futureData.object() + "]");
//            } catch (ClassNotFoundException ex) {
//                Logger.getLogger(TomP2PAdapter.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IOException ex) {
//                Logger.getLogger(TomP2PAdapter.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
    }

    @Override
    public void sendAll(Message msg, boolean metoo) {
  

//System.err.println("new SendAll " + msg.content.getClass());
        if (neighs == null) {
            neighs = peer.peerBean().peerMap().all();
        } else {
            List<PeerAddress> neighs2 = peer.peerBean().peerMap().all();
            Iterator it = neighs2.iterator();
            while (it.hasNext()) {
                PeerAddress add = (PeerAddress) it.next();
                if (!neighs.contains(add)){
                    neighs.add(add);
                    System.err.println(add.peerSocketAddress().inetAddress().toString());
                }
            }
            neighs2 = peer.peerBean().peerMap().allOverflow();
            it = neighs2.iterator();
            while (it.hasNext()) {
                PeerAddress add = (PeerAddress) it.next();
                if (!neighs.contains(add)) {
                    neighs.add(add);
                    System.err.println(add.peerSocketAddress().inetAddress().toString());
                }
            }
            
            ArrayList saved = (ArrayList) read("vlan0", "peers-realm", "vlan0", null);
            it = saved.iterator();
            while (it.hasNext()) {
                PeerAddress add = (PeerAddress) it.next();
                if (!neighs.contains(add)) {
                    neighs.add(add);
                    System.err.println(add.peerSocketAddress().inetAddress().toString());
                }
            }
        }
        //List<PeerAddress> neighs = peer.peerBean().peerMap().all();
        System.err.println(neighs.size());
// replace seqnum++ by the timestap: prevents a "reincarnated" node to reuse the same seqnums
        TomP2PMessage P2Pmsg = new TomP2PMessage(TomP2PMessage.BCAST, peer.peerID(), System.currentTimeMillis(), msg);
        Iterator it = neighs.iterator();

        while (it.hasNext()) {

            PeerAddress p1 = (PeerAddress) it.next();
            // send direct
            FutureDirect futureData;
            futureData = peer.peer().sendDirect(p1).object(P2Pmsg).start();
            // blocking send one by one... what about a listener after all sends were sent ??
            futureData.awaitUninterruptibly();

        }
//            neighs = peer.peerBean().peerMap().allOverflow();
//            while (it.hasNext()) {
//
//                PeerAddress p1 = (PeerAddress) it.next();
//                // send direct
//                FutureDirect futureData;
//                futureData = peer.peer().sendDirect(p1).object(P2Pmsg).start();
//                // blocking send one by one... what about a listener after all sends were sent ??
//                futureData.awaitUninterruptibly();
//
//            }

        if (metoo) {
            PeerAddress p1 = peer.peerAddress();
            FutureDirect futureData;
            futureData = peer.peer().sendDirect(p1).object(P2Pmsg).start();
        }

    }

    @Override
    public void save(Serializable value, Serializable... keys) {
        try {
            Number160 key0 = Number160.ZERO;
            Number160 key1 = Number160.ZERO;
            Number160 key2 = Number160.ZERO;
            Number160 key3 = Number160.ZERO;

            switch (keys.length) {
                case 1:
                    if (keys[0].getClass().equals(String.class
                    )) {
                        key0 = Number160.createHash((String) keys[0]);
                    } else {
                        cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                        key0 = new Number160(id.toString());
                    }
                    peer.put(key0).domainKey(Number160.ZERO).versionKey(Number160.ZERO).data(new Data(value)).start();
                    break;

                case 2:
                    if (keys[0].getClass().equals(String.class
                    )) {
                        key0 = Number160.createHash((String) keys[0]);
                    } else {
                        cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                        key0 = new Number160(id.toString());

                    }
                    if (keys[1].getClass().equals(String.class
                    )) {
                        key1 = Number160.createHash((String) keys[1]);
                    } else {
                        cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                        key1 = new Number160(id.toString());
                    }
                    peer.put(key0).domainKey(key1).versionKey(Number160.ZERO).data(new Data(value)).start();
                    break;
                default:
                    if (keys[0].getClass().equals(String.class
                    )) {
                        key0 = Number160.createHash((String) keys[0]);
                    } else {
                        cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                        key0 = new Number160(id.toString());

                    }
                    if (keys[1].getClass().equals(String.class
                    )) {
                        key1 = Number160.createHash((String) keys[1]);
                    } else {
                        cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                        key1 = new Number160(id.toString());

                    }
                    if (keys[2].getClass().equals(String.class
                    )) {
                        key2 = Number160.createHash((String) keys[2]);
                    } else {
                        cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[2];
                        key2 = new Number160(id.toString());
                    }
                    peer.put(key0).domainKey(key1).versionKey(key2).data(new Data(value)).start();
                    break;

            }
            //peer.put(Number160.createHash(key)).data(new Data(value)).start();
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        } catch (IOException ex) {
            Logger.getLogger(TomP2PAdapter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void blocking_save(Serializable value, Serializable... keys) {
        try {
            // we consider the following order for keys: 0 = location, 1 = domain, 2 = content, 3 = version
            // in all cases where key2==null the content key is equal to the location key = hash (key0)
            // This means that in most cases the data will be stored in the location with the key = content key
            // Using a content key different from location may allow "targetting" a node to store different ressources
            Data dt = new Data(value);
            PutBuilder pb;
            Number160 location = Number160.ZERO;
            Number160 domain = Number160.ZERO;
            Number160 content = Number160.ZERO;
            Number160 version = Number160.ZERO;

            RequestP2PConfiguration rp = new RequestP2PConfiguration(1, 0, 0);

            if (keys == null) {

                pb = peer.put(location).requestP2PConfiguration(rp).data(content, dt);

            } else {
                //System.out.println(keys[0].getClass());

                switch (keys.length) {

                    case 1:
                        if (keys[0] != null) {
                            if (keys[0].getClass().equals(String.class
                            )) {
                                location = Number160.createHash((String) keys[0]);
                            } else {
                                cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                                location = new Number160(id.toString());
                            }
                        }
                        content = location;
                        pb = peer.put(location).requestP2PConfiguration(rp).data(content, dt);
                        //System.out.println(keys[0]+" "+location + " " + content);

                        //pb = peer.put(Number160.createHash(keys[0])).domainKey(Number160.ZERO).versionKey(Number160.ZERO).data(dt);
                        break;

                    case 2:
                        if (keys[0] != null) {
                            if (keys[0].getClass().equals(String.class
                            )) {
                                location = Number160.createHash((String) keys[0]);
                            } else {
                                cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                                location = new Number160(id.toString());

                            }
                        }
                        if (keys[1] != null) {
                            if (keys[1].getClass().equals(String.class
                            )) {
                                domain = Number160.createHash((String) keys[1]);
                            } else {
                                cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                                domain = new Number160(id.toString());
                            }
                        }
                        content = location;
                        pb = peer.put(location).requestP2PConfiguration(rp).data(domain, content, dt);

                        //pb = peer.put(Number160.createHash(keys[0])).domainKey(Number160.createHash(keys[1])).versionKey(Number160.ZERO).data(dt);
                        break;

                    case 3:
                        if (keys[0] != null) {
                            if (keys[0].getClass().equals(String.class
                            )) {
                                location = Number160.createHash((String) keys[0]);
                                //System.err.println("location = String");
                            } else {
                                System.err.println("location = Number160");
                                cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                                location = new Number160(id.toString());

                            }
                        }
                        if (keys[1] != null) {
                            if (keys[1].getClass().equals(String.class
                            )) {
                                domain = Number160.createHash((String) keys[1]);
                            } else {
                                cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                                domain = new Number160(id.toString());
                            }
                        }
                        content = location;

                        if (keys[2] != null) {
                            if (keys[2].getClass().equals(String.class
                            )) {
                                content = Number160.createHash((String) keys[2]);
                            } else {
                                cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[2];
                                content = new Number160(id.toString());
                            }
                        }
                        pb = peer.put(location).requestP2PConfiguration(rp).data(location, domain, content, version, dt);

                        break;
                    default:
                        //pb = peer.put(Number160.createHash(keys[0])).domainKey(Number160.createHash(keys[1])).versionKey(Number160.createHash(keys[2])).data(dt);
                        if (keys[0] != null) {
                            if (keys[0].getClass().equals(String.class
                            )) {
                                location = Number160.createHash((String) keys[0]);
                                //System.err.println("location = String");
                            } else {
                                //System.err.println("location = Number160");
                                cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                                location = new Number160(id.toString());

                            }
                        }
                        if (keys[1] != null) {
                            if (keys[1].getClass().equals(String.class
                            )) {
                                domain = Number160.createHash((String) keys[1]);
                            } else {
                                cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                                domain = new Number160(id.toString());
                            }
                        }
                        content = location;

                        if (keys[2] != null) {
                            if (keys[2].getClass().equals(String.class
                            )) {
                                content = Number160.createHash((String) keys[2]);
                            } else {
                                cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[2];
                                content = new Number160(id.toString());

                            }
                        }
                        if (keys[3] != null) {
                            if (keys[3].getClass().equals(String.class
                            )) {
                                version = Number160.createHash((String) keys[3]);
                            } else {
                                cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[3];
                                version = new Number160(id.toString());
                            }
                        }
                        pb = peer.put(location).requestP2PConfiguration(rp).data(location, domain, content, version, dt);

                        break;
                }
            }
            System.err.println("savin " + location + " cont " + content);
            //pb = peer.put(location).data(location, domain, content, version, dt);

            if (spf != null) {
                pb.addPostRoutingFilter(spf);
            }

            //PutBuilder pb = peer.put(Number160.createHash(key)).data(dt);
            /*  commented to test delays
             pb.idleTCPMillis(60000);
             pb.idleUDPMillis(60000);
             pb.slowResponseTimeoutSeconds(60000);
             */
            //pb.forceTCP();
            FuturePut futurePut = pb.start();
            //futurePut.awaitUninterruptibly(4000);
            futurePut.awaitUninterruptibly();

            dt.release();
            //DBase.commit();
            //DBase.getEngine().clearCache();
            //DBase.getEngine().compact();

            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (IOException ex) {
            Logger.getLogger(TomP2PAdapter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean contains(Serializable... keys) {
        StorageLayer sl = peer.storageLayer();
        //Number160 myid = peer.peerID();
        Number160 location = this.getId();
        Number160 domain = Number160.ZERO;
        Number160 content = Number160.ZERO;
        Number160 version = Number160.ZERO;

        if (keys == null) {
            return sl.contains(Number640.ZERO);

        } else {
            switch (keys.length) {
                case 1:
                    if (keys[0] != null) {
                        if (keys[0].getClass().equals(String.class)) {
                            location = Number160.createHash((String) keys[0]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                            location = new Number160(id.toString());
                        }
                    }
                    content = location;
                    break;

                case 2:
                    if (keys[0] != null) {
                        if (keys[0].getClass().equals(String.class
                        )) {
                            location = Number160.createHash((String) keys[0]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                            location = new Number160(id.toString());

                        }
                    }
                    if (keys[1] != null) {
                        if (keys[1].getClass().equals(String.class
                        )) {
                            domain = Number160.createHash((String) keys[1]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                            domain = new Number160(id.toString());
                        }
                    }
                    content = location;
                    break;

                case 3:
                    if (keys[0] != null) {
                        if (keys[0].getClass().equals(String.class)) {
                            location = Number160.createHash((String) keys[0]);
                            //System.err.println("xi, entroi nesse");
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                            location = new Number160(id.toString());
                            //System.err.println("entroi nesse");

                        }
                    }
                    if (keys[1] != null) {
                        if (keys[1].getClass().equals(String.class
                        )) {
                            domain = Number160.createHash((String) keys[1]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                            domain = new Number160(id.toString());

                        }
                    }
                    if (keys[2] != null) {
                        if (keys[2].getClass().equals(String.class
                        )) {
                            content = Number160.createHash((String) keys[2]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[2];
                            content = new Number160(id.toString());
                        }
                    }
                    break;
                default:
                    if (keys[0] != null) {
                        if (keys[0].getClass().equals(String.class
                        )) {
                            location = Number160.createHash((String) keys[0]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                            location = new Number160(id.toString());

                        }
                    }
                    if (keys[1] != null) {
                        if (keys[1].getClass().equals(String.class
                        )) {
                            domain = Number160.createHash((String) keys[1]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                            domain = new Number160(id.toString());

                        }
                    }
                    if (keys[2] != null) {
                        if (keys[2].getClass().equals(String.class
                        )) {
                            content = Number160.createHash((String) keys[2]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[2];
                            content = new Number160(id.toString());

                        }
                    }
                    if (keys[3] != null) {
                        if (keys[3].getClass().equals(String.class
                        )) {
                            version = Number160.createHash((String) keys[3]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[3];
                            version = new Number160(id.toString());
                        }
                    }
            }
        }
        //System.err.println("reading from "+location + "  "+ domain+ "  "+ content);
        Number640 query = new Number640(location, domain, content, version);
        return sl.contains(query);
    }

    @Override
    public Serializable read(Serializable... keys) {

        //FutureGet futureDHT = peer.get(Number160.ZERO).contentKey(Number160.createHash(key)).start();
        // data(contentKey,data)
        Number160 location = Number160.ZERO;
        Number160 domain = Number160.ZERO;
        Number160 content = Number160.ZERO;
        Number160 version = Number160.ZERO;
        boolean multiple = false; // specifies if there is a single response or a DataMap

        GetBuilder gt = null;

        if (keys == null) {
            gt = peer.get(location).contentKey(content);

        } else {
            switch (keys.length) {
                case 1:
                    // two possibilities for keyL
                    //    id --> ok (search)
                    //    null --> impossible (so return null)
                    if (keys[0] == null) {
                        // should return null but in save we store a value in location=Number160.ZERO
                        //return null;
                    } else {
                        if (keys[0] != null) {
                            if (keys[0].getClass().equals(String.class
                            )) {
                                location = Number160.createHash((String) keys[0]);
                            } else {
                                cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                                location = new Number160(id.toString());
                            }
                        }
                    }
                    content = location;
                    //System.out.println(keys[0]+" "+location + " " + content);
                    gt = peer.get(location).contentKey(content);

                    // Why not setting version/domain? perhaps the last version is not zero...
                    //gt = peer.get(location).domainKey(Number160.ZERO).contentKey(content).versionKey(Number160.ZERO);
                    break;

                case 2:
                    // four possibilities for keyL x keyD
                    // id, id --> search L, D
                    // id, null --> search on L, D=ZERO
                    // null, id --> search on L=ZERO, D
                    // null, null --> search on L=ZERO, D=ZERO
                    if (keys[0] != null) {
                        if (keys[0].getClass().equals(String.class
                        )) {
                            location = Number160.createHash((String) keys[0]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                            location = new Number160(id.toString());

                        }
                    }
                    if (keys[1] != null) {
                        if (keys[1].getClass().equals(String.class
                        )) {
                            domain = Number160.createHash((String) keys[1]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                            domain = new Number160(id.toString());
                        }
                    }
                    content = location;
                    gt = peer.get(location).domainKey(domain).contentKey(content);
                    // same as above. If no version is given, take the last.
                    //gt = peer.get(location).domainKey(domain).versionKey(Number160.ZERO);

                    break;

                case 3:

                    if (keys[0] != null) {
                        if (keys[0].getClass().equals(String.class
                        )) {
                            location = Number160.createHash((String) keys[0]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                            location = new Number160(id.toString());

                        }
                    }
                    if (keys[1] != null) {
                        if (keys[1].getClass().equals(String.class
                        )) {
                            domain = Number160.createHash((String) keys[1]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                            domain = new Number160(id.toString());

                        }
                    }
                    if (keys[2] != null) {
                        if (keys[2].getClass().equals(String.class
                        )) {
                            content = Number160.createHash((String) keys[2]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[2];
                            content = new Number160(id.toString());
                        }
                    }
                    gt = peer.get(location).domainKey(domain).contentKey(content);

                    break;
                default:
                    if (keys[0] != null) {
                        if (keys[0].getClass().equals(String.class
                        )) {
                            location = Number160.createHash((String) keys[0]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                            location = new Number160(id.toString());

                        }
                    }
                    if (keys[1] != null) {
                        if (keys[1].getClass().equals(String.class
                        )) {
                            domain = Number160.createHash((String) keys[1]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                            domain = new Number160(id.toString());

                        }
                    }
                    if (keys[2] != null) {
                        if (keys[2].getClass().equals(String.class
                        )) {
                            content = Number160.createHash((String) keys[2]);
                        } else {
                            cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[2];
                            content = new Number160(id.toString());

                        }
                    }
                    if (keys[3] != null) { // if has version use it, else get all versions
                        if (keys[3] != null) {
                            if (keys[3].getClass().equals(String.class
                            )) {
                                version = Number160.createHash((String) keys[3]);
                                if (keys[3].equals("_LAST")) {
                                    gt = peer.get(location).domainKey(domain).contentKey(content).getLatest();
                                } else {
                                    gt = peer.get(location).domainKey(domain).contentKey(content).versionKey(version);
                                }
                            } else {
                                cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[3];
                                version = new Number160(id.toString());
                                gt = peer.get(location).domainKey(domain).contentKey(content).versionKey(version);
                            }
                        }

                    } else {
                        gt = peer.get(location).domainKey(domain).contentKey(content).all();
                        multiple = true;

                    }

            }
        }
        FutureGet futureDHT = gt.start();
        futureDHT.awaitUninterruptibly();
        if (futureDHT.isSuccess()) {
            Object obj;
            try {
                if (multiple == true) {
                    Map<Number640, Data> dm = futureDHT.dataMap();
                    ArrayList al = new ArrayList();
                    Iterator it = dm.values().iterator();
                    while (it.hasNext()) {
                        Data dt = (Data) it.next();
                        al.add(dt.object());
                    }
                    obj = al;
                    //System.out.println("Multiple");

                } else {
                    obj = futureDHT.data().object();
                    //System.out.println("simple");

                }
                if (obj == null) {
                    //System.out.println("Objet numm");
                    return null;
                } else {
                    return (Serializable) obj;
                }
            } catch (EOFException ex) {
                return null;
            } catch (NullPointerException ex) {
                return null;

            } catch (ClassNotFoundException ex) {
                Logger.getLogger(TomP2PAdapter.class
                        .getName()).log(Level.SEVERE, null, ex);

            } catch (IOException ex) {
                Logger.getLogger(TomP2PAdapter.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.out.println("not success");
        }
        return null;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(Serializable... keys) {
        RemoveBuilder rb;
        Number160 location = Number160.ZERO;
        Number160 domain = Number160.ZERO;
        Number160 content = Number160.ZERO;
        Number160 version = Number160.ZERO;

        switch (keys.length) {
            case 1:
                if (keys[0] != null) {
                    if (keys[0].getClass().equals(String.class
                    )) {
                        location = Number160.createHash((String) keys[0]);
                    } else {
                        cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                        location = new Number160(id.toString());
                    }
                }
                break;

            case 2:
                if (keys[0] != null) {
                    if (keys[0].getClass().equals(String.class
                    )) {
                        location = Number160.createHash((String) keys[0]);
                    } else {
                        cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                        location = new Number160(id.toString());

                    }
                }
                if (keys[1] != null) {
                    if (keys[1].getClass().equals(String.class
                    )) {
                        domain = Number160.createHash((String) keys[1]);
                    } else {
                        cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                        domain = new Number160(id.toString());
                    }
                }
                break;
            default:
                if (keys[0] != null) {
                    if (keys[0].getClass().equals(String.class
                    )) {
                        location = Number160.createHash((String) keys[0]);
                    } else {
                        cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[0];
                        location = new Number160(id.toString());

                    }
                }
                if (keys[1] != null) {
                    if (keys[1].getClass().equals(String.class
                    )) {
                        domain = Number160.createHash((String) keys[1]);
                    } else {
                        cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[1];
                        domain = new Number160(id.toString());

                    }
                }
                if (keys[2] != null) {
                    if (keys[2].getClass().equals(String.class
                    )) {
                        version = Number160.createHash((String) keys[2]);
                    } else {
                        cloudfit.util.Number160 id = (cloudfit.util.Number160) keys[2];
                        version = new Number160(id.toString());
                    }
                }
                break;
        }
        rb = peer.remove(location).domainKey(domain).versionKey(version);
        rb.start().awaitUninterruptibly();

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
