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
import cloudfit.service.Community;
import cloudfit.service.JobManagerInterface;
import cloudfit.service.ServiceInterface;
import cloudfit.service.ThreadSolve;
import cloudfit.service.ThreadSolveDHT;
import cloudfit.storage.SerializedDiskStorage;
import cloudfit.storage.StorageAdapterInterface;
import cloudfit.util.Number160;
import java.net.InetSocketAddress;

/**
 * Main factory of the CloudFIT framework, provides object of several classes
 * TODO: perform class instantiation based on a properties file
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class TheBigFactory {
    public static ServiceInterface getCommunity(long id, ORBInterface orb)
    {
        return new Community(id, orb);
    }
    
    
    public static ORBInterface getORB()
    {
        return new CoreORB();
    }
    
    public static CoreQueue getCoreQueue()
    {
        CoreQueue queue = new CoreQueue();
        queue.start();
        return queue;
    }
    
    public static NetworkAdapterInterface getP2P (CoreQueue queue, InetSocketAddress peer)
    {
        
        //if (select.matches("EasyPastry".toLowerCase())) {
        return new EasyPastryAdapter(queue, peer);
        //}
        //else return null;
    }
    
    public static StorageAdapterInterface getStorage()
    {
        return new SerializedDiskStorage();
    }
    
    public static JobManagerInterface getThreadSolve(ServiceInterface service, Number160 jobId, ApplicationInterface jobClass, String[] args)
    {
        //ThreadSolve TS = new ThreadSolveDHT(service, jobId, jobClass, args);
//        Class cl = Class.forName("com.bla.TestActivity");
//        ThreadSolve ts2 = (ThreadSolve)cl.getConstructor(ServiceInterface.class, int.class , ApplicationInterface.class , String.class).newInstance(service, jobId, jobClass, args);
        ThreadSolve TS = new ThreadSolve(service, jobId, jobClass, args);
        
        //TS.start();
        return TS;
    }
}
