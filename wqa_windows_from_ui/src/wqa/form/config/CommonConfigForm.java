/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wqa.form.config;

import java.awt.Frame;
import java.util.ArrayList;
import javax.swing.JTabbedPane;
import wqa.control.common.DevConfigTable;

/**
 *
 * @author chejf
 */
public class CommonConfigForm extends ConfigForm {

    public CommonConfigForm(Frame parent, boolean modal, String name) {
        super(parent, modal, name);
    }

    private ArrayList<ConfigTablePane> pane = new ArrayList();
    private JTabbedPane TabbedPane = new JTabbedPane();

    public boolean InitModel(DevConfigTable[] configs) throws Exception {
        pane.clear();
        for (DevConfigTable config : configs) {
            ConfigTablePane configTablePane = new ConfigTablePane(config);
            this.pane.add(configTablePane);
            TabbedPane.add(config.GetListName(), configTablePane);
        }

        this.AddPane(this.TabbedPane);
        return true;
    }

    @Override
    public void Close() {
        super.Close(); //To change body of generated methods, choose Tools | Templates.
    }

}
