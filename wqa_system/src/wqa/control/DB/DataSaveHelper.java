/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.control.DB;

import java.util.logging.Level;
import nahon.comm.faultsystem.LogCenter;
import wqa.bill.db.JDBDataTable;
import wqa.bill.db.H2DBSaver;
import wqa.system.WQAPlatform;

/**
 *
 * @author chejf
 */
public class DataSaveHelper {

    private final H2DBSaver db_instance;

    public DataSaveHelper(H2DBSaver db_instance) {
        this.db_instance = db_instance;
    }

    // <editor-fold defaultstate="collapsed" desc="定时设置"> 
    private int time_span = 10;
    private final String Time_Span_Key = "CollectTime";
    private final int max_time = 3600;
    private final int min_time = 1;

    public void SetCollectTime(int time_span) {
        if (time_span > this.max_time) {
            time_span = this.max_time;
        }

        if (time_span < this.min_time) {
            time_span = this.min_time;
        }

        if (this.time_span != time_span) {
            this.time_span = time_span;
            WQAPlatform.GetInstance().GetConfig().setProperty(Time_Span_Key, String.valueOf(time_span));
            WQAPlatform.GetInstance().SaveConfig();
        }
    }
    // </editor-fold>  

    // <editor-fold defaultstate="collapsed" desc="定时采集">     
    //采集数据
    public void SaveData(DataRecord data) {
        this.db_instance.dbLock.lock();
        try {
            JDBDataTable data_dbhelper = new JDBDataTable(this.db_instance);
            //然后创建设备数据表
            data_dbhelper.CreateTableIfNotExist(data);
            //添加数据到设备数据表
            data_dbhelper.AddData(data);
        } catch (Exception ex) {
            LogCenter.Instance().PrintLog(Level.SEVERE, ex);
        } finally {
            this.db_instance.dbLock.unlock();
        }
    }
    // </editor-fold>  
}
