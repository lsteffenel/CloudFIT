/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudfit.storage;

import java.io.Serializable;
import rice.p2p.commonapi.Id;
import rice.p2p.past.ContentHashPastContent;

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class OldImmutableContent extends ContentHashPastContent implements DHTContent {

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
     * @param idf to generate a hash of the content
     * @param content to be stored
     */
    public OldImmutableContent(Id id, Serializable content) {
        super(id);
        this.content = content;
    }

    /**
     * A descriptive toString()
     */
    public String toString() {
        return "MyPastContent [" + content + "]";
    }
}
