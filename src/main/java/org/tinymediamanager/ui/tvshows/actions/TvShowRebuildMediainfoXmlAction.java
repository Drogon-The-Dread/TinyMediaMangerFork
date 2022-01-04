/*
 * Copyright 2012 - 2022 Manuel Laggner
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

package org.tinymediamanager.ui.tvshows.actions;

import static org.tinymediamanager.ui.TmmFontHelper.L1;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.TmmProperties;
import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.core.tvshow.tasks.TvShowReloadMediaInformationTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * the class {@link TvShowRebuildMediainfoXmlAction} is used to delete mediainfo.xml for selected TV shows/episodes and rebuild it afterwards
 *
 * @author Manuel Laggner
 */
public class TvShowRebuildMediainfoXmlAction extends TmmAction {

  private static final long           serialVersionUID = -2029243504238273761L;


  public TvShowRebuildMediainfoXmlAction() {
    putValue(NAME, TmmResourceBundle.getString("tvshow.rebuildmediainfoxml"));
    putValue(SHORT_DESCRIPTION, TmmResourceBundle.getString("tvshow.rebuildmediainfoxml"));
    putValue(SMALL_ICON, IconManager.REFRESH);
    putValue(LARGE_ICON_KEY, IconManager.REFRESH);
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<Object> selectedObjects = TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects();
    List<TvShow> selectedTvShows = new ArrayList<>();
    Set<TvShowEpisode> selectedEpisodes = new HashSet<>();

    for (Object obj : selectedObjects) {
      if (obj instanceof TvShow) {
        TvShow tvShow = (TvShow) obj;
        selectedTvShows.add(tvShow);
        selectedEpisodes.addAll(tvShow.getEpisodes());
      }

      if (obj instanceof TvShowSeason) {
        TvShowSeason season = (TvShowSeason) obj;
        selectedEpisodes.addAll(season.getEpisodes());
      }

      if (obj instanceof TvShowEpisode) {
        TvShowEpisode tvShowEpisode = (TvShowEpisode) obj;
        selectedEpisodes.add(tvShowEpisode);
      }
    }

    if (selectedTvShows.isEmpty() && selectedEpisodes.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getInstance(), TmmResourceBundle.getString("tmm.nothingselected"));
      return;
    }

    // display warning and ask the user again
    if (!TmmProperties.getInstance().getPropertyAsBoolean("tvshow.hidedeletemediainfoxmlhint")) {
      JCheckBox checkBox = new JCheckBox(TmmResourceBundle.getString("tmm.donotshowagain"));
      TmmFontHelper.changeFont(checkBox, L1);
      checkBox.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

      Object[] options = { TmmResourceBundle.getString("Button.yes"), TmmResourceBundle.getString("Button.no") };
      Object[] params = { TmmResourceBundle.getString("tvshow.deletemediainfoxml.desc"), checkBox };
      int answer = JOptionPane.showOptionDialog(MainWindow.getInstance(), params, TmmResourceBundle.getString("tvshow.deletemediainfoxml"),
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);

      // the user don't want to show this dialog again
      if (checkBox.isSelected()) {
        TmmProperties.getInstance().putProperty("tvshow.hidedeletemediainfoxmlhint", String.valueOf(checkBox.isSelected()));
      }

      if (answer != JOptionPane.YES_OPTION) {
        return;
      }
    }

    for (TvShow tvShow : selectedTvShows) {
      tvShow.getMediaFiles(MediaFileType.MEDIAINFO).forEach(mediaFile -> {
        Utils.deleteFileSafely(mediaFile.getFileAsPath());
        tvShow.removeFromMediaFiles(mediaFile);
      });
    }
    for (TvShowEpisode episode : selectedEpisodes) {
      episode.getMediaFiles(MediaFileType.MEDIAINFO).forEach(mediaFile -> {
        Utils.deleteFileSafely(mediaFile.getFileAsPath());
        episode.removeFromMediaFiles(mediaFile);
      });
    }

    // get data of all files within all selected TV shows/episodes
    TmmThreadPool task = new TvShowReloadMediaInformationTask(selectedTvShows, new ArrayList<>(selectedEpisodes));
    TmmTaskManager.getInstance().addMainTask(task);
  }
}
