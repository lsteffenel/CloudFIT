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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * collects and calculates the system CPU load based on multiple observations
 * made with a interval of ms milleseconds between them. The number of
 * observations and the interval can be set as parameters).
 *
 * @author kirsch
 */
public class CPUAverageLoadCollector extends AbstractOSCollector {

    public static final int DEFAULT_NB_OBSERVATIONS = 5;
    public static final int DEFAULT_INTERVAL = 500;
    private int interval;
    private int nbObs;

    public CPUAverageLoadCollector() {
        super.setName("CPULoad");
        super.setDescription("Average System load accros multiple observations");
        this.interval = DEFAULT_NB_OBSERVATIONS;
        this.nbObs = DEFAULT_INTERVAL;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getNbObservation() {
        return nbObs;
    }

    public void setNbObservation(int nbObs) {
        this.nbObs = nbObs;
    }

    @Override
    public List collect() {
        List<Double> results = new ArrayList<>();
        double[] observations = new double[this.nbObs];

        //ready for nbObs observations
        for (int i = 0; i < observations.length; i++) {
            //observing the VM default measure
            observations[i] = getBean().getSystemLoadAverage();
            
            try { //sleep a little before a new observation
                Thread.sleep(this.interval);
            } catch (InterruptedException ex) {
                Logger.getLogger(CPUAverageLoadCollector.class.getName()).log(Level.WARNING,
                        "Interreupted sleep, maybe collecting load information on a shorter interval", ex);
            }
        }

        results.add(this.average(observations));

        return results;
    }

    protected Double average(double obs[]) {
        double moy = 0;

        for (double o : obs) {
            moy += o;
        }

        if (obs.length >= 0) {
            moy /= obs.length;
        } else {
            moy = 0;
        }

        return new Double(moy);
    }
}
