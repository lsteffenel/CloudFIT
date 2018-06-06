/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.storage;

import cloudfit.util.Number160;
import java.io.Serializable;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class DHTStorageUnit implements Serializable {

    private Number160 jobId = null;
    private int taskId = 0;
    private Serializable content = null;

    public DHTStorageUnit(Number160 jid, int tid, Serializable cont) {
        jobId = jid;
        taskId = tid;
        content = cont;
    }

    public Serializable getContent() {
        return content;
    }

    public int getTaskId() {
        return taskId;
    }

    public Number160 getJobId() {
        return jobId;
    }
}
