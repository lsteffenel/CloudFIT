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
import java.util.ArrayList;
import java.util.List;
import java.nio.file.FileSystems;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * discovers the available disk space on the file systems
 *
 * @author kirsch
 */
public class AvailableDiskSpaceCollector extends AbstractOSCollector<FileStoreStruct> {

    public static String COLLECTOR_NAME = "Thing.Device.Storage.Available";
    public static String COLLECTOR_DESCR = "Available disk space.";

    public AvailableDiskSpaceCollector() {
        super.setName(COLLECTOR_NAME);
        super.setDescription(COLLECTOR_DESCR);
    }

    @Override
    public List collect() {
        int availFS = 0;
        List<FileStoreStruct> results = new ArrayList<>();

        for (FileStore fs : FileSystems.getDefault().getFileStores()) {
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
    public boolean checkValue(Serializable value) {
        List<FileStoreStruct> disks = this.collect();
        // checks free space in each available disk. If one has space, says ok
        boolean hasSpace = false;
        Iterator it = disks.iterator();
        while (it.hasNext()) {
            FileStoreStruct disk = (FileStoreStruct) it.next();
            Double space = disk.freeSpace;
            if ((Double) value <= space) {
                hasSpace = true;
            }
        }
        return hasSpace;
    }

}
