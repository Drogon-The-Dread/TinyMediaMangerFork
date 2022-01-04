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

package org.tinymediamanager.ui.tvshows.filters;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * the class {@link TvShowHDRFormatFilter} provides a filter for the HDR format of TV shows
 *
 * @author Wolfgang Janes
 */
public class TvShowHDRFormatFilter extends AbstractCheckComboBoxTvShowUIFilter<String> {
  private final TvShowList tvShowList = TvShowModuleManager.getInstance().getTvShowList();

  public TvShowHDRFormatFilter() {
    super();
    checkComboBox.enableFilter((s, s2) -> s.toLowerCase(Locale.ROOT).startsWith(s2.toLowerCase(Locale.ROOT)));
    buildHdrArray();
    PropertyChangeListener propertyChangeListener = evt -> buildHdrArray();
    tvShowList.addPropertyChangeListener(Constants.HDR_FORMAT, propertyChangeListener);
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.hdrformat"));
  }

  @Override
  public String getId() {
    return "hdrFormatFilter";
  }

  @Override
  protected String parseTypeToString(String type) throws Exception {
    return type;
  }

  @Override
  protected String parseStringToType(String string) throws Exception {
    return string;
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {

    List<String> hdrformat = checkComboBox.getSelectedItems();

    for (TvShowEpisode episode : episodes) {
      List<MediaFile> mfs = episode.getMediaFiles(MediaFileType.VIDEO);
      for (MediaFile mf : mfs) {
        if (invert ^ hdrformat.contains(mf.getHdrFormat())) {
          return true;
        }
      }
    }

    return false;
  }

  public void buildHdrArray() {
    List<String> hdrformats = new ArrayList<>(tvShowList.getHdrFormatInEpisodes());
    Collections.sort(hdrformats);
    setValues(hdrformats);
  }
}
