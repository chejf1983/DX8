/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.control.DB;

import java.util.Date;
import java.util.logging.Level;
import nahon.comm.faultsystem.LogCenter;
import wqa.bill.db.H2DBSaver;
import wqa.bill.db.JDBDataTable;
import wqa.control.data.DevID;
import wqa.system.WQAPlatform;

/**
 *
 * @author chejf
 */
public class DBFixHelper {

    private final H2DBSaver db_instance;

    public DBFixHelper(H2DBSaver db_instance) {
        this.db_instance = db_instance;
    }

    //删除表
    public void DeleteData(Date beforetime, wqa.control.data.IMainProcess process) {
        WQAPlatform.GetInstance().GetThreadPool().submit(new Runnable() {
            @Override
            public void run() {
                db_instance.dbLock.lock();
                try {
                    JDBDataTable dtable = new JDBDataTable(db_instance);
                    //获取所有数据表
                    DevID[] dev_data_tables = dtable.ListAllDevice();
                    for (int i = 0; i < dev_data_tables.length; i++) {
                        //删除表之前的数据
                        dtable.DeleteData(dev_data_tables[i], beforetime);
                        //如果表空了，删除表
                        if (dtable.IsTableEmpty(dev_data_tables[i])) {
                            new JDBDataTable(db_instance).DropTable(dev_data_tables[i]);
                        }
                        process.SetValue((float) i * 100 / (float) dev_data_tables.length);
                    }
                } catch (Exception ex) {
                    LogCenter.Instance().SendFaultReport(Level.SEVERE, "获取设备列表失败", ex);
                } finally {
                    db_instance.dbLock.unlock();
                    process.Finish(null);
                }
            }
        });
    }

    //return xx MB
    public float GetDBSize() {
        return this.db_instance.GetDBSize();
    }
}
