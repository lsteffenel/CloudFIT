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

import cloudfit.application.ApplicationInterface;
import cloudfit.network.EasyPastryAdapter;
import cloudfit.network.NetworkAdapterInterface;
import cloudfit.network.TomP2PAdapter;
import cloudfit.service.Community;
import cloudfit.service.JobManagerInterface;
import cloudfit.service.JobManager;
import cloudfit.service.JobsScheduler;
import cloudfit.service.ServiceInterface;
import cloudfit.storage.SerializedDiskStorage;
import cloudfit.storage.StorageAdapterInterface;
import cloudfit.util.Number160;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Main factory of the CloudFIT framework, provides object of several classes
 * TODO: perform class instantiation based on a properties file
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class TheBigFactory {

    public static Community getCommunity(String id, ORBInterface orb, RessourceManager rm) {
        return new Community(id, orb, rm);
    }

    public static RessourceManager getRM(JobsScheduler jobScheduler) {
        return new RessourceManager(jobScheduler);
    }

    public static JobsScheduler getJS() {
        return new JobsScheduler();
    }

    public static ORBInterface getORB() {
        return new CoreORB();
    }


    public static NetworkAdapterInterface getP2P(CoreQueue queue, InetSocketAddress peer) {

        //if (select.matches("EasyPastry".toLowerCase())) {
        return new TomP2PAdapter(queue, peer);
        //return new EasyPastryAdapter(queue, peer);
        //}
        //else return null;
    }

    public static StorageAdapterInterface getStorage() {
        return new SerializedDiskStorage();
    }

    public static JobManagerInterface getJobManager(ServiceInterface service, Number160 jobId, ApplicationInterface jobClass, String[] args, Properties props) {
//        Class cl = Class.forName("com.bla.TestActivity");
//        ThreadSolve ts2 = (ThreadSolve)cl.getConstructor(ServiceInterface.class, int.class , ApplicationInterface.class , String.class).newInstance(service, jobId, jobClass, args);
        //ThreadSolve TS = new ThreadSolve(service, jobId, jobClass, args, props);
        JobManager JM = new JobManager(service, jobId, jobClass, args, props);

        //TS.start();
        return JM;
    }

    public static Community initNetwork(InetSocketAddress peer, String scopeName) {
        ///////////////////// Pastry

        /* Declaration of the main class
         * all the internal initialization is made on the constructor
         */
        CoreORB TDTR = (CoreORB) getORB();

        /* Creates a ressource Manager
         */
        JobsScheduler js = new JobsScheduler();
        RessourceManager rm = getRM(js);

        //NetworkAdapterInterface P2P = new EasyPastryDHTAdapter(queue, peer, community);
        NetworkAdapterInterface P2P = new TomP2PAdapter(TDTR.getQueue(), peer);

        TDTR.setNetworkAdapter(P2P);

        /* creates a module to plug on the main class
         * and subscribe it to the messaging system
         */
        if (scopeName == null) {
            scopeName = "vlan0";
        }
        Community community = getCommunity(scopeName, TDTR, rm);

        TDTR.subscribe(community);

        if (!scopeName.equals("vlan0")) {
            // also creates a default community for "nameless" jobs
            Community vlan0 = getCommunity("vlan0", TDTR, rm);
            TDTR.subscribe(vlan0);
        }
        //TDTR.setStorage(new SerializedDiskStorage());
        TDTR.setStorage((StorageAdapterInterface) P2P);

        System.err.println("starting network");

        return community;
    }
}
