/* *************************************************************** *
 * PER-MARE Project (project number 13STIC07)
 * http://cosy.univ-reims.fr/~lsteffenel/per-mare
 * A CAPES/MAEE/ANII STIC-AmSud collaboration program.
 * All rigths reserved to project partners:
 *  - Universite de Reims Champagne-Ardenne, Reims, France 
 *  - Universite Paris 1 Pantheon Sorbonne, Paris, France
 *  - Universidade Federal de Santa Maria, Santa Maria, Brazil
 *  - Universidad de la Republica, Montevideo, Uruguay
 * 
 * *************************************************************** *
 */

package org.permare.context;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * collects available (unallocated) disk storage information by FileSystem and total.  
 * @author kirsch
 */
public class UnallocatedDiskSpaceCollector extends AbstractCollector<Double> {

    public static String COLLECTOR_NAME = "Thing.Device.Storage.Unallocated";
    public static String COLLECTOR_DESCR = "Unallocated storage space by FileSystem and total";
    
    public UnallocatedDiskSpaceCollector() {
        super.setName(COLLECTOR_NAME);
        super.setDescription(COLLECTOR_DESCR);
    }

    @Override
    public List<Double> collect() {
        double total = 0.0;
        List<Double> results = new ArrayList<>();
        
        for (FileStore fs: FileSystems.getDefault().getFileStores()) {
            try {
                //System.out.println(fs.type());
                double space = fs.getUnallocatedSpace() / 1024;
                total += space;
                results.add(new Double(space));
            } catch (IOException ex) {
                Logger.getLogger(AvailableDiskSpaceCollector.class.getName()).log(Level.WARNING, 
                        "Disk space information is unavailable", ex);
            }            
        }
        
        results.add(new Double(total));
        
        return results;
    
    }

    @Override
    public boolean checkValue(Serializable value) {
        return true;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
