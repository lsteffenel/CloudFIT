/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class FileContainer implements Serializable {

    //private ByteArrayInputStream contentS;
    private byte[] content;
    private String name = "";

    public FileContainer(String file) {

        File f = new File(file);
        name = f.getName();
        int length = (int) f.length();
        content = new byte[length];

        FileInputStream fo;
        try {
            fo = new FileInputStream(f);
            fo.read(content);
            fo.close();
            //contentS = new ByteArrayInputStream(content);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileContainer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileContainer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public byte[] getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

}
