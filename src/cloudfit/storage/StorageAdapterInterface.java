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

import java.io.Serializable;

/**
 * Generic definition of the methods associated to the storage of data
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public interface StorageAdapterInterface {

    public void save(Serializable value, String... keys);

    public void blocking_save(Serializable value, String... keys);

    public boolean contains(String... keys);

    public Serializable read(String... keys);

    public void remove(String... keys);
}
