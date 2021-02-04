/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.control.common;

import java.util.HashMap;

/**
 *
 * @author chejf
 */
public class CDevDataTable {

    private static CDevDataTable instance;

    public static CDevDataTable GetInstance() {
        if (instance == null) {
            instance = new CDevDataTable();
        }
        return instance;
    }

    private CDevDataTable() {
        devmap.put(0x08000000, 8);
        devmap.put(0x08000001, 4);
    }
    public HashMap<Integer, Integer> devmap = new HashMap();

}
