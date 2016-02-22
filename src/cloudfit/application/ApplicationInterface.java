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
import java.io.Serializable;

/**
 * ApplicationInterface resumes the behavior CloudFit expects from its
 * applications. Applications running over CloudFit must implement this
 * interface.
 *
 * @author kirsch
 */
public interface ApplicationInterface extends Serializable,Cloneable {

    /**
     * returns total number of blocks to be consumed on a Job
     */
    public int getNumberOfBlocks();

    /**
     * calculates how many blocks a job must consume. Calculated value will next
     * be available through getNumberOfBlocks method.
     *
     * @see #getNumberOfBlocks
     */
    public void numberOfBlocks();

    public void setNumberOfBlocks(int nbBlocks);

    /**
     * returns the accumulator object, which concentrate all results from a Job.
     */
    public Serializable getAccumulator();

    /**
     * defines a new accumulator object. Previous results should be lost after
     * this operation.
     */
    public void setAccumulator(Serializable value);

    
    /**
     * defines the execution arguments for the jobs. These arguments are mostly command-line arguments
     * or parameters from the application profile     *
     */
    public void setArgs(String[] args);
    
    /**
     * defines the execution arguments for the jobs. These arguments are mostly command-line arguments
     * or parameters from the application profile  
     * This method also sets the JobManagerInterface to allow read/write using the StorageAdapter
     */
    public void setArgs(String[] args, JobManagerInterface ts);

    /**
     * retrieves the execution arguments used for the job
     */
    public String[] getArgs();

    /**
     * combines the results from a job, combining results from its differents
     * tasks, adding such results to the accumulator. This method is
     * <i>optional</i>.
     */
    //public Serializable consumeBlock(int number, Serializable value);
    public void consumeBlock(Serializable accumulator, int number, Serializable value);

    /**
     * executes a task for a job.
     */
    public Serializable executeBlock(int number, Serializable[] required);

    /**
     * Consumer starter : initialises the task consumer component. This method
     * is <i>optional</i>.
     */
    public Serializable initializeApplication();

    /**
     * Finalizes the consumer, returing its accumulator. This method is
     * <i>optional</i>.
     */
    public Serializable finalizeApplication();
    
    /**
     * Stores a serialized object using the StorageAdapter. 
     */
    public void save(int tasknumber, Serializable obj, String...keys);
    
    
    /**
     * Reads a serialized object using the StorageAdapter. 
     */
    public Serializable read(String...key);
    
    public void remove(String...key);
    
    
}


