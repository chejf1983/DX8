/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.dev;

import base.migp.mem.EIA;
import base.migp.node.MIGP_CmdSend;
import base.pro.absractio.AbstractIO;
import base.pro.absractio.IOInfo;
import base.pro.convert.NahonConvert;
import java.util.HashMap;
import wqa.bill.io.SIOInfo;
import wqa.bill.io.ShareIO;

/**
 *
 * @author chejf
 */
public class MIGPDevFactory {

    public static int DEF_TIMEOUT = 400; //ms
    public static int DEF_RETRY = 3;

    // <editor-fold defaultstate="collapsed" desc="设备类目录">
    private final HashMap<Integer, String> class_map = new HashMap<>();

    public IDevice SearchOneDev(ShareIO io, byte addr) throws Exception {
        //创建一个基础协议包
        MIGP_CmdSend base = new MIGP_CmdSend(Convert(io), (byte) 0xF0, addr);
        //搜索设备基本信息，根据基本信息创建虚拟设备
//        return null;
        return this.BuildDevice(io, (byte) addr, SearchDevType(base));
    }

    //创建设备
    private IDevice BuildDevice(ShareIO io, byte addr, int DevType) throws Exception {
        if (DevType != -1) {
            return new AbsDevice(io, DevType);
        }
        return null;
    }

    public static int dynamic_num = 0;

    //获取设备类型函数
    public static int SearchDevType(MIGP_CmdSend sender) {
        EIA VPA00 = new EIA(0x40, 4);//设备类型地址
        dynamic_num++;
        try {
            dynamic_num = dynamic_num % 3;
            //创建一个基础协议包
            byte[] ret = sender.GetMEM(VPA00, VPA00.length + dynamic_num, 1, 200);

            //搜索设备基本信息，根据基本信息创建虚拟设备
            int devtype = NahonConvert.ByteArrayToInteger(ret, 0);
            //检查是否是A版本的溶解氧
            if (devtype == 0x08000000 || devtype == 0x08000001) {
                return devtype;
            } else {
                return -1;
            }
        } catch (Exception ex) {
            System.out.println(ex);
            return -1;
        }
    }

    public static AbstractIO Convert(ShareIO io) {
        return new AbstractIO() {
            private final ShareIO instance = io;

            @Override
            public boolean IsClosed() {
                return instance.IsClosed();
            }

            @Override
            public void Open() throws Exception {
                this.instance.Open();
            }

            @Override
            public void Close() {
                this.instance.Close();
            }

            @Override
            public void SendData(byte[] data) throws Exception {
                this.instance.SendData(data);
            }

            @Override
            public int ReceiveData(byte[] data, int timeout) throws Exception {

                int len = this.instance.ReceiveData(data, timeout);
//                System.out.println("收到" + len);
                return len;
            }

            @Override
            public IOInfo GetConnectInfo() {
                SIOInfo ioinfo = this.instance.GetIOInfo();
                return new IOInfo(ioinfo.iotype, ioinfo.par);
            }

            @Override
            public int MaxBuffersize() {
                return this.instance.MaxBuffersize();
//                return 65535;
            }
        };
    }
    // </editor-fold> 

}
