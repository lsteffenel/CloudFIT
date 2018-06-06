/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.core;

import cloudfit.service.JobsScheduler;
import java.util.Properties;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public interface RessourceManagerInterface {

    public int howManyCores();

    public boolean checkRequirements(Properties reqRessources);

    public void setCollectors();

    public WorkData getWork();
    
    public JobsScheduler getJobScheduler();
}
