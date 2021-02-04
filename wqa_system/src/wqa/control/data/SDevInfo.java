/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.control.data;

import wqa.bill.io.ShareIO;



/**
 *
 * @author chejf
 */
public class SDevInfo {

    public ShareIO io;
    public DevID dev_id;

    public boolean EqualsTo(SDevInfo other) {
        return this.io == other.io
                && this.dev_id.EqualsTo(other.dev_id);
    }
}
