/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.core;

import cloudfit.application.ApplicationInterface;
import cloudfit.application.TaskStatus;
import cloudfit.service.JobManagerInterface;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class WorkData {

    public JobManagerInterface thrSolver;
    public ApplicationInterface app;
    public TaskStatus taskId;

    public WorkData(JobManagerInterface thrSolver, ApplicationInterface app, TaskStatus taskId) {
        this.thrSolver = thrSolver;
        this.app = app;
        this.taskId = taskId;
    }
}
