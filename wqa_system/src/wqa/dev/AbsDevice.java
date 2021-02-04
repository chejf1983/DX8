/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.dev;

import base.migp.impl.MIGPEia;
import base.migp.mem.MDA;
import base.migp.mem.NVPA;
import base.migp.mem.VPA;
import base.migp.node.MIGP_CmdSend;
import base.migp.reg.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import wqa.bill.io.ShareIO;
import wqa.control.common.CDevDataTable;
import wqa.control.data.DevID;
import wqa.control.data.SConfigItem;
import wqa.control.data.SDevInfo;

/**
 *
 * @author chejf
 */
public class AbsDevice implements IDevice {

    protected MIGP_CmdSend base_drv;
    private final ShareIO local_io;
    public static int DEF_TIMEOUT = 400; //ms
    public static int DEF_RETRY = 3;
    private final int devtype;
    private final int ch_num;

    MIGPEia eiainfo = new MIGPEia(this.base_drv);
//    public final IMEG EDEVTYPE = new IMEG(new EIA(0x40, 4), "设备类型");  //R  

    public final IMEG VDEVSTAT = new IMEG(new VPA(0x00, 1), "工作状态", 0, 3);  //R  
    public final IMEG VTEST = new IMEG(new VPA(0x01, 1), "启动测试");  //RW  

    public final IMEG NVTESTTIME = new IMEG(new NVPA(0x00, 2), "单次采集周期数", 1, 100);  //RW  
    public final IMEG NVAVR = new IMEG(new NVPA(0x02, 2), "平均次数", 1, 50);  //RW  
    public final IMEG NVCHLS = new IMEG(new NVPA(0x04, 2), "通道使能");  //RW  
    public final IMEG[] NVCHLPAR = new IMEG[]{
        new IMEG(new NVPA(0x10, 2), "波长数据1"), new IMEG(new NVPA(0x12, 2), "波长数据2"),
        new IMEG(new NVPA(0x14, 2), "波长数据3"), new IMEG(new NVPA(0x16, 2), "波长数据4"),
        new IMEG(new NVPA(0x18, 2), "波长数据5"), new IMEG(new NVPA(0x1a, 2), "波长数据6"),
        new IMEG(new NVPA(0x1c, 2), "波长数据7"), new IMEG(new NVPA(0x1e, 2), "波长数据8")};  //RW  

    public final IMEG[] MCHLDATA = new IMEG[]{
        new IMEG(new MDA(0x00, 2), "光强数据1"), new IMEG(new MDA(0x02, 2), "光强数据2"),
        new IMEG(new MDA(0x04, 2), "光强数据3"), new IMEG(new MDA(0x06, 2), "光强数据4"),
        new IMEG(new MDA(0x08, 2), "光强数据5"), new IMEG(new MDA(0x0a, 2), "光强数据6"),
        new IMEG(new MDA(0x0c, 2), "光强数据7"), new IMEG(new MDA(0x0e, 2), "光强数据8")};  //RW  

    public AbsDevice(ShareIO io, int devtype) {
        this.base_drv = new MIGP_CmdSend(MIGPDevFactory.Convert(io), (byte) 0xF0, (byte) 0x00);
        this.local_io = io;
        this.devtype = devtype;
        ch_num = CDevDataTable.GetInstance().devmap.get(this.devtype);

    }

    // <editor-fold defaultstate="collapsed" desc="公共接口">
    @Override
    public SDevInfo GetDevInfo() {
        SDevInfo info = new SDevInfo();
        info.dev_id = new DevID(this.devtype, 0x00, eiainfo.EBUILDSER.GetValue());
        info.io = this.local_io;
        return info;
    }

    private int chlnum = 8;

    //初始化设备
    @Override
    public void InitDevice() throws Exception {
        //获取eia信息
        this.ReadMEG(eiainfo.EBUILDDATE, eiainfo.EBUILDSER, eiainfo.EDEVNAME, eiainfo.EHWVER, eiainfo.ESWVER);
        this.ReadMEG(NVTESTTIME, NVAVR, NVCHLS,
                NVCHLPAR[0], NVCHLPAR[1], NVCHLPAR[2], NVCHLPAR[3],
                NVCHLPAR[4], NVCHLPAR[5], NVCHLPAR[6], NVCHLPAR[7]);

        if (this.devtype == 0x08000000) {
            chlnum = 8;
            configs = new MEG[]{NVTESTTIME, NVAVR, NVCHLPAR[0], NVCHLPAR[1], NVCHLPAR[2], NVCHLPAR[3], NVCHLPAR[4], NVCHLPAR[5], NVCHLPAR[6], NVCHLPAR[7]};
        } else {
            chlnum = 4;
            configs = new MEG[]{NVTESTTIME, NVAVR, NVCHLPAR[0], NVCHLPAR[1], NVCHLPAR[2], NVCHLPAR[3]};
        }

        for (int i = 0; i < switchs.length; i++) {
            this.switchs[i] = (NVCHLS.GetValue() & (0x01 << i)) != 0;
        }
    }

