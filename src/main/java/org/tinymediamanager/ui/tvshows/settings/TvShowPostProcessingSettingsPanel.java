/*
 * Copyright 2012 - 2021 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.ui.tvshows.settings;

import static org.tinymediamanager.ui.TmmFontHelper.H3;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.PostProcess;
import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.DocsButton;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.dialogs.PostProcessDialog;
import org.tinymediamanager.ui.tvshows.dialogs.TvShowPostProcessDialog;

import net.miginfocom.swing.MigLayout;

/**
 * the class {@link TvShowPostProcessingSettingsPanel} holds the settings for post process action for movies
 *
 * @author Wolfgang Janes
 */
public class TvShowPostProcessingSettingsPanel extends JPanel {

  private final TvShowSettings settings = TvShowModuleManager.getInstance().getSettings();

  private JTable               tablePostProcesses;
  private JButton              btnRemoveProcess;
  private JButton              btnAddProcess;
  private JButton              btnEditProcess;

  TvShowPostProcessingSettingsPanel() {

    initComponents();
    initDataBindings();

    // button listeners
    btnAddProcess.addActionListener(e -> {

      PostProcessDialog dialog = new TvShowPostProcessDialog();
      dialog.pack();
      dialog.setLocationRelativeTo(MainWindow.getInstance());
      dialog.setVisible(true);
    });

    btnRemoveProcess.addActionListener(e -> {
      int row = tablePostProcesses.getSelectedRow();
      row = tablePostProcesses.convertRowIndexToModel(row);

      if (row != -1) {
        PostProcess process = settings.getPostProcessTvShow().get(row);
        settings.removePostProcessTvShow(process);
      }
    });

    btnEditProcess.addActionListener(e -> {
      int row = tablePostProcesses.getSelectedRow();
      row = tablePostProcesses.convertRowIndexToModel(row);

      if (row != -1) {
        PostProcess process = settings.getPostProcessTvShow().get(row);
        if (process != null) {
          PostProcessDialog dialog = new TvShowPostProcessDialog();
          dialog.setProcess(process);
          dialog.pack();
          dialog.setLocationRelativeTo(MainWindow.getInstance());
          dialog.setVisible(true);
        }
      }
    });

    // set Column Headers
    tablePostProcesses.getColumnModel().getColumn(0).setHeaderValue(TmmResourceBundle.getString("Settings.processname"));
    tablePostProcesses.getColumnModel().getColumn(1).setHeaderValue(TmmResourceBundle.getString("metatag.path"));
    tablePostProcesses.getColumnModel().getColumn(2).setHeaderValue(TmmResourceBundle.getString("Settings.commandname"));

  }

  private void initComponents() {
    setLayout(new MigLayout("", "[600lp,grow]", "[]"));
    {
      JPanel panelProcess = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][600lp][]", "[500lp]"));
      JLabel lblProcess = new TmmLabel(TmmResourceBundle.getString("Settings.postprocessing"), H3);
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelProcess, lblProcess, false);
      collapsiblePanel.addExtraTitleComponent(new DocsButton("/tvshows/settings#post-processing"));
      add(collapsiblePanel, "growx,wmin 0");

      {
        JScrollPane spProcesses = new JScrollPane();
        panelProcess.add(spProcesses, "cell 1 0,grow");
        tablePostProcesses = new TmmTable();
        spProcesses.setViewportView(tablePostProcesses);

        btnAddProcess = new JButton(TmmResourceBundle.getString("Button.add"));
        panelProcess.add(btnAddProcess, "flowy,cell 2 0,growx,aligny top");

        btnEditProcess = new JButton(TmmResourceBundle.getString("Button.edit"));
        panelProcess.add(btnEditProcess, "cell 2 0,growx");

        btnRemoveProcess = new JButton(TmmResourceBundle.getString("Button.remove"));
        panelProcess.add(btnRemoveProcess, "cell 2 0,growx");
      }
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSettings, List<PostProcess>> settingsBeanProperty = BeanProperty.create("postProcessTvShow");
    JTableBinding<PostProcess, TvShowSettings, JTable> jTableBinding = SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy.READ_WRITE,
        settings, settingsBeanProperty, tablePostProcesses);

    BeanProperty<PostProcess, String> wolBeanProperty_1 = BeanProperty.create("name");
    jTableBinding.addColumnBinding(wolBeanProperty_1);

    BeanProperty<PostProcess, String> wolBeanProperty_3 = BeanProperty.create("path");
    jTableBinding.addColumnBinding(wolBeanProperty_3);

    BeanProperty<PostProcess, String> wolBeanProperty_4 = BeanProperty.create("command");
    jTableBinding.addColumnBinding(wolBeanProperty_4);

    jTableBinding.setEditable(false);
    jTableBinding.bind();
  }

}
