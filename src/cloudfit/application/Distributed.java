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
package cloudfit.application;

import cloudfit.service.JobManagerInterface;
import cloudfit.storage.DHTStorageUnit;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Abstract class to define some methods used by an application in distributed
 * mode
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public abstract class Distributed implements ApplicationInterface, Serializable {

    /**
     * Valeur de l'accumulateur.
     */
    private Serializable accumulator;
    /**
     * Arguments de lancement du composant.
     */
    private String[] args;
    /**
     * Nombre de blocs dans le segment.
     */
    private int nbBlocks;

    protected JobManagerInterface threadSolve;

    /* ------------------------------------------------------------------------- */
    /**
     * Obtention du nombre de blocs. Cette methode est disponible pour les
     * methodes utilisateur du programme a definir.
     *
     * @return Nombre de blocs.
     */
// Commented because confusion with nuberOfBlocks. Using one single method
//   @Override
//    public  int getNumberOfBlocks () {
//    return nbBlocks;
//    }
    @Override
    public void setArgs(String[] args, JobManagerInterface ts) {
        this.args = args;
        this.threadSolve = ts;
    }

    @Override
    public void setArgs(String[] args) {
        this.args = args;
    }

    @Override
    public String[] getArgs() {
        return args;
    }

    /**
     * Finalizes the consumer, returing its accumulator. This method is
     * <i>optional</i>.
     */
    public Serializable finalizeApplication(ArrayList tasksResults) {
        // do nothing here, it's optional
        return null;
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Obtention de la valeur de l'accumulateur. Cette methode est disponible
     * pour les methodes utilisateur.
     *
     * @return Valeur de l'accumulateur.
     */
    @Override
    public Serializable getAccumulator() {
        return accumulator;
    }

    /* ------------------------------------------------------------------------- */
    /**
     * Definition de la valeur de l'accumulateur.
     *
     * @param value Nouvelle valeur de l'accumulateur.
     */
    @Override
    public void setAccumulator(Serializable value) {
        accumulator = null;
        accumulator = value;
    }

    @Override
    public int getNumberOfBlocks() {
        return nbBlocks;
    }

    @Override
    public void setNumberOfBlocks(int nbBlocks) {
        this.nbBlocks = nbBlocks;
    }

    @Override
    public void save(int tasknumber, Serializable obj, String... keys) {
        DHTStorageUnit dsu = new DHTStorageUnit(threadSolve.getJobId(), tasknumber, obj);
        threadSolve.save(dsu, keys);
    }

    /**
     * Reads a serialized object using the StorageAdapter.
     */
    @Override
    public Serializable read(String... key) {
        return threadSolve.read(key);
    }

    @Override
    public void remove(String... key) {
        threadSolve.remove(key);
    }

}
