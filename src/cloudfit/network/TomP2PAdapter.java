/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.network;

import cloudfit.core.CoreQueue;
import cloudfit.core.Message;
import cloudfit.service.Community;
import cloudfit.service.TaskStatusMessage;
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.DSASignatureFactory;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.dht.PutBuilder;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.p2p.PostRoutingFilter;
import net.tomp2p.p2p.SlowPeerFilter;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
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
    private Community community = null;
    //InetSocketAddress add = null;

    //final private Peer peer;
    private PeerDHT peer = null;
    private DB DBase = null;
    //private long seqnum = 0;
    //private Number160 id;

    static HashMap<Number160, TreeSet> history;

    PostRoutingFilter spf = null;

    public Number160 getId() {
        return peer.peerID();
    }

    public TomP2PAdapter(CoreQueue queue, InetSocketAddress add, Community comm) {
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
            this.community = comm;
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
            NetworkInterface iface = ifDetect();
            //System.err.println(iface.getHardwareAddress() + " " + iface.getInetAddresses().nextElement().getHostAddress() + " ");
            //String sid =   iface.getHardwareAddress().toString() + " ";
            byte[] mac = iface.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));        
        }
            //System.err.println(iface.getDisplayName() + " " + sb.toString() );
            String sid = sb.toString() + " " + iface.getInetAddresses().nextElement().getHostAddress() + " "+ port;
            Number160 id = Number160.createHash(sid);
            System.err.println(sid+"\n"+id);

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
            if (storageType != null) {
                if (storageType.equals("disk")) {

                    File storagePath = new File(path);
                    //System.err.println("Exists or not ? "+ storagePath.exists());
                    if (!storagePath.exists()) {
                        System.err.println("creating dir");
                        storagePath.mkdirs();
                    }
                    //System.err.println("Exists or not ? "+ storagePath.exists());

                    //DBase = DBMaker.newFileDB(new File(storagePath,"tomp2p")).transactionDisable().cacheDisable().closeOnJvmShutdown().make();
                    //DBase = DBMaker.newFileDB(new File(storagePath,"tomp2p")).transactionDisable().closeOnJvmShutdown().make();
                    //DBase = DBMaker.newFileDB(new File(storagePath,"tomp2p")).transactionDisable().cacheSoftRefEnable().closeOnJvmShutdown().sizeLimit(1024*1024*1024*1024).make();
                    //DBase = DBMaker.newFileDB(new File(storagePath,"tomp2p")).transactionDisable().cacheSoftRefEnable().closeOnJvmShutdown().make();
                    File base = new File(storagePath, "tomp2p_"+id);
                    //DBase = DBMaker.newFileDB(base).transactionDisable().asyncWriteEnable().cacheSoftRefEnable().closeOnJvmShutdown().make();
                //DBase = DBMaker.newFileDB(base).cacheDisable().closeOnJvmShutdown().make();

                    //DBase = DBMaker.newFileDB(base).mmapFileEnableIfSupported().asyncWriteEnable().cacheSoftRefEnable().transactionDisable().closeOnJvmShutdown().make();
                    DBase = DBMaker.newFileDB(base).transactionDisable().cacheSoftRefEnable().closeOnJvmShutdown().make();
                    //StorageDisk sd = new StorageDisk(DBase, id, storagePath, new DSASignatureFactory(), 60 * 1000);
                    
                    StorageDisk sd = new StorageDisk(DBase, id, storagePath, new DSASignatureFactory(), 10 * 1000);

                    pbd.storage(sd);
                }
            } else {
                //Memory storage

            }
            // node start

            peer = pbd.start();

            String slow = PropertiesUtil.getProperty("slow");
            spf = new SlowPeerFilter();

            if (slow != null) {
                if (slow.equals("true")) {

                    System.err.println("changing slow to true");
                    PeerAddress pa = peer.peerAddress().changeSlow(true);
                    peer.peer().peerBean().serverPeerAddress(pa);

                }
            }
//            

