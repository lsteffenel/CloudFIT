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

package cloudfit.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic storage implementation that stores the data as Java serialized objects
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class SerializedDiskStorage implements StorageAdapterInterface {

    /**
     * Stores the given data in a filename "key"
     * @param key
     * @param value 
     */
    @Override
    public void save(String key, Serializable value, boolean mutable) {
        try {
            //use buffering
            OutputStream file = new FileOutputStream(key);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            try {
                output.writeObject(value);
                output.flush();
            } finally {
                output.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(SerializedDiskStorage.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /**
     * Stores the given data in a filename "key"
     * @param key
     * @param value 
     */
    @Override
    public void blocking_save(String key, Serializable value, boolean mutable) {
        this.save(key,value, mutable);
    }

    /**
     * reads the file named "key"
     * @param key
     * @return 
     */
    @Override
    public Serializable read(String key) {
        Serializable element = null;
        try {
            //use buffering
            InputStream file = new FileInputStream(key);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            try {
                //deserialize the List
                element = (Serializable) input.readObject();

            } finally {
                input.close();
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SerializedDiskStorage.class.getName()).log(Level.SEVERE, "Cannot perform deserializing", ex);
        } catch (IOException ex) {
            Logger.getLogger(SerializedDiskStorage.class.getName()).log(Level.SEVERE, "Cannot perform input", ex);
        }
        return element;
    }

    /**
     * erases the file named "key"
     * @param key 
     */
    @Override
    public void remove(String key) {
        File file = new File(key);
        if (file.delete()) {
            System.err.println(file.getName() + " is deleted!");
        } else {
            System.err.println("Delete operation is failed.");
        }
    }
}
