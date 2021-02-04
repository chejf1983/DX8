/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.control.common;

import java.util.ArrayList;
import java.util.logging.Level;
import nahon.comm.faultsystem.LogCenter;
import wqa.bill.io.ShareIO;
import wqa.control.data.SConfigItem;
import wqa.dev.IDevice;

/**
 *
 * @author chejf
 */
public class DevConfigTable {
    
    private IDevice dev;
    private DevControl parent;
    
    public DevConfigTable(IDevice dev, DevControl parent) {
        this.dev = dev;
        this.parent = parent;
    }
    
    public String GetValue(String key) {
        for (SConfigItem item : dev.GetInfoList()) {
            if (item.data_name.contentEquals(key)) {
                return item.value;
            }
        }
        return "";
    }
    
    public void InitConfigTable() {
        try {
            ((ShareIO) dev.GetDevInfo().io).Lock();
            this.dev.InitDevice();
//            this.msg_instance.UpdateConfigEvent.CreateEvent(null);
        } catch (Exception ex) {
            LogCenter.Instance().SendFaultReport(Level.SEVERE, ex);
        } finally {
            ((ShareIO) dev.GetDevInfo().io).UnLock();
        }
    }
    
    public SConfigItem[] GetConfigList() {
        try {
            ArrayList<SConfigItem> GetInfoList = dev.GetInfoList();
            GetInfoList.add(SConfigItem.CreateRWItem("采集周期", parent.getCollectlag() / 1000 + "", "秒"));
            return GetInfoList.toArray(new SConfigItem[0]);
        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }
    }
    
    public String GetListName() {
        return this.dev.GetDevInfo().dev_id.ToChineseString();
    }
    
    public void SetConfigList(SConfigItem[] list) {
        try {
            
            ((ShareIO) dev.GetDevInfo().io).Lock();
            ArrayList<SConfigItem> changelist = new ArrayList();
            for (int i = 0; i < list.length; i++) {
                if (list[i].IsKey("采集周期")) {
                    int lag = Integer.valueOf(list[i].value) * 1000;
                    if(lag < 1000){
                        lag = 1000;
                    }
                    parent.setCollectlag(lag);
                } else if (list[i].IsChanged()) {
                    changelist.add(list[i]);
                    list[i].Updated();
                }
            }
            dev.SetInfoList(changelist);
            LogCenter.Instance().ShowMessBox(Level.SEVERE, "设置成功");
        } catch (Exception ex) {
            LogCenter.Instance().SendFaultReport(Level.SEVERE, ex);
        } finally {
            ((ShareIO) dev.GetDevInfo().io).UnLock();
        }
    }
    
}