    @Override
    public int[] GetNmArray() {
        int[] nmarray = new int[this.ch_num];
        for (int i = 0; i < nmarray.length; i++) {
            nmarray[i] = NVCHLPAR[i].GetValue();
        }
        return nmarray;
    }

    @Override
    public int ReTestType() {
        return MIGPDevFactory.SearchDevType(base_drv);
    }
    // </editor-fold> 

    // <editor-fold defaultstate="collapsed" desc="内部接口">
    protected void ReadMEG(MEG... megs) throws Exception {
        this.base_drv.ReadMEG(DEF_RETRY, DEF_TIMEOUT, megs);
    }

    protected void SetMEG(MEG... megs) throws Exception {
        this.base_drv.SetMEG(DEF_RETRY, DEF_TIMEOUT, megs);
    }
    // </editor-fold>     

    // <editor-fold defaultstate="collapsed" desc="设备信息"> 
    //设备信息
    @Override
    public ArrayList<SConfigItem> GetInfoList() {
        ArrayList<SConfigItem> list = new ArrayList();
        list.add(SConfigItem.CreateRWItem(eiainfo.EDEVNAME.toString(), eiainfo.EDEVNAME.GetValue(), ""));
        list.add(SConfigItem.CreateRItem(eiainfo.EBUILDSER.toString(), eiainfo.EBUILDSER.GetValue().trim(), ""));
        list.add(SConfigItem.CreateRItem(eiainfo.EBUILDDATE.toString(), eiainfo.EBUILDDATE.GetValue(), ""));
        list.add(SConfigItem.CreateRItem(eiainfo.ESWVER.toString(), eiainfo.ESWVER.GetValue(), ""));
        list.add(SConfigItem.CreateRItem(eiainfo.EHWVER.toString(), eiainfo.EHWVER.GetValue(), ""));
        list.add(SConfigItem.CreateRItem("设备类型", String.format("0X%04X", this.devtype), ""));

        for (MEG meg : configs) {
            list.add(SConfigItem.CreateRWItem(meg.toString(), meg.GetValue() + "", ""));
        }

        for (int i = 1; i <= this.ch_num; i++) {
            list.add(SConfigItem.CreateBItem("CH" + i, this.switchs[i - 1]));
        }
        return list;
    }

    private MEG[] configs;
    private boolean[] switchs = new boolean[chlnum];

    @Override
    public void SetInfoList(ArrayList<SConfigItem> list) throws Exception {
        for (SConfigItem item : list) {
            //更新名字
            if (item.IsKey(eiainfo.EDEVNAME.toString())) {
                SetConfigREG(eiainfo.EDEVNAME, item.value);
            }
            if (item.IsKey(eiainfo.EBUILDSER.toString())) {
                SetConfigREG(eiainfo.EBUILDSER, item.value);
            }
            if (item.IsKey(eiainfo.EBUILDDATE.toString())) {
                SetConfigREG(eiainfo.EBUILDDATE, item.value);
            }

            for (MEG meg : configs) {
                if (item.IsKey(meg.toString())) {
                    SetConfigREG(meg, item.value);
                }
            }

            for (int i = 1; i <= this.ch_num; i++) {
                if (item.IsKey("CH" + i)) {
                    switchs[i - 1] = Boolean.valueOf(item.value);
                }
            }
        }

        int tmp = 0;
        for (int i = 0; i < this.ch_num; i++) {
            if (this.switchs[i]) {
                tmp += (0x01 << i);
            }
        }
        SetConfigREG(this.NVCHLS, tmp + "");
    }

    protected void SetConfigREG(MEG reg, String value) throws Exception {
        String lastvalue = reg.GetValue().toString();
        if (!reg.ConmpareTo(reg.Convert(value))) {
            try {
                reg.SetValue(reg.Convert(value));
                this.SetMEG(reg);
            } catch (Exception ex) {
                reg.SetValue(reg.Convert(lastvalue));
                throw ex;
            }
        }
    }
    // </editor-fold>  

        // <editor-fold defaultstate="collapsed" desc="测试接口">
    @Override
    public void StartTest() throws Exception {
        VTEST.SetValue(17);
        this.SetMEG(VTEST);
    }

    @Override
    public int[] CollectData() throws Exception {
        boolean is_data_ok = false;
        while (!is_data_ok) {
            this.base_drv.ReadMEG(DEF_RETRY, DEF_TIMEOUT, VDEVSTAT);
            if (VDEVSTAT.GetValue() == 0) {
                is_data_ok = true;
            }
            if (VDEVSTAT.GetValue() == 1) {
                TimeUnit.MILLISECONDS.sleep(300);
            }
            if (VDEVSTAT.GetValue() == 2) {
                throw new Exception("故障中");
            }
        }

        this.base_drv.ReadMEG(DEF_RETRY, DEF_TIMEOUT,
                MCHLDATA[0], MCHLDATA[1], MCHLDATA[2], MCHLDATA[3],
                MCHLDATA[4], MCHLDATA[5], MCHLDATA[6], MCHLDATA[7]);
        int[] ret = new int[this.ch_num];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = MCHLDATA[i].GetValue();
        }
        return ret;
    }
    // </editor-fold> 

}
