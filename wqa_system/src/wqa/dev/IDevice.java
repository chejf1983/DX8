/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.dev;

import java.util.ArrayList;
import wqa.control.data.SConfigItem;
import wqa.control.data.SDevInfo;

/**
 *
 * @author chejf
 */
public interface IDevice {

    public void StartTest() throws Exception;

    public int[] CollectData() throws Exception;

    public SDevInfo GetDevInfo();

    public void InitDevice() throws Exception;

    public int ReTestType();
    
    public ArrayList<SConfigItem> GetInfoList();
    
    public void SetInfoList(ArrayList<SConfigItem> list) throws Exception ;

    int[] GetNmArray();
}
