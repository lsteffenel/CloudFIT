package cloudfit.network;


import java.io.Serializable;
import net.tomp2p.peers.Number160;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Luiz Angelo STEFFENEL <Luiz-Angelo.Steffenel@univ-reims.fr>
 */
public class TomP2PMessage implements Serializable{
    
    public static int UNICAST = 0;
    public static int BCAST = 1;
    private int type; // 0 = unicast, 1 = bcast
    private Number160 senderId;
    private long sequenceNumber;
    private Serializable content;
    
    public TomP2PMessage(int type, Number160 senderId, long sequenceNumber, Serializable content)
    {
        this.type = type;
        this.senderId = senderId;
        this.sequenceNumber = sequenceNumber;
        this.content = content;
    }

    public Number160 getSenderId() {
        return senderId;
    }

    public void setSenderId(Number160 senderId) {
        this.senderId = senderId;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long seqnum) {
        this.sequenceNumber = seqnum;
    }
    
    public Serializable getContent() {
        return content;
    }

    public void setContent(Serializable content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    
    
}
