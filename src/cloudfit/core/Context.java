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
package cloudfit.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * @author @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class Context {

    /* ****************************************************************************
     * Proprietes privees
     */
    /**
     * Repertoire de base du contexte.
     */
    private static String baseHome;
    /**
     * Dernier iid utilise localement.
     */
    private static int lastIid;
    private static String fileSeparator;
    private String cloudfitHome;
    private static char pSep;                          // Separateur de chemin


    /* ****************************************************************************
     * Constructeur de classe
     */
    /* ------------------------------------------------------------------------- */
    public Context() {
        //lastIid = -1;
        String str;                         // Chaine de travail
        String tmp = "";                    // Chemin temporaire
        File homeDir;                       // Dossier correspondant à confiitHome

//  Obtention du separateur de fichiers
        try {
            fileSeparator = new String(System.getProperty("file.separator"));
            str = System.getProperty("path.separator");
            pSep = str.charAt(0);
        } catch (Exception e) {
            fileSeparator = new String("/");
            pSep = ':';
        }
        //port = PORT_DMN;
        // Préparation du dossier acceuillant les classes et les résultats
        cloudfitHome = System.getProperty("user.home") + System.getProperty("file.separator") + ".confiit";
        homeDir = new File(cloudfitHome);
        if (!homeDir.exists()) {
            try {
                homeDir.mkdir();
            } catch (Exception e) {
                //Display.fatal("Can't create " + homeDir.getAbsolutePath() + "(" + e.getMessage() + ")");
            }
        }
    }

    /* ****************************************************************************
     * Methodes publiques
     */
    /* ------------------------------------------------------------------------- */
    public static String getSep() {
        return new String(fileSeparator);
    }

    /**
     * Suppression des fichiers temporaires du contexte.
     */
    public static void clean() {
        cleanDirectory(baseHome);
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Obtention des arguments d'execution d'une instance.
     *
     * @return Arguments d'execution de l'instance.
     */
    public static String getBaseHome() {
        return new String(baseHome);
    }


    /* ------------------------------------------------------------------------- */
    /**
     * Obtention des donnees brutes d'une classe.
     *
     * @param iid Identifiant de l'instance.
     * @param name Nom de la classe a charger.
     * @return Donnees brutes de la classe.
     */
    public static byte[] getClassData(String iid, String name) {
        byte[] data = null;             // Code de la classe
        File fc;						// Descripteur du fichier des classes
        InputStream in;					// Flux de lecture

        try {
            fc = new File(baseHome + getSep() + iid + getSep()
                    + "classes" + getSep() + name.replace(".", "/") + ".class");
            if (!fc.exists()) {
                return null;
            }
            in = new FileInputStream(fc);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (Exception e) {
            //Display.error("Unable to load class", e);
        }
        return data;
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Obtention du tableau des fichiers de classes d'une instance.
     *
     * @param iid Identifiant de l'instance.
     * @return Tableau des fichiers de classes de l'instance.
     */
    public static File[] getClasses(String iid) {
        String[] dirList;               // Liste des fichiers du repertoire
        File fDir;                      // Structure d'acces du repertoire
        int i;                          // Variable de boucle
        File[] result;                  // Tableau resultat
        Vector vector;                  // Vecteur des fichiers

        vector = new Vector();
        try {
            fDir = new File(baseHome + getSep() + iid + getSep()
                    + "classes");
            dirList = fDir.list();
//      Rien a faire si le repertoire est vide
            if (dirList == null) {
                return new File[0];
            }
//      Memorisation des fichiers de classes de l'instance
            for (i = 0; i < dirList.length; i++) {
                if (dirList[i].endsWith(".class")) {
                    vector.add(dirList[i]);
                }
            }
//      Construction du tableau resultat
            result = new File[vector.size()];
            for (i = 0; i < vector.size(); i++) {
                result[i] = new File(baseHome + getSep() + iid
                        + getSep() + "classes" + getSep()
                        + (String) vector.elementAt(i));
            }
            return result;
        } catch (Exception e) {
            return new File[0];
        }
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Obtention des arguments d'execution d'une instance.
     *
     * @param iid Identifiant de l'instance.
     * @return Arguments d'execution de l'instance.
     */
    public static String getDirectory(String iid) {
        return new String(baseHome + getSep() + iid);
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Obtention du fichier de trace de la sortie erreur d'une instance.
     *
     * @param iid Identifiant de l'instance.
     * @return Fichier de trace de la sortie erreur de l'instance.
     */
    public static File getErrFile(String iid) {
        return new File(baseHome + getSep() + iid + getSep()
                + "stderr.log");
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Obtention du tableau des fichiers d'&eacute;tat d'instance.
     *
     * @return Tableau des fichiers d'&eacute;tat d'instance.
     */
    public static File[] getInstanceFiles() {
        String[] dirList;               // Liste des fichiers du repertoire
        File fDir;                      // Structure d'acces du repertoire
        int i;                          // Variable de boucle
        File[] result;                  // Tableau resultat
        Vector vector;                  // Vecteur des fichiers

        vector = new Vector();
        try {
            fDir = new File(baseHome);
            dirList = fDir.list();
//      Rien a faire si le repertoire est vide
            if (dirList == null) {
                return new File[0];
            }
//      Memorisation des fichiers d'etat d'instance
            for (i = 0; i < dirList.length; i++) {
                if (dirList[i].endsWith(".sts")) {
                    vector.add(dirList[i]);
                }
            }
//      Construction du tableau resultat
            result = new File[vector.size()];
            for (i = 0; i < vector.size(); i++) {
                result[i] = new File(baseHome + getSep()
                        + (String) vector.elementAt(i));
            }
            return result;
        } catch (Exception e) {
            return new File[0];
        }
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Obtention du fichier de trace d'une instance.
     *
     * @param iid Identifiant de l'instance.
     * @return Fichier de trace de l'instance.
     */
    public static File getLogFile(String iid) {
        return new File(baseHome + getSep() + iid + getSep()
                + iid + ".log");
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Obtention du fichier de trace de la sortie standard d'une instance.
     *
     * @param iid Identifiant de l'instance.
     * @return Fichier de trace de la sortie standard de l'instance.
     */
    public static File getOutFile(String iid) {
        return new File(baseHome + getSep() + iid + getSep()
                + "stdout.log");
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Obtention du fichier de resultat d'une instance.
     *
     * @param iid Identifiant de l'instance.
     * @return Fichier de resultat de l'instance.
     */
    public static File getResultFile(String iid) {
        return new File(baseHome + getSep() + iid + getSep()
                + iid + ".res");
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Obtention du fichier de l'&eacute;tat d'une instance.
     *
     * @param iid Identifiant de l'instance.
     * @return Fichier de l'&eacute;tat de l'instance.
     */
    public static File getStatusFile(String iid) {
        return new File(baseHome + getSep() + iid + ".sts");
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Obtention d'un nouveau iid. Le numero de l'instance creee est stocke dans
     * le fichier de iid du demon (si possible).
     *
     * @return Iid cree.
     */
    public static synchronized String newIid() {
        ObjectInputStream in;          	// Lecteur du fichier de iid
        ObjectOutputStream out;         // Ecriveur du fichier de iid

//	Lire le fichier si premiere demande
        if (lastIid == -1) {
            try {
                in = new ObjectInputStream(new FileInputStream(baseHome
                        + getSep() + "lastiid"));
                lastIid = in.read();
                in.close();
            } catch (Exception e) {
                lastIid = 0;
            }
        }
//	Nouveau numero d'instance
        lastIid++;
        try {
//		Sauvegarde du numero d'instance
            out = new ObjectOutputStream(new FileOutputStream(baseHome
                    + getSep() + "lastiid"));
            out.write(lastIid);
            out.close();
        } catch (Exception e) {
            //Display.error("Can't create lastiid file", e);
        }
//	Creation de l'instance avec un nouveau iid
        //return new String(Config.getAddressPort().replace(':', '_') + "-"
        //        + lastIid);
        return new String("-"
                + lastIid);
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Enregistrement d'une classe utilisateur.
     *
     * @param iid Identifiant de l'instance.
     * @param name Nom de la classe.
     * @param data Donnees brutes de la classe.
     * @return <b>true</b> si la classe a ete enregistree correctement, et
     * <b>false</b> sinon.
     */
    public static synchronized boolean registerClass(String iid, String name,
            byte[] data) {
        String fileName;                // Nom du fichier de classe
        OutputStream out;               // Flux d'ecriture
        String[] pack;
        try {
            fileName = baseHome + getSep() + iid + getSep()
                    + "classes";
            pack = name.split("\\.");
            for (int i = 0; i < pack.length - 1; ++i) {
                fileName = fileName.concat(getSep() + pack[i]);
            }
            new File(fileName).mkdirs();

            //fileName = fileName + Config.getSep () + name + ".class";
            fileName = fileName + getSep() + pack[pack.length - 1] + ".class";
            out = new FileOutputStream(fileName);
            out.write(data);
            out.close();
            return true;
        } catch (FileNotFoundException ex) {
            return false;//Display.error ("Unable to register class - FileNotFound", ex);
        } catch (IOException ex) {
            return false;//Display.error ("Unable to register class - IOException", ex);
        }
        //	catch (Exception e) {
        //	    return Display.error ("Unable to register class", e);
        //		}
        //    }

    }
//	catch (Exception e) {
//	    return Display.error ("Unable to register class", e);
//		}
//    }

    /* ------------------------------------------------------------------------- */
    /**
     * Definition du repertoire de stockage des classes utilisateur.
     *
     * @param value Repertoire de stockage des classes utilisateur.
     */
    public static void setBaseHome(String value) {
//	Memoriser les noms des repertoires de classes et de resultat
        baseHome = new String(value);
//	Creer les repertoires s'ils n'existent pas
        try {
            new File(baseHome).mkdirs();
        } catch (Exception e) {
            //Display.error("Building directories", e);
        }
    }

    /* ****************************************************************************
     * Methodes privees
     */
    /* ------------------------------------------------------------------------- */
    /**
     * Nettoyage d'un repertoire.
     *
     * @param dirname Nom du repertoire a nettoyer.
     */
    private static void cleanDirectory(String dirname) {
        String[] dirList;               // Liste des fichiers du repertoire
        File fDir;                      // Structure d'acces du repertoire
        File file;                      // Manipulateur de fichier
        int i;                          // Variable de boucle

        try {
//		Ouvrir le repertoire
            fDir = new File(dirname);
            dirList = fDir.list();
//		Rien a faire si le repertoire est vide
            if (dirList == null) {
                return;
            }
//		Supprimer les fichiers qu'il contient
            for (i = 0; i < dirList.length; i++) {
                file = new File(dirname + getSep() + dirList[i]);
                if (file.isDirectory() && !file.getName().equals("database")) {
                    cleanDirectory(dirname + getSep() + dirList[i]);
                }
                file.delete();
            }
        } catch (Exception e) {
            //Display.error("cleaning directory " + dirname, e);
        }
    }
}
