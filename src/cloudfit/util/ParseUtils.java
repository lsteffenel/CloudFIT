/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.ParseException;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class ParseUtils {

    /**
     * Generate a InetSocketAddress from a string of the form "[host][:port]".
     * WORKS ONLY WITH IPV4
     *
     * @param s the string containing the addresses.
     * @param defaultHost Host to use if the string doesn't contain a host part.
     * If <b>null</b> string must contain a host part.
     * @param defaultPort Port to use if the string doesn't contain a port part.
     * If <b>-1</b> string must contain a host part.
     * @return a parsed address
     * @throws ParseException
     * @throws UnknownHostException
     */
    public static InetSocketAddress parseSocketAddress(String s, String defaultHost, int defaultPort)
            throws ParseException, UnknownHostException {
        InetSocketAddress addr = null;
        final int iport = s.indexOf(':');
        if (iport < 0) {
            if (defaultPort < 0) {
                throw new ParseException("Missing port in \"" + s + "\"", 0);
            }
            addr = new InetSocketAddress(InetAddress.getByName(s), defaultPort);
        } else if (iport == 0) {
            if (defaultHost == null) {
                throw new ParseException("Missing host in \"" + s + "\"", iport);
            }
            addr = new InetSocketAddress(defaultHost, Integer.parseInt(s.substring(1)));
        } else if (iport < s.length() - 1) {
            addr = new InetSocketAddress(InetAddress.getByName(s.substring(0, iport)), Integer.parseInt(s.substring(iport + 1)));
        } else {
            throw new ParseException("Missing port in \"" + s + "\"", iport);
        }

        return addr;
    }

}
