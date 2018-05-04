/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.ui.movies.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.combobox.TmmCheckComboBox;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * this class is used for a media source movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieMediaSourceFilter extends AbstractMovieUIFilter {
  private TmmCheckComboBox<MediaSource> checkComboBox;

  public MovieMediaSourceFilter() {
    super();
    buildAndInstallMediaSources();
    MediaSource.addListener(evt -> SwingUtilities.invokeLater(this::buildAndInstallMediaSources));
  }

  @Override
  public String getId() {
    return "movieMediaSource";
  }

  @Override
  public String getFilterValueAsString() {
    List<String> values = new ArrayList<>();
    for (MediaSource mediaSource : checkComboBox.getSelectedItems()) {
      values.add(mediaSource.name());
    }
    try {
      return objectMapper.writeValueAsString(values);
    }
    catch (Exception e) {
      return null;
    }
  }

  @Override
  public void setFilterValue(Object value) {
    List<MediaSource> selectedItems = new ArrayList<>();

    try {
      List<String> values = objectMapper.readValue((String) value, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

      for (String source : values) {
        MediaSource mediaSource = MediaSource.getMediaSource(source);
        selectedItems.add(mediaSource);
      }

    }
    catch (Exception ignored) {
    }

    checkComboBox.setSelectedItems(selectedItems);
  }

  @Override
  public boolean accept(Movie movie) {
    List<MediaSource> selectedItems = checkComboBox.getSelectedItems();
    return selectedItems.contains(movie.getMediaSource());
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    checkComboBox = new TmmCheckComboBox<>();
    return checkComboBox;
  }

  private void buildAndInstallMediaSources() {
    // remove the listener to not firing unnecessary events
    checkComboBox.removeActionListener(actionListener);

    List<MediaSource> selectedItems = checkComboBox.getSelectedItems();

    checkComboBox.setItems(Arrays.asList(MediaSource.values()));

    if (!selectedItems.isEmpty()) {
      checkComboBox.setSelectedItems(selectedItems);
    }

    // re-add the itemlistener
    checkComboBox.addActionListener(actionListener);
  }
}
