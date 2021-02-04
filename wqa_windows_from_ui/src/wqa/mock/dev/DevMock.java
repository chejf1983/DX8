/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.mock.dev;

import base.migp.mem.EIA;
import base.migp.mem.MDA;
import base.migp.mem.NVPA;
import base.migp.mem.VPA;
import base.migp.reg.IMEG;
import base.migp.reg.SMEG;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import nahon.comm.event.Event;
import nahon.comm.event.EventListener;

/**
 *
 * @author chejf
 */
public class DevMock {

    // <editor-fold defaultstate="collapsed" desc="DO寄存器"> 
    public SMEG EDEVNAME = new SMEG(new EIA(0x00, 0x10), "设备名称");
    public SMEG EHWVER = new SMEG(new EIA(0x10, 0x01), "硬件版本");
    public SMEG ESWVER = new SMEG(new EIA(0x18, 0x04), "软件版本");
    public SMEG EBUILDSER = new SMEG(new EIA(0x20, 0x10), "序列号");
    public SMEG EBUILDDATE = new SMEG(new EIA(0x30, 0xA), "生产日期");

    public final IMEG EDEVTYPE = new IMEG(new EIA(0x40, 4), "设备类型");  //R  

    public final IMEG VDEVSTAT = new IMEG(new VPA(0x00, 1), "工作状态", 0, 3);  //R  
    public final IMEG VTEST = new IMEG(new VPA(0x01, 1), "启动测试");  //RW  

    public final IMEG NVTESTTIME = new IMEG(new NVPA(0x00, 2), "单次采集周期数", 1, 100);  //RW  
    public final IMEG NVAVR = new IMEG(new NVPA(0x02, 2), "平均次数", 1, 50);  //RW  
    public final IMEG NVCHLS = new IMEG(new NVPA(0x04, 2), "通道使能");  //RW  
    public final IMEG[] NVCHLPAR = new IMEG[]{
        new IMEG(new NVPA(0x10, 2), "波长数据1"), new IMEG(new NVPA(0x12, 2), "波长数据2"),
        new IMEG(new NVPA(0x14, 2), "波长数据3"), new IMEG(new NVPA(0x16, 2), "波长数据4"),
        new IMEG(new NVPA(0x18, 2), "波长数据5"), new IMEG(new NVPA(0x1a, 2), "波长数据6"),
        new IMEG(new NVPA(0x1c, 2), "波长数据7"), new IMEG(new NVPA(0x1e, 2), "波长数据7")};  //RW  

    public final IMEG[] MCHLDATA = new IMEG[]{
        new IMEG(new MDA(0x00, 2), "光强数据1"), new IMEG(new MDA(0x02, 2), "光强数据2"),
        new IMEG(new MDA(0x04, 2), "光强数据3"), new IMEG(new MDA(0x06, 2), "光强数据4"),
        new IMEG(new MDA(0x08, 2), "光强数据5"), new IMEG(new MDA(0x0a, 2), "光强数据6"),
        new IMEG(new MDA(0x0c, 2), "光强数据7"), new IMEG(new MDA(0x0e, 2), "光强数据8")};  //RW  

// </editor-fold> 
    public MigpClient client = new MigpClient();

    public DevMock() {
        client.RegisterREGS(
                EDEVNAME,
                ESWVER,
                EBUILDSER,
                EHWVER,
                EBUILDDATE,
                EDEVTYPE,
                VDEVSTAT,
                VTEST,
                NVTESTTIME,
                NVAVR,
                NVCHLS,
                NVCHLPAR[0], NVCHLPAR[1], NVCHLPAR[2], NVCHLPAR[3],
                NVCHLPAR[4], NVCHLPAR[5], NVCHLPAR[6], NVCHLPAR[7],
                MCHLDATA[0], MCHLDATA[1], MCHLDATA[2], MCHLDATA[3],
                MCHLDATA[4], MCHLDATA[5], MCHLDATA[6], MCHLDATA[7]);

        client.ReadEvent.RegeditListener(new EventListener() {
            @Override
            public void recevieEvent(Event event) {
                try {
                    CreateData();
                } catch (Exception ex) {
                    Logger.getLogger(DevMock.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public void ResetREGS() throws Exception {
        EDEVNAME.SetValue("");
        EBUILDSER.SetValue("");
        ESWVER.SetValue("");
        EBUILDDATE.SetValue("");
        EHWVER.SetValue("");
        EDEVTYPE.SetValue(0x08000000);
        NVCHLPAR[0].SetValue(340);
        NVCHLPAR[1].SetValue(405);
        NVCHLPAR[2].SetValue(450);
        NVCHLPAR[3].SetValue(505);
        NVCHLPAR[4].SetValue(530);
        NVCHLPAR[5].SetValue(560);
        NVCHLPAR[6].SetValue(630);
        NVCHLPAR[7].SetValue(680);
//        for (int i = 0; i < NVCHLPAR.length; i++) {
//            MCHLDATA[i].SetValue(200 + i);
//        }
//        
//            MCHLDATA[0].SetValue(0);
        client.addr = 2;
        client.bandrate = 1;
        WriteREGS();
    }

    Random test = new Random(1);

    public void CreateData() throws Exception {
        for (int i = 0; i < NVCHLPAR.length; i++) {
            MCHLDATA[i].SetValue((int) (40000 + test.nextDouble() * 10));
        }
        WriteREGS();
    }

    public void ReadREGS() throws Exception {
        this.client.Refresh();
    }

    public void WriteREGS() throws Exception {
        this.client.DowloadRegs();
    }
}
