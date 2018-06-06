/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.util;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import rice.p2p.commonapi.Id;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class HashUtils {

    /**
     * Util methods used in the EasyPastry applications
     *
     * @author Ruben Mondejar </a>
     */
    public static Id generateHash(Object data) {

        if (data instanceof String) {
            return generateHash(((String) data).getBytes());
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {

            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(data);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] bytes = bos.toByteArray();

        return generateHash(bytes);
    }

    public static Id generateHash(byte[] data) {
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("No SHA support!");
        }

        md.update(data);
        byte[] digest = md.digest();

        Id newId = rice.pastry.Id.build(digest);

        return newId;
    }

    public static String generateStringHash(String data) {
        return generateHash(data).toStringFull();

    }

}
