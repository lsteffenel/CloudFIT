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
import java.nio.file.FileStore;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.FileSystems;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * discovers the available disk space on the file systems
 * @author kirsch
 */
public class AvailableDiskSpaceCollector implements Collector<FileStoreStruct> {

    @Override
    public List collect() {
        int availFS = 0;        
        List<FileStoreStruct> results = new ArrayList<>();
        
        for (FileStore fs: FileSystems.getDefault().getFileStores()) {
            availFS++;
            try {
                FileStoreStruct fss = new FileStoreStruct();
                //System.out.println(fs.type());
                //results.add(fs);
                //results.add(new Double(fs.getUnallocatedSpace() / 1024));
                fss.freeSpace = fs.getUsableSpace() / 1024;
                fss.path = fs.name();
                fss.store = fs;
                //results.add(new Double(fs.getUsableSpace() / 1024));
                results.add(fss);
            } catch (IOException ex) {
                Logger.getLogger(AvailableDiskSpaceCollector.class.getName()).log(Level.WARNING, 
                        "Disk space information is unavailable", ex);
            }            
        }
        
        //results.add(new Double(availFS));
        
        return results;
    }

    @Override
    public String getCollectorName() {
        return "Available Storage";
    }

    @Override
    public String getCollectorDescription() {
        return "Available disk space (unallocated and usable) on the file systems and total nb of FS";
    }
    
    
}