//            //IndirectReplication indir = new IndirectReplication(peer).replicationFactor(1).keepData(false);
//            IndirectReplication indir = new IndirectReplication(peer);
//            indir.autoReplication(true);
//            indir.addReplicationFilter(new SlowReplicationFilter());
//            indir.start();
            System.err.println("Slow = " + peer.peerAddress().isSlow());

            //peer.peerAddress().changeSlow(true);
            //new IndirectReplication(peer).start();
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
                Thread.sleep(5000);
                System.err.println("peer knows: " + peer.peerBean().peerMap().all() + " unverified: "
                        + peer.peerBean().peerMap().allOverflow());

            }
            // delivery method
            peer.peer().objectDataReply(new ObjectDataReply() {
                @Override
                public Object reply(final PeerAddress sender, final Object request) throws Exception {
                    TomP2PMessage P2Pmsg = (TomP2PMessage) request;
                    if (P2Pmsg.getType() == TomP2PMessage.BCAST) {
                        if (history.containsKey(P2Pmsg.getSenderId())) {
                            TreeSet ts = history.get(P2Pmsg.getSenderId());
                            if (ts.contains(P2Pmsg.getSequenceNumber())) {
                                // do nothing, already know
                                //System.err.println("do nothing, already have it" + P2Pmsg.getSenderId() + " (" + P2Pmsg.getSequenceNumber() + ")");
                            } else {
                                //System.err.println("unknown seq");
                                ts.add(P2Pmsg.getSequenceNumber());
                                
                                //bbcast(P2Pmsg);

                            }
                        } else {
                            //System.err.println("unknown unknown sender");
                            TreeSet ts = new TreeSet();
                            ts.add(P2Pmsg.getSequenceNumber());
                            history.put(P2Pmsg.getSenderId(), ts);
                            //bbcast(P2Pmsg);

                        }
                    }
                    //System.err.println("msg arrived");
                    contentDelivery((Message) P2Pmsg.getContent());
                    return "ack";
                }

                private void bbcast(TomP2PMessage P2Pmsg) {
                    List<PeerAddress> neighs = peer.peerBean().peerMap().all();
                    Iterator it = neighs.iterator();
                    while (it.hasNext()) {
                        PeerAddress p1 = (PeerAddress) it.next();
                        //System.err.println(p1.toString());
                        //peer.peer().sendDirect(p1).object("test").start();

                        // send direct
                        FutureDirect futureData;
                        futureData = peer.peer().sendDirect(p1).object(P2Pmsg).start();
                        // blocking send one by one... what about a listener after all sends were sent ??
//            futureData.awaitUninterruptibly();

                    }
                }

            });

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

        System.setProperty("java.net.preferIPv4Stack" , "true");
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

                //System.err.println(address.getHostAddress());
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
                    socket.socket().setSoTimeout(3000);

                    // bind the socket to your local interface
                    socket.bind(new InetSocketAddress(address, 0));
                    //System.err.println(socket.socket().getLocalPort());

                    // try to connect to *somewhere*
                    socket.connect(new InetSocketAddress("www.univ-reims.fr", 80));
                } catch (IOException ex) {
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
            //System.err.println("Delivery : ");
            //Message element = ((EasyPastryContent) content).getContent();

            // only TaskStatusMessages are intercepted here
            if (element.content.getClass() == TaskStatusMessage.class) {

                if (community.hasJob(
                        ((TaskStatusMessage) element.content).getJobId())) {
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
    public void sendAll(Message msg) {
        //System.err.println("new SendAll");
        List<PeerAddress> neighs = peer.peerBean().peerMap().all();
        // replace seqnum++ by the timestap: prevents a "reincarnated" node to reuse the same seqnums
        TomP2PMessage P2Pmsg = new TomP2PMessage(TomP2PMessage.BCAST, peer.peerID(), System.currentTimeMillis(), msg);
        Iterator it = neighs.iterator();
        while (it.hasNext()) {
            PeerAddress p1 = (PeerAddress) it.next();
            //System.err.println(p1.toString());
            //peer.peer().sendDirect(p1).object("test").start();

            // send direct
            FutureDirect futureData;
            futureData = peer.peer().sendDirect(p1).object(P2Pmsg).start();
            // blocking send one by one... what about a listener after all sends were sent ??
            //futureData.awaitUninterruptibly();

        }
        PeerAddress p1 = peer.peerAddress();
        FutureDirect futureData;
        futureData = peer.peer().sendDirect(p1).object(P2Pmsg).start();
    }

//    private void bbcast(TomP2PMessage P2Pmsg) {
//        List<PeerAddress> neighs = peer.peerBean().peerMap().all();
//        Iterator it = neighs.iterator();
//        while (it.hasNext()) {
//            PeerAddress p1 = (PeerAddress) it.next();
//            System.err.println(p1.toString());
//            //peer.peer().sendDirect(p1).object("test").start();
//
//            // send direct
//            FutureDirect futureData;
//            futureData = peer.peer().sendDirect(p1).object(P2Pmsg).start();
//            // blocking send one by one... what about a listener after all sends were sent ??
////            futureData.awaitUninterruptibly();
//
//        }
//    }
    @Override
    public void save(String key, Serializable value, boolean mutable) {
        try {
            peer.put(Number160.createHash(key)).data(new Data(value)).start();
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

        } catch (IOException ex) {
            Logger.getLogger(TomP2PAdapter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void blocking_save(String key, Serializable value, boolean mutable) {
        try {
            //peer.put(Number160.createHash(key)).data(new Data(value)).start().awaitUninterruptibly();
            //RequestP2PConfiguration rp = new RequestP2PConfiguration(1, 0, 0);
            Data dt = new Data(value);
            //PutBuilder pb = peer.put(Number160.createHash(key)).requestP2PConfiguration(rp).data(dt);
            //PutBuilder pb = peer.put(Number160.ZERO).requestP2PConfiguration(rp).data(Number160.createHash(key),dt);
            PutBuilder pb = peer.put(Number160.createHash(key)).data(dt);
            if (spf != null) {
                pb.addPostRoutingFilter(spf);
            }

            //PutBuilder pb = peer.put(Number160.createHash(key)).data(dt);
            pb.idleTCPMillis(60000);
            pb.idleUDPMillis(60000);
            pb.slowResponseTimeoutSeconds(60000);
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
    public Serializable read(String key) {

        //FutureGet futureDHT = peer.get(Number160.ZERO).contentKey(Number160.createHash(key)).start();
        FutureGet futureDHT = peer.get(Number160.createHash(key)).start();
        futureDHT.awaitUninterruptibly();
        if (futureDHT.isSuccess()) {

            //return (FileContent) futureDHT.getData().getObject();
            //return (Serializable) futureDHT.dataMap().values().iterator().next().object();
            Object obj;
            try {
                obj = futureDHT.data().object();
                if (obj == null) {
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

        }
        return null;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(String key) {
        peer.remove(Number160.createHash(key)).start().awaitUninterruptibly();

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
