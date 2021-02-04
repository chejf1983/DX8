/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.control.common;

import java.math.BigDecimal;
import nahon.comm.event.EventCenter;
import wqa.control.data.SDisplayData;

/**
 *
 * @author chejf
 */
public class DevMonitor {

    private DevControl parent;
    public int index;

    public DevMonitor(DevControl parent, int index) {
        this.parent = parent;
        this.index = index;
    }

    public DevControl GetParent() {
        return this.parent;
    }

    // <editor-fold defaultstate="collapsed" desc="采集状态"> 
    public enum State {
        FREE,
        WORKING,
        ERROR
    }
    public State currentState = State.FREE;
    // </editor-fold>   

    // <editor-fold defaultstate="collapsed" desc="设置参考光"> 
    public void SetRefData() {
        this.base_data = lastdata;
        base_enable = true;
    }
    private int lastdata = 0;
    public int base_data = 0;
    boolean base_enable = false;

    private float GetAbsValue() {
        double tmp;
        double tbase = base_data == 0 ? 0.001d : this.base_data;
        double tlast = lastdata == 0 ? 0.001d : this.lastdata;

        if (tbase < tlast) {
            tmp = 0 - Math.log10(tlast / tbase);
        } else {
            tmp = Math.log10(tbase / tlast);
        }
//        System.out.println(this.base_data + ":" + this.lastdata + ":"+ tmp);
        return new BigDecimal(tmp).setScale(5, BigDecimal.ROUND_HALF_UP).floatValue();
    }
    // </editor-fold>   

    // <editor-fold defaultstate="collapsed" desc="采集数据"> 
    public EventCenter<SDisplayData> DataEvent = new EventCenter();

    public SDisplayData CollectData(int data) {
        SDisplayData tmpdata = new SDisplayData(GetParent().GetConnectInfo().dev_id,  this.parent.device.GetNmArray()[index]);
        tmpdata.datas[0].mainData = (float) data;
        this.lastdata = data;
        if (base_enable) {
            tmpdata.datas[1].mainData = GetAbsValue();
            tmpdata.datas[1].info = "参比光强:" + base_data;
        }
        DataEvent.CreateEvent(tmpdata);
        return tmpdata;
    }
    // </editor-fold>   
}
