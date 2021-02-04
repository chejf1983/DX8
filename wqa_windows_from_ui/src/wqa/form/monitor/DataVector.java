/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.form.monitor;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import nahon.comm.event.EventCenter;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import wqa.control.data.SDataElement;
import wqa.control.data.SDisplayData;

/**
 *
 * @author chejf
 */
public class DataVector {

    private final ReentrantLock datalist_lock = new ReentrantLock();
    private final ArrayList<SDisplayData> datasource = new ArrayList();
    private final int maxlen = 1800;

    // <editor-fold defaultstate="collapsed" desc="公共接口">  
    public EventCenter<SDisplayData> InputData = new EventCenter();

    //输入数据
    public void InputData(SDisplayData data) {
        datalist_lock.lock();
        try {
            while (datasource.size() > this.maxlen) {
                datasource.remove(0);
            }
            datasource.add(data);
            InputData.CreateEvent(data);
        } finally {
            datalist_lock.unlock();
        }
    }

    //清除数据
    public void Clean() {
        datalist_lock.lock();
        try {
            SDisplayData data = this.GetLastData();
            this.datasource.clear();
            this.datasource.add(data);
        } finally {
            datalist_lock.unlock();
        }
    }

    public SDisplayData GetLastData() {
        datalist_lock.lock();
        try {
            if (this.datasource.isEmpty()) {
                return null;
            }

            return this.datasource.get(datasource.size() - 1);
        } finally {
            datalist_lock.unlock();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="MainData Line">  
    public String select_name = "";

    //设置显示曲线
    public void SetSelectName(String name) {
        this.select_name = name;
    }

    private ArrayList<String> describe = new ArrayList();

    public String[] GetDataTimeDescribe() {
        return describe.toArray(new String[0]);
    }

    public TimeSeries GetdateTimeSeries() {
        SDisplayData lastdata = this.GetLastData();
        TimeSeries mainline = new TimeSeries("");
        describe.clear();
        //清空数据
//        mainline.clear();
        //检查数据是否为空
        if (lastdata != null) {
            //找到选择的数据类型序号
            datalist_lock.lock();
            try {
                //遍历数据，找到只当类型的值，添加曲线
                for (int i = 0; i < this.datasource.size(); i++) {
                    if (!Float.isNaN(this.datasource.get(i).GetDataElement(select_name).mainData)) {
                        SDataElement e_data = this.datasource.get(i).GetDataElement(select_name);
                        mainline.addOrUpdate(new Second(this.datasource.get(i).time), e_data.mainData);
                        describe.add(e_data.info);
                    }
                }
            } finally {
                datalist_lock.unlock();
            }
        }

        return mainline;
    }
    // </editor-fold>

    public String[] GetSupportDataName() {
        return SDisplayData.data_names;
    }
}
