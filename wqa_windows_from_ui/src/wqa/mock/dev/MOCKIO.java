/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.mock.dev;

import java.util.logging.Level;
import java.util.logging.Logger;
import wqa.bill.io.IAbstractIO;
import wqa.bill.io.SIOInfo;

/**
 *
 * @author chejf
 */
public class MOCKIO implements IAbstractIO {

    private boolean isclosed = true;
    private MigpClient client = null;

    private static MOCKIO io;

    public static MOCKIO CreateIO() {
        if (io == null) {
            try {
                DevMock dev_mock = new DevMock();
                dev_mock.ResetREGS();
                io = new MOCKIO(dev_mock.client);
            } catch (Exception ex) {
                Logger.getLogger(MOCKIO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return io;
    }

    public MOCKIO(MigpClient client) {
        this.client = client;
    }

    @Override
    public boolean IsClosed() {
        return isclosed;
    }

    @Override
    public void Open() throws Exception {
        this.isclosed = false;
    }

    @Override
    public void Close() {
        this.isclosed = true;
    }

    @Override
    public void SendData(byte[] data) throws Exception {
        PrintLog.PrintIO("SEN:");
        for (int i = 0; i < data.length; i++) {
            PrintLog.PrintIO(String.format("%02X ", data[i]));
        }
        PrintLog.PrintlnIO("");

        this.client.ReceiveCmd(data);
    }

    @Override
    public int ReceiveData(byte[] data, int timeout) throws Exception {
        byte[] mem = this.client.Reply();
        System.arraycopy(mem, 0, data, 0, mem.length);
        if (mem.length > 0) {
            PrintLog.PrintIO("REC:");
            for (int i = 0; i < mem.length; i++) {
                PrintLog.PrintIO(String.format("%02X ", mem[i]));
            }
            PrintLog.PrintlnIO("");
        }
        return mem.length;
    }

    @Override
    public SIOInfo GetConnectInfo() {
        return new SIOInfo(SIOInfo.COM, "COM9", "9600");
    }

    @Override
    public int MaxBuffersize() {
        return 65535;
    }

    @Override
    public void SetConnectInfo(SIOInfo info) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
