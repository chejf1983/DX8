/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.control.DB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import wqa.bill.db.JDBDataTable;
import wqa.control.common.CDevDataTable;
import wqa.control.data.DevID;

/**
 *
 * @author chejf
 */
public class DataRecord {

    //有效数据
    public Float[] values;
    //时间
    public Date time;
    //DB数据信息
    public DevID dev_info;

    private String[] names;

    public DataRecord(DevID dev_info) {
        //获取DB显示数据
        this.dev_info = dev_info;
        //赋值数据值
        this.values = new Float[CDevDataTable.GetInstance().devmap.get(dev_info.dev_type) * 3];

        names = new String[values.length];
        
        for (int i = 0; i < names.length; i+=3) {
            int cl = (int)i / 3 + 1;
            names[i ] = "通道" + cl + "光强";
            names[i  + 1] = "通道" + cl + "吸光度";
            names[i  + 2] = "通道" + cl + "参考光";
        }   
    }

    public DataRecord(DevID dev_info, ResultSet set) throws SQLException {
        this(dev_info);
        this.InitData(set);
    }

    public void InitData(ResultSet set) throws SQLException {
        //读取时间
        this.time = set.getTimestamp(JDBDataTable.Time_Key);

        //获取静态数据表
        for (int i = 0; i < values.length; i++) {
            //根据显示数据内容查找静态数据表的序号，对应到数据库中的位置
            values[i] = set.getFloat(JDBDataTable.DataIndexKey + i);
        }
    }

    public String[] GetNames() {
        return names;
    }

    public String[] GetExlNames() {
        String[] ret = new String[names.length + 1];
        ret[0] = "时间";
        System.arraycopy(names, 0, ret, 1, names.length);
        return ret;
    }

    public Object[] GetExlValue() {
        Object[] ret = new Object[this.values.length + 1];
        ret[0] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.time);
        for (int i = 0; i < values.length; i++) {
            ret[i + 1] = values[i];
        }
        return ret;
    }
}
