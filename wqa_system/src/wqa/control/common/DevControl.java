/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.control.common;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import nahon.comm.event.EventCenter;
import nahon.comm.faultsystem.LogCenter;
import wqa.bill.io.ShareIO;
import wqa.control.DB.DataRecord;
import wqa.control.data.SDevInfo;
import wqa.control.data.SDisplayData;
import wqa.dev.IDevice;
import wqa.system.WQAPlatform;

/**
 *
 * @author chejf
 */
public class DevControl {

    public DevControl(IDevice device) {
        this.device = device;
        this.initDevMonitor();

        try {
            Integer valueOf = Integer.valueOf(WQAPlatform.GetInstance().GetConfig().getProperty("CTIME", "2000"));
            this.setCollectlag(valueOf);
        } catch (Exception ex) {

        }
    }

    // <editor-fold defaultstate="collapsed" desc="控制器状态"> 
    public enum ControlState {
        CONNECT,
        DISCONNECT
    }

    private ControlState state = ControlState.DISCONNECT;
    private final ReentrantLock state_lock = new ReentrantLock(true);

    public ControlState GetState() {
        return this.state;
    }
    public EventCenter<ControlState> StateChange = new EventCenter();

    public void ChangeState(ControlState state, String info) {
        if (this.state != state) {
            this.state = state;
            this.StateChange.CreateEvent(state, info);
            LogCenter.Instance().PrintLog(Level.SEVERE, "切换状态->" + state);
        }
    }

    public void ChangeState(ControlState state) {
        ChangeState(state, "");
    }
    // </editor-fold>    

    // <editor-fold defaultstate="collapsed" desc="基本信息"> 
    final IDevice device;

    public SDevInfo GetConnectInfo() {
        return device.GetDevInfo();
    }
    // </editor-fold>    

    // <editor-fold defaultstate="collapsed" desc="动作"> 
    private Date last_time = new Date();
    private int keepalivelag = 2000;
    private int collectlag = 2000;//采样间隔
    private boolean start_collect = false;//运行开关
    private int run_time = -1; //运行时间 ms
    private int run_lag = 100; //ms
    private ListenImp<Boolean> impl;

    public void setRuntime(int run_time, ListenImp<Boolean> impl) {
        this.run_time = run_time * 1000;
        this.impl = impl;
    }

    public int getRuntime() {
        return (int) (this.run_time / 1000);
    }

    public boolean isStart_collect() {
        return start_collect;
    }

    public void setStart_collect(boolean start_collect) {
        last_time = new Date();
        this.start_collect = start_collect;
        if (this.impl != null) {
            impl.Call(start_collect);
        }
    }

    public int getCollectlag() {
        return collectlag;
    }

    public void setCollectlag(int collectlag) {
        if (collectlag != this.collectlag) {
            this.collectlag = collectlag;
            WQAPlatform.GetInstance().GetConfig().setProperty("CTIME", collectlag + "");
        }
    }

    private void KeepAlive() throws Exception {
        if (this.keepalivelag > 0) {
            this.keepalivelag -= run_lag;
            return;
        }
        this.keepalivelag = 2000;

        //其他状态下，开心跳检查重连设备
        for (int j = 0; j < 2; j++) {
            if (ReConnect()) {
                if (GetState() == ControlState.DISCONNECT) {
                    device.InitDevice();
                    ChangeState(ControlState.CONNECT);
                }
                return;
            }
        }
        //心跳包多一次检查
        ChangeState(ControlState.DISCONNECT);
    }

    public boolean ReConnect() {
        try {
            int devtype = this.device.ReTestType();
            return this.device.GetDevInfo().dev_id.dev_type == devtype;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean CollectData() {
        try {
            this.device.StartTest();
            int[] datas = this.device.CollectData();
            DataRecord record = new DataRecord(this.device.GetDevInfo().dev_id);
            record.time = new Date();
            for (int i = 0; i < datas.length; i++) {
                SDisplayData spdata = this.collect_instances[i].CollectData(datas[i]);
                record.values[i * 3] = spdata.datas[0].mainData;
                if (this.collect_instances[i].base_enable) {
                    record.values[i * 3 + 1] = spdata.datas[1].mainData;
                    record.values[i * 3 + 2] = (float) collect_instances[i].base_data;
                } else {
                    record.values[i * 3 + 1] = Float.NaN;
                    record.values[i * 3 + 2] = Float.NaN;
                }
            }
            WQAPlatform.GetInstance().GetDBHelperFactory().GetDataRecorder().SaveData(record);
            return true;
        } catch (Exception ex) {
            LogCenter.Instance().PrintLog(Level.SEVERE, ex);
        }
        return false;
    }
//18:05

    private boolean wait(int timeout) {
        Date now = new Date();
        if (now.getTime() - last_time.getTime() > timeout) {
            last_time.setTime(last_time.getTime() + timeout);
            run_time = run_time - timeout;
            if (run_time < 0) {
                run_time = 0;
                start_collect = false;
            }
            return true;
        }
        return false;
    }

    private void MainAction() {
        try {
            ((ShareIO) device.GetDevInfo().io).Lock();
            //连接状态下，获取数据
            if (GetState() == ControlState.CONNECT) {
                if (this.start_collect) {
                    if (wait(collectlag)) {
                        if (!CollectData()) {
                            ChangeState(ControlState.DISCONNECT);
                        }
                        if (this.impl != null) {
                            impl.Call(start_collect);
                        }
                    }

                } else {
                    KeepAlive();
                }
            } else if (GetState() == ControlState.DISCONNECT) {
                KeepAlive();
            }
        } catch (Exception ex) {
            ChangeState(ControlState.DISCONNECT);
            LogCenter.Instance().PrintLog(Level.SEVERE, ex);
        } finally {
            ((ShareIO) device.GetDevInfo().io).UnLock();
        }
    }

    private class Process implements Runnable {

        boolean is_start = true;

        @Override
        public void run() {
            while (is_start) {
                state_lock.lock();
                try {
                    MainAction();
                } finally {
                    state_lock.unlock();
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(run_lag);
                } catch (InterruptedException ex) {
                    Logger.getLogger(DevMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    // </editor-fold>    

    // <editor-fold defaultstate="collapsed" desc="输入">
    private Process run_process = null;

    public void Start() {
        state_lock.lock();
        try {
            if (this.run_process == null) {
                device.InitDevice();
                run_process = new Process();
                this.ChangeState(ControlState.CONNECT);
                WQAPlatform.GetInstance().GetThreadPool().submit(run_process);
            }
        } catch (Exception ex) {
            LogCenter.Instance().PrintLog(Level.SEVERE, ex);
        } finally {
            state_lock.unlock();
        }
    }

    //停止控制
    public void End() {
        state_lock.lock();
        try {
            if (this.run_process != null) {
                this.ChangeState(ControlState.DISCONNECT);
                run_process.is_start = false;
                run_process = null;
            }
        } finally {
            state_lock.unlock();
        }
    }
    // </editor-fold>    

    // <editor-fold defaultstate="collapsed" desc="模块"> 
    private DevMonitor[] collect_instances;

    private void initDevMonitor() {
        collect_instances = new DevMonitor[this.device.GetNmArray().length];
        for (int i = 0; i < collect_instances.length; i++) {
            collect_instances[i] = new DevMonitor(this, i);
            collect_instances[i].currentState = DevMonitor.State.WORKING;
        }
    }

    public DevMonitor[] GetCollector() {
        return this.collect_instances;
    }

    public DevConfigTable GetConfigTable() {
        return new DevConfigTable(this.device, this);
    }
    // </editor-fold>   

}
