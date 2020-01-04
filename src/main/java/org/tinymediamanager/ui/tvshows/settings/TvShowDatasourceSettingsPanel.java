/*
 * Copyright 2012 - 2020 Manuel Laggner
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

import java.awt.Cursor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowSettings;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.CollapsiblePanel;
import org.tinymediamanager.ui.components.TmmLabel;

import net.miginfocom.swing.MigLayout;

/**
 * The class {@link TvShowDatasourceSettingsPanel} is used to display data sources related settings
 * 
 * @author Manuel Laggner
 */
class TvShowDatasourceSettingsPanel extends JPanel {
  private static final long           serialVersionUID = -675729644848101096L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private TvShowSettings              settings         = TvShowModuleManager.SETTINGS;
  private JCheckBox                   chckbxDvdOrder;
  private JTextField                  tfAddBadword;
  private JList<String>               listBadWords;
  private JList<String>               listDatasources;
  private JList<String>               listSkipFolder;
  private JButton                     btnAddDatasource;
  private JButton                     btnRemoveDatasource;
  private JButton                     btnAddSkipFolder;
  private JButton                     btnRemoveSkipFolder;
  private JButton                     btnRemoveBadWord;
  private JButton                     btnAddBadWord;

  TvShowDatasourceSettingsPanel() {
    // UI initializations
    initComponents();
    initDataBindings();

    // logic initializations
    btnAddDatasource.addActionListener(arg0 -> {
      String path = TmmProperties.getInstance().getProperty("tvshow.datasource.path"); //$NON-NLS-1$
      Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.tvshowdatasource.folderchooser"), path); //$NON-NLS-1$
      if (file != null && Files.isDirectory(file)) {
        settings.addTvShowDataSources(file.toAbsolutePath().toString());
        TmmProperties.getInstance().putProperty("tvshow.datasource.path", file.toAbsolutePath().toString());
      }
    });
    btnRemoveDatasource.addActionListener(arg0 -> {
      int row = listDatasources.getSelectedIndex();
      if (row != -1) { // nothing selected
        String path = settings.getTvShowDataSource().get(row);
        String[] choices = { BUNDLE.getString("Button.continue"), BUNDLE.getString("Button.abort") }; //$NON-NLS-1$
        int decision = JOptionPane.showOptionDialog(null, String.format(BUNDLE.getString("Settings.tvshowdatasource.remove.info"), path),
            BUNDLE.getString("Settings.datasource.remove"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices,
            BUNDLE.getString("Button.abort")); //$NON-NLS-1$
        if (decision == JOptionPane.YES_OPTION) {
          setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
          settings.removeTvShowDataSources(path);
          setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }
    });
    btnAddSkipFolder.addActionListener(e -> {
      String path = TmmProperties.getInstance().getProperty("tvshow.ignore.path"); //$NON-NLS-1$
      Path file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.ignore"), path); //$NON-NLS-1$
      if (file != null && Files.isDirectory(file)) {
        settings.addSkipFolder(file.toAbsolutePath().toString());
        TmmProperties.getInstance().putProperty("tvshow.ignore.path", file.toAbsolutePath().toString());
      }
    });
    btnRemoveSkipFolder.addActionListener(e -> {
      int row = listSkipFolder.getSelectedIndex();
      if (row != -1) { // nothing selected
        String ingore = settings.getSkipFolder().get(row);
        settings.removeSkipFolder(ingore);
      }
    });
    btnAddBadWord.addActionListener(e -> {
      if (StringUtils.isNotEmpty(tfAddBadword.getText())) {
        try {
          Pattern.compile(tfAddBadword.getText());
        }
        catch (PatternSyntaxException ex) {
          JOptionPane.showMessageDialog(null, BUNDLE.getString("message.regex.error")); //$NON-NLS-1$
          return;
        }
        TvShowModuleManager.SETTINGS.addBadWord(tfAddBadword.getText());
        tfAddBadword.setText("");
      }
    });
    btnRemoveBadWord.addActionListener(arg0 -> {
      int row = listBadWords.getSelectedIndex();
      if (row != -1) {
        String badWord = TvShowModuleManager.SETTINGS.getBadWord().get(row);
        TvShowModuleManager.SETTINGS.removeBadWord(badWord);
      }
    });
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[grow]", "[][15lp!][][15lp!][]"));
    {
      JPanel panelDatasources = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][400lp][][grow]", "[100lp,grow][][]"));

      JLabel lblDatasourcesT = new TmmLabel(BUNDLE.getString("Settings.source"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelDatasources, lblDatasourcesT, true);
      add(collapsiblePanel, "cell 0 0,growx, wmin 0");
      {
        JScrollPane scrollPaneDataSources = new JScrollPane();
        panelDatasources.add(scrollPaneDataSources, "cell 1 0,grow");

        listDatasources = new JList();
        listDatasources.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPaneDataSources.setViewportView(listDatasources);

        btnAddDatasource = new JButton(IconManager.ADD_INV);
        panelDatasources.add(btnAddDatasource, "flowy, cell 2 0, aligny top, growx");
        btnAddDatasource.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$

        btnRemoveDatasource = new JButton(IconManager.REMOVE_INV);
        panelDatasources.add(btnRemoveDatasource, "flowy, cell 2 0, aligny top, growx");
        btnRemoveDatasource.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$

        chckbxDvdOrder = new JCheckBox(BUNDLE.getString("Settings.dvdorder")); //$NON-NLS-1$
        panelDatasources.add(chckbxDvdOrder, "cell 1 2 2 1");
      }
    }
    {
      JPanel panelIgnore = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][400lp][][grow]", "[100lp,grow]"));

      JLabel lblIgnoreT = new TmmLabel(BUNDLE.getString("Settings.ignore"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelIgnore, lblIgnoreT, true);
      add(collapsiblePanel, "cell 0 2,growx,wmin 0");
      {
        JScrollPane scrollPaneIgnore = new JScrollPane();
        panelIgnore.add(scrollPaneIgnore, "cell 1 0,grow");

        listSkipFolder = new JList();
        scrollPaneIgnore.setViewportView(listSkipFolder);

        btnAddSkipFolder = new JButton(IconManager.ADD_INV);
        panelIgnore.add(btnAddSkipFolder, "flowy, cell 2 0, aligny top, growx");
        btnAddSkipFolder.setToolTipText(BUNDLE.getString("Settings.addignore")); //$NON-NLS-1$

        btnRemoveSkipFolder = new JButton(IconManager.REMOVE_INV);
        panelIgnore.add(btnRemoveSkipFolder, "flowy, cell 2 0, aligny top, growx");
        btnRemoveSkipFolder.setToolTipText(BUNDLE.getString("Settings.removeignore")); //$NON-NLS-1$
      }
    }
    {
      JPanel panelBadWords = new JPanel(new MigLayout("hidemode 1, insets 0", "[20lp!][300lp][][grow]", "[][100lp,grow][]"));

      JLabel lblBadWordsT = new TmmLabel(BUNDLE.getString("Settings.movie.badwords"), H3); //$NON-NLS-1$
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel(panelBadWords, lblBadWordsT, true);
      add(collapsiblePanel, "cell 0 4,growx,wmin 0");
      {
        JLabel lblBadWordsDesc = new JLabel(BUNDLE.getString("Settings.movie.badwords.hint"));
        panelBadWords.add(lblBadWordsDesc, "cell 1 0 3 1");

        JScrollPane scrollPaneBadWords = new JScrollPane();
        panelBadWords.add(scrollPaneBadWords, "cell 1 1,grow");

        listBadWords = new JList();
        scrollPaneBadWords.setViewportView(listBadWords);

        btnRemoveBadWord = new JButton(IconManager.REMOVE_INV);
        panelBadWords.add(btnRemoveBadWord, "cell 2 1,aligny bottom");
        btnRemoveBadWord.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$

        tfAddBadword = new JTextField();
        panelBadWords.add(tfAddBadword, "cell 1 2,growx");

        btnAddBadWord = new JButton(IconManager.ADD_INV);
        panelBadWords.add(btnAddBadWord, "cell 2 2, growx");
        btnAddBadWord.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
      }
    }
  }

  protected void initDataBindings() {
    BeanProperty<TvShowSettings, Boolean> settingsBeanProperty_1 = BeanProperty.create("dvdOrder");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<TvShowSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxDvdOrder, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<TvShowSettings, List<String>> settingsBeanProperty_2 = BeanProperty.create("tvShowDataSource");
    JListBinding<String, TvShowSettings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, listDatasources);
    jListBinding.bind();
    //
    BeanProperty<TvShowSettings, List<String>> settingsBeanProperty_3 = BeanProperty.create("skipFolder");
    JListBinding<String, TvShowSettings, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, listSkipFolder);
    jListBinding_1.bind();
    //
    BeanProperty<TvShowSettings, List<String>> settingsBeanProperty_4 = BeanProperty.create("badWord");
    JListBinding<String, TvShowSettings, JList> jListBinding_2 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, listBadWords);
    jListBinding_2.bind();
  }
}
