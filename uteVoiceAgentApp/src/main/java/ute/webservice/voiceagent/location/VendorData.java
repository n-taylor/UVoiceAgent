package ute.webservice.voiceagent.location;

/**
 * Stores data conveyed about tag vendors
 * Created by Nathan Taylor on 6/1/2018.
 */

public class VendorData {

    private long vendorId;
    private int elementId;
    private String data;
    private String lastReceivedTime;
    private int lastReceivedSeqNum;


    public VendorData(long vendorId, int elementId, String data, String lastReceivedTime, int lastReceivedSeqNum){
        this.vendorId = vendorId;
        this.elementId = elementId;
        this.data = data;
        this.lastReceivedTime = lastReceivedTime;
        this.lastReceivedSeqNum = lastReceivedSeqNum;
    }


    public long getVendorId() {
        return vendorId;
    }

    public int getElementId() {
        return elementId;
    }

    public String getData() {
        return data;
    }

    public String getLastReceivedTime() {
        return lastReceivedTime;
    }

    public int getLastReceivedSeqNum() {
        return lastReceivedSeqNum;
    }
}
