/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.bill.io;

import java.util.concurrent.locks.ReentrantLock;
import nahon.comm.event.EventCenter;
import wqa.bill.io.SDataPacket.IOEvent;

/**
 *
 * @author chejf
 */
public class ShareIO {

    private IAbstractIO io;
    private final ReentrantLock share_lock = new ReentrantLock(true);

    public EventCenter<SDataPacket> SendReceive = new EventCenter();

    public ShareIO(IAbstractIO io) {
        this.io = io;
    }
  
    // <editor-fold defaultstate="collapsed" desc="IO控制">   
    public void Lock() throws Exception {
        share_lock.lock();
    }

    public void UnLock() {
        if (share_lock.isLocked()) {
            share_lock.unlock();
        }
    }  

    public void Open() throws Exception {
        this.io.Open();
    }

    public void Close() {
        this.io.Close();
    }
    
    public SIOInfo GetConnectInfo() {
        return this.io.GetConnectInfo();
    }

    public void SetConnectInfo(SIOInfo info) {
        this.io.SetConnectInfo(info);
    }
    // </editor-fold>  

    // <editor-fold defaultstate="collapsed" desc="IMAbstractIO接口"> 
    public void SendData(byte[] data) throws Exception {
        if (!this.io.IsClosed()) {
            byte[] tmp = new byte[data.length];
            System.arraycopy(data, 0, tmp, 0, data.length);
            this.SendReceive.CreateEventAsync(new SDataPacket(this.io.GetConnectInfo(), IOEvent.Send, tmp));
            this.io.SendData(data);
        }
    }

    public int ReceiveData(byte[] data, int timeout) throws Exception {
        if (!this.io.IsClosed()) {
            int reclen = this.io.ReceiveData(data, timeout);
            if (reclen > 0) {
                byte[] tmp = new byte[reclen];
                System.arraycopy(data, 0, tmp, 0, reclen);
                this.SendReceive.CreateEventAsync(new SDataPacket(this.io.GetConnectInfo(), IOEvent.Receive, tmp));
            }
            return reclen;
        } else {
            return 0;
        }
    }

    public boolean IsClosed() {
        return this.io.IsClosed();
    }
    
    public SIOInfo GetIOInfo() {
        return this.io.GetConnectInfo();
    }

    public int MaxBuffersize() {
        return this.io.MaxBuffersize();
    }
    // </editor-fold>  
}
