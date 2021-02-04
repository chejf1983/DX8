/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.control.data;

import java.util.Date;

/**
 *
 * @author chejf
 */
public class SDisplayData {

    public static String[] data_names = new String[]{"光强", "吸光度"};

    public SDisplayData(DevID dev_id, int nm) {
        this.dev_id = dev_id;
        this.nm = nm;
        this.time = new Date();

        //单位信息
        datas = new SDataElement[data_names.length];
        for (int i = 0; i < datas.length; i++) {
            datas[i] = new SDataElement();
            datas[i].name = data_names[i];
            datas[i].mainData = Float.NaN;
        }
    }

    public SDataElement GetDataElement(String nametype) {
        for (SDataElement data : this.datas) {
            if (data.name.contentEquals(nametype)) {
                return data;
            }
        }

        return null;
    }
    public final DevID dev_id;
    public int nm;
    public Date time;
    public SDataElement[] datas = new SDataElement[0];
}
