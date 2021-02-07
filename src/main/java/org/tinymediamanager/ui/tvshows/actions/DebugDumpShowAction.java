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
package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.tinymediamanager.core.TmmResourceBundle;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

public class DebugDumpShowAction extends TmmAction {
  private static final long           serialVersionUID = -8473181347332963044L;
  

  public DebugDumpShowAction() {
    putValue(NAME, TmmResourceBundle.getString("debug.entity.dump"));
    putValue(SHORT_DESCRIPTION, TmmResourceBundle.getString("debug.entity.dump.desc"));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<Object> selectedObjects = TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects();

    if (selectedObjects.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getInstance(), TmmResourceBundle.getString("tmm.nothingselected"));
      return;
    }

    HashSet<TvShow> shows = new HashSet<>(); // no dupes
    for (Object obj : selectedObjects) {
      if (obj instanceof TvShow) {
        TvShow tvShow = (TvShow) obj;
        shows.add(tvShow);
      }
      else if (obj instanceof TvShowSeason) {
        TvShowSeason season = (TvShowSeason) obj;
        shows.add(season.getTvShow());
      }
      else if (obj instanceof TvShowEpisode) {
        TvShowEpisode tvShowEpisode = (TvShowEpisode) obj;
        shows.add(tvShowEpisode.getTvShow());
      }
    }

    for (TvShow tvShow : shows) {
      TvShowModuleManager.getInstance().dump(tvShow);
    }
  }
}
