/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class FileTools {
      /**
     * looks for input files on the arguments. If argument is a directory, it
     * includes all files inside, recursively.
     */
    public static List<String> loadInput(String dir) {
            List<String> filenames = new ArrayList<String>();
            File target = new File(dir);

            if (target.isDirectory()) {
                addDirectoryFiles(filenames, target);
            } else { // target is a file
                filenames.add(target.getPath());
            }
        
        Collections.sort(filenames);
        return filenames;
    }

    private static boolean addDirectoryFiles(List<String> filenames, File target) {

        if (!target.isDirectory()) {
            filenames.add(target.getPath());
            return false;
        }

        File[] listOfFiles = target.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                addDirectoryFiles(filenames,file);
            }
        }
        return true;
    }
    
}
