/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.storage;

import cloudfit.network.NetworkAdapterInterface;
import cloudfit.network.TomP2PAdapter;
import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class DHT {
    
    private static List<String> filenames = new java.util.concurrent.CopyOnWriteArrayList<String>();
    
    
    
    public static void main(String[] args) {
        long start;
        long end;

        Options options = new Options();
        Option help = new Option("help", "print this message");
        Option put = OptionBuilder.withArgName("put")
                .hasArg()
                .withDescription("put data on the DHT")
                .create("put");
        //sourceDir.setRequired(true);
        Option get = OptionBuilder.withArgName("get")
                .hasArg()
                .withDescription("get data from the DHT")
                .create("get");
        //destDir.setRequired(true);
        Option delete = OptionBuilder.withArgName("delete")
                .hasArg()
                .withDescription("delete file from the DHT")
                .create("delete");
        
        Option node = OptionBuilder.withArgName("node")
                .hasArg()
                .withDescription("Optional address to join the P2P network")
                .create("node");
        Option port = OptionBuilder.withArgName("port")
                .hasArg()
                .withDescription("Optional port to join the P2P network")
                .create("port");
 
        options.addOption(put);
        options.addOption(get);
        options.addOption(delete);
        options.addOption(node);
        options.addOption(port);

        // create the parser
        CommandLineParser parser = new PosixParser();
        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            usage(options);
            return;
        }
        
        InetSocketAddress peer = null; // the defaut value = discovery

        if (line.hasOption("node")) {
            if (line.hasOption("port")) {
                peer = new InetSocketAddress(line.getOptionValue("node"), Integer.parseInt(line.getOptionValue("port")));
            } else {
                peer = new InetSocketAddress(line.getOptionValue("node"), 7777);
            }

        } else {
                peer = new InetSocketAddress(InetAddress.getLoopbackAddress(), 7777);       
        }
        
        //////////////////////////////////////////////////
        
//        
//        ///////////////////// Pastry
//
//        /* Declaration of the main class
//         * all the internal initialization is made on the constructor
//         */
//        CoreORB TDTR = (CoreORB) TheBigFactory.getORB();
//
//
//        /* Define if connecting to a peer or network discovery
//         * 
//         */
//        CoreQueue queue = TheBigFactory.getCoreQueue();
//
//        TDTR.setQueue(queue);
//
//        /* creates a module to plug on the main class
//         * and subscribe it to the messaging system
//         */
//        Community community = new Community(1, TDTR);
//
//        //NetworkAdapterInterface P2P = new EasyPastryDHTAdapter(queue, peer, community);
//        NetworkAdapterInterface P2P = new TomP2PAdapter(queue, peer, community);
//        
//
//        TDTR.setNetworkAdapter(P2P);
//
//        TDTR.subscribe(community);
//
//        //TDTR.setStorage(new SerializedDiskStorage());
//        TDTR.setStorage((StorageAdapterInterface) P2P);
//        
        
        NetworkAdapterInterface P2P = new TomP2PAdapter(null, peer, null);
        
        start = System.currentTimeMillis();

        /////////////////////////////////////////////////
        
        // OPTION PARSING
        // is there a "node" and "port" option ?
        
        if (line.hasOption("put")) {
            
            List<String> files = loadInput(line.getOptionValue("put"));
        
        System.err.println(files);
        System.err.println(files.size());
        
        
        
        Iterator it = files.iterator();
        int number = 0;
        while (it.hasNext()) {
            String file = (String)it.next();
            long init = System.currentTimeMillis();
            FileContainer fc = new FileContainer(file);
            DHTStorageUnit dsu = new DHTStorageUnit(null, -1, (Serializable)fc);
            
            ((StorageAdapterInterface)P2P).blocking_save(dsu,"input.data" + number);
            //save("input.data" + number, fc, false, number); 
            number++;
            long fin = System.currentTimeMillis();
            System.err.println(file+" ("+number+") saved in "+ (fin-init) + " ms");
            
            //System.gc();
            
        }
        System.gc();
        
        }
        
        if (line.hasOption("get")) {
           // repetitions = Integer.parseInt(line.getOptionValue("repeat"));
            ((StorageAdapterInterface)P2P).read(null);

        }
        
        if (line.hasOption("delete")) {
           // rargs[0] = line.getOptionValue("nreducers");
            ((StorageAdapterInterface)P2P).remove(null);
        }

        
        
            //job.setReducer("Reducer");
                //for (int i=0; i<repetitions; i++) {
                
        
                end = System.currentTimeMillis();

//                Thread.sleep(3000);

                System.err.println("Total time = " + (end - start));

                System.err.println("Total time = " + (end - start));

                
                Scanner sc = new Scanner(System.in);
                String i = sc.next();
                //}
                
                System.exit(0);
                

    }

    private static void usage(Options options) {

        // Use the inbuilt formatter class
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("MRLauncher", options);

    }
    
    
    private NetworkAdapterInterface initNetwork(InetSocketAddress peer) {
        
        
        NetworkAdapterInterface P2P = new TomP2PAdapter(null, peer, null);
        System.err.println("starting network");
        
        return P2P;          

    }
    
    /**
     * looks for input files on the arguments. If argument is a directory, it
     * includes all files inside, recursively.
     */
    private static List<String> loadInput(String dir) {
        if (filenames.isEmpty()) {
            File target = new File(dir);

            if (target.isDirectory()) {
                addDirectoryFiles(target);
            } else { // target is a file
                filenames.add(target.getPath());
            }
        }
        return filenames;
    }

    private static boolean addDirectoryFiles(File target) {

        if (!target.isDirectory()) {
            filenames.add(target.getPath());
            return false;
        }

        File[] listOfFiles = target.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                addDirectoryFiles(file);
            }
        }
        return true;
    }
    
}
