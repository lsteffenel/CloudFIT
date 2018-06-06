package cloudfit.storage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import cloudfit.core.CoreORB;
import cloudfit.core.TheBigFactory;
import cloudfit.service.Community;
import cloudfit.util.Number160;
import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class VersionTest {

    private static List<String> filenames = new java.util.concurrent.CopyOnWriteArrayList<String>();
    private static Community community;

    public static void main(String[] args) {
        long start;
        long end;

        Options options = new Options();
        Option help = new Option("help", "print this message");
        Option node = OptionBuilder.withArgName("node")
                .hasArg()
                .withDescription("Optional address to join the P2P network")
                .create("node");
        Option port = OptionBuilder.withArgName("port")
                .hasArg()
                .withDescription("Optional port to join the P2P network")
                .create("port");

        options.addOption(help);
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

        community = TheBigFactory.initNetwork(peer, "scopeName");

        start = System.currentTimeMillis();

        System.out.println("Series 1 - Location");

        String valor = "value";
        String valor2 = "toto";

        DHTStorageUnit myDSU = new DHTStorageUnit(null, -1, valor);
        DHTStorageUnit myDSU2 = new DHTStorageUnit(null, -1, valor2);

        community.save(myDSU, valor);
        community.save(myDSU2, null);

        DHTStorageUnit dsu = (DHTStorageUnit) community.read(valor);
        if (dsu != null) {
            System.out.println("with location " + dsu.getContent() + " (value)");
        } else {
            System.out.println("null");
        }
        dsu = (DHTStorageUnit) community.read(null);
        if (dsu != null) {
            System.out.println("location NULL " + dsu.getContent() + " (toto)");
        } else {
            System.out.println("null");
        }

        System.out.println("\n\nSeries 2 - Location, domain");

        String valor3 = "valor3";
        String valor4 = "valor4";

        DHTStorageUnit myDSU3 = new DHTStorageUnit(null, -1, valor3);
        DHTStorageUnit myDSU4 = new DHTStorageUnit(null, -1, valor4);

        community.save(myDSU, "location", "domain");
        community.save(myDSU2, "location", null);
        community.save(myDSU3, null, "domain");
        community.save(myDSU4, null, null);

        dsu = (DHTStorageUnit) community.read("location", "domain");
        if (dsu != null) {
            System.out.println("with location, domain " + dsu.getContent() + " (value)");
        } else {
            System.out.println("null");
        }
        dsu = (DHTStorageUnit) community.read("location", null);
        if (dsu != null) {
            System.out.println("location, NULL " + dsu.getContent() + " (toto)");
        } else {
            System.out.println("null");
        }
        dsu = (DHTStorageUnit) community.read(null, "domain");
        if (dsu != null) {
            System.out.println("with null, domain " + dsu.getContent() + " (valor3)");
        } else {
            System.out.println("null");
        }
        dsu = (DHTStorageUnit) community.read(null, null);
        if (dsu != null) {
            System.out.println("null, NULL " + dsu.getContent() + " (valor4)");
        } else {
            System.out.println("null");
        }

        System.out.println("\n\nSeries 3 - Location, domain, content");

        String valor5 = "valor5";
        String valor6 = "valor6";
        String valor7 = "valor7";
        String valor8 = "valor8";

        DHTStorageUnit myDSU5 = new DHTStorageUnit(null, -1, valor5);
        DHTStorageUnit myDSU6 = new DHTStorageUnit(null, -1, valor6);
        DHTStorageUnit myDSU7 = new DHTStorageUnit(null, -1, valor7);
        DHTStorageUnit myDSU8 = new DHTStorageUnit(null, -1, valor8);

        community.save(myDSU, "location", "domain", "content");
        community.save(myDSU2, "location", "domain", null);
        community.save(myDSU3, "location", null, "content");
        community.save(myDSU4, "location", null, null);
        community.save(myDSU5, null, "domain", "content");
        community.save(myDSU6, null, "domain", null);
        community.save(myDSU7, null, null, "content");
        community.save(myDSU8, null, null, null);

        dsu = (DHTStorageUnit) community.read("location", "domain", "content");
        if (dsu != null) {
            System.out.println("LDC " + dsu.getContent() + " (value)");
        } else {
            System.out.println("null");
        }
        dsu = (DHTStorageUnit) community.read("location", "domain", null);
        if (dsu != null) {
            System.out.println("LDn " + dsu.getContent() + " (toto)");
        } else {
            System.out.println("null");
        }
        dsu = (DHTStorageUnit) community.read("location", null, "content");
        if (dsu != null) {
            System.out.println("LnC " + dsu.getContent() + " (valor3)");
        } else {
            System.out.println("null");
        }
        dsu = (DHTStorageUnit) community.read("location", null, null);
        if (dsu != null) {
            System.out.println("Lnn " + dsu.getContent() + " (valor4)");
        } else {
            System.out.println("null");
        }
        dsu = (DHTStorageUnit) community.read(null, "domain", "content");
        if (dsu != null) {
            System.out.println("nDC " + dsu.getContent() + " (valor5)");
        } else {
            System.out.println("null");
        }
        dsu = (DHTStorageUnit) community.read(null, "domain", null);
        if (dsu != null) {
            System.out.println("nDn " + dsu.getContent() + " (valor6)");
        } else {
            System.out.println("null");
        }
        dsu = (DHTStorageUnit) community.read(null, null, "content");
        if (dsu != null) {
            System.out.println("nnC " + dsu.getContent() + " (valor7)");
        } else {
            System.out.println("null");
        }
        dsu = (DHTStorageUnit) community.read(null, null, null);
        if (dsu != null) {
            System.out.println("nnn " + dsu.getContent() + " (valor8)");
        } else {
            System.out.println("null");
        }

        //Multiversion Test
        System.out.println("\n\nMultiversion ");
        String valornn = "3";
        myDSU = new DHTStorageUnit(null, -1, valornn);
        community.save(myDSU, "toto", "toto", "toto", "a3");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(VersionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        String valor1 = "1";
        myDSU = new DHTStorageUnit(null, -1, valor1);
        community.save(myDSU, "toto", "toto", "toto", "v1");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(VersionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        String valorn = "2";
        myDSU = new DHTStorageUnit(null, -1, valorn);
        community.save(myDSU, "toto", "toto", "toto", "v2");

        ArrayList al2 = (ArrayList) community.read("toto", "toto", "toto", null);
        System.out.println("read " + al2.size());
        for (int j = 0; j < al2.size(); ++j) {
            dsu = (DHTStorageUnit) al2.get(j);
            System.out.println(dsu.getContent());
        }

        dsu = (DHTStorageUnit) community.read("toto", "toto", "toto", "_LAST");
        System.out.println("last version  = " + dsu.getContent());

        end = System.currentTimeMillis();

//                Thread.sleep(3000);
        System.out.println("Total time = " + (end - start));

        Scanner sc = new Scanner(System.in);
        String i = sc.next();
        //}

        System.exit(0);

    }

    private static void usage(Options options) {

        // Use the inbuilt formatter class
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Submitter", options);

    }

}
