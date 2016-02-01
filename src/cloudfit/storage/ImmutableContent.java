/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.storage;

import cloudfit.util.MultiMap;
import java.io.Serializable;
import java.util.ArrayList;
import rice.p2p.commonapi.Id;
import rice.p2p.past.ContentHashPastContent;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastException;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class ImmutableContent extends ContentHashPastContent implements Serializable, DHTContent {

    /**
     * Store the content.
     *
     * Note that this class is Serializable, so any non-transient field will
     * automatically be stored to to disk.
     */
    private Serializable content;

    @Override
    public Serializable getContent() {
        return content;
    }

    /**
     * Takes an environment for the timestamp An IdFactory to generate the hash
     * The content to be stored.
     *
     * @param id to generate a hash of the content
     * @param content to be stored
     */
    public ImmutableContent(Id id, Serializable content) {
        super(id);
        this.content = content;
    }

    /**
     * A descriptive toString()
     */
    public String toString() {
        return "MyPastContent [" + content + "]";
    }

    @Override
    public boolean isMutable() {
        return false;
    }

//    @Override
//    public PastContent checkInsert(Id id, PastContent existingContent)
//            throws PastException {
//
//        // only allow correct content hash key
//        if (!id.equals(getId())) {
//            throw new PastException(
//                    "ContentHashPastContent: can't insert, content hash incorrect");
//        }
//
//        if (existingContent != null) {
////
//            if (!(((ImmutableContent) existingContent).getContent() instanceof DHTStorageUnit)) {
//                throw new PastException(
//                        "ImmutableContent : can't insert, existing object for the TID("
//                        + this.myId + ") is of unknown class type" + ((ImmutableContent) existingContent).getContent().getClass());
//            } else {
//                synchronized (existingContent) {
//                    // Update existing Catalog entry
//                    ImmutableContent ctt = (ImmutableContent) existingContent;
//                    DHTStorageUnit newcontent = (DHTStorageUnit) content;
//                    DHTStorageUnit original = (DHTStorageUnit) ctt.getContent();
//                    // if the same task produced the data, ignore. Otherwise, merge data
//                    if (!(newcontent.getJobId() == original.getJobId() && newcontent.getTaskId() == original.getTaskId())) {
//                        MultiMap originalmap = (MultiMap) original.getContent();
//                        MultiMap newmapdata = (MultiMap) newcontent.getContent();
//                        newmapdata.putAll(originalmap);
//                        //System.err.println("Data inserted");
//                    } else {
//                        //System.err.println("same taskid, ignoring"+newcontent.getJobId()+"-"+newcontent.getTaskId());
//                    }
//                }
//            }
//        }
//
//        return this; 
//    }
}
