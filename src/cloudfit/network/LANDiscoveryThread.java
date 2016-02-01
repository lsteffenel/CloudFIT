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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilitary class, used to discover peers via IP Multicast
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class LANDiscoveryThread extends Thread {

    private int port = 8532;
    private int taille = 4;
    private byte rcvbuffer[] = null;
    private byte sndbuffer[] = null;
    private String mgroup = "224.1.1.249";
    private MulticastSocket socket = null;
    private static InetAddress group = null;
    private InetAddress myaddress = null;
    private InetSocketAddress myfulladd = null;

    /**
     * Main class constructor, uses the default parameters.
     */
    public LANDiscoveryThread() {
        initParameters();
    }

    /**
     * Alternative constructor, uses the parameters
     *
     * @param mgroup the IP multicast group to join
     * @param port the port
     */
    public LANDiscoveryThread(String mgroup, int port) {
        this.mgroup = mgroup;
        this.port = port;
        initParameters();
    }

    public void run() {
        try {
            socket.joinGroup(group);
            while (true) {
                DatagramPacket query = new DatagramPacket(rcvbuffer, rcvbuffer.length);
                socket.receive(query);
                sndbuffer = query.getData();
                //System.err.println("ici");
                //if (!query.getAddress().equals(myaddress))
                //{
                DatagramPacket response = new DatagramPacket(serialize(myfulladd), sndbuffer.length);
                response.setAddress(query.getAddress());
                response.setPort(query.getPort());
                socket.send(response);
                sleep(100);
                //} // else ignore
            }
            //socket.leaveGroup(group);
            //socket.close();
        } catch (InterruptedException ex) {
            Logger.getLogger(LANDiscoveryThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LANDiscoveryThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Used to open a multicast socket to listen/diffuse
     */
    private void initParameters() {
        this.setName("LANDiscoveryThread");
        try {
            socket = new MulticastSocket(port);
            group = InetAddress.getByName(mgroup);
            System.err.println(group);
            myaddress = getInetAddress();
            myfulladd = new InetSocketAddress(myaddress, port);
            sndbuffer = serialize(myfulladd);
            rcvbuffer = new byte[sndbuffer.length];
        } catch (IOException ex) {
            Logger.getLogger(LANDiscoveryThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method to return the InetAddress of this node. If several interfaces are
     * available, picks the IP that has a route to another machine.
     *
     * @return the InetAddress of this machine.
     */
    public InetAddress getInetAddress() {
        InetAddress ip = null;
        try {
            ip = InetAddress.getLocalHost();

            if (ip.getHostAddress().startsWith("127")) {
                Socket s = new Socket();//("192.168.1.1", 80); 
                s.connect(new InetSocketAddress("www.google.com", 80), 2000);
                ip = s.getLocalAddress();
                s.close();
            }

        } catch (UnknownHostException e) {
            //e.printStackTrace();
        } catch (Exception e) {
            //e.printStackTrace();
            //System.err.println("error inetAddr by socket");
        }

        return ip;
    }

    /**
     * Looks for another peer in the network (thanks to UDP Multicast)
     *
     * @param timeout how many seconds to wait until fail
     * @return the InetAddress of the peer, or null if none
     */
    public InetSocketAddress findPeer(int timeout) {
        try {
            DatagramSocket dsocket = new DatagramSocket();
            DatagramPacket query = new DatagramPacket(sndbuffer, sndbuffer.length);
            query.setAddress(group);
            query.setPort(port);
            dsocket.send(query);
            DatagramPacket response = new DatagramPacket(rcvbuffer, rcvbuffer.length);
            dsocket.setSoTimeout(timeout);
            dsocket.receive(response);
            System.err.println("Found a peer :" + deserialize(response.getData()));
            
            if (deserialize(response.getData()).equals(myfulladd)) {
                System.err.println("c'est moi même!!!!");
                return null;
            } else {
                return deserialize(response.getData());
            }
        } catch (SocketTimeoutException to) {
            System.err.println("Cannot find a peer to bootstrap.");
            return null;
        } catch (IOException ex) {
            Logger.getLogger(LANDiscoveryThread.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    public static void main(String[] args) {
        LANDiscoveryThread ldt = new LANDiscoveryThread();
        ldt.start();
    }

    private byte[] serialize(InetSocketAddress obj) {
        ObjectOutput out = null;
        byte[] yourBytes = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            yourBytes = bos.toByteArray();
            out.close();
            bos.close();

        } catch (IOException ex) {
            Logger.getLogger(LANDiscoveryThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return yourBytes;
    }

    private InetSocketAddress deserialize(byte[] in) {
        InetSocketAddress radd = null;
        try {
            ObjectInput oin = null;
            ByteArrayInputStream bis = new ByteArrayInputStream(in);
            oin = new ObjectInputStream(bis);
            radd = (InetSocketAddress) oin.readObject();
        } catch (IOException ex) {
            Logger.getLogger(LANDiscoveryThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LANDiscoveryThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        return radd;
    }
}
