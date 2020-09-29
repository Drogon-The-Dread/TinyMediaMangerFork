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
package org.tinymediamanager.ui.tvshows.actions;

import static org.tinymediamanager.core.Constants.IMDB;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.util.RatingUtil;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.actions.TmmAction;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

public class TvShowFetchImdbRating extends TmmAction {
  private static final Logger         LOGGER = LoggerFactory.getLogger(TvShowFetchImdbRating.class);
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages");

  public TvShowFetchImdbRating() {
    putValue(LARGE_ICON_KEY, IconManager.RATING_BLUE);
    putValue(SMALL_ICON, IconManager.RATING_BLUE);
    putValue(NAME, BUNDLE.getString("tvshow.refetchimdbrating"));
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
  }

  @Override
  protected void processAction(ActionEvent e) {
    List<TvShow> selectedTvShows = TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows();
    List<TvShowEpisode> selectedEpisodes = new ArrayList<>();

    // add all episodes which are not part of a selected tv show
    for (Object obj : TvShowUIModule.getInstance().getSelectionModel().getSelectedObjects()) {
      if (obj instanceof TvShowEpisode) {
        TvShowEpisode episode = (TvShowEpisode) obj;
        if (!selectedTvShows.contains(episode.getTvShow())) {
          selectedEpisodes.add(episode);
        }
      }
    }

    if (selectedEpisodes.isEmpty() && selectedTvShows.isEmpty()) {
      JOptionPane.showMessageDialog(MainWindow.getInstance(), BUNDLE.getString("tmm.nothingselected"));
      return;
    }

    for (TvShow tvShow : selectedTvShows) {
      try {
        MediaRating rating = RatingUtil.getImdbRating(tvShow.getImdbId());
        if (rating != null) {
          tvShow.setRating(rating);
        }
      }
      catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      catch (Exception ex) {
        LOGGER.error("could not fetch imdb rating - {}", ex.getMessage());
      }
    }

    for (TvShowEpisode episode : selectedEpisodes) {
      try {
        MediaRating rating = RatingUtil.getImdbRating(episode.getIdAsString(IMDB));
        if (rating != null) {
          episode.setRating(rating);
        }
      }
      catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      catch (Exception ex) {
        LOGGER.error("could not fetch imdb rating - {}", ex.getMessage());
      }
    }
  }
}
