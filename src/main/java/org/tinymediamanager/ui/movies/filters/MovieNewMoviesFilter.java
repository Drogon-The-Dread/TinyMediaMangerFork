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

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.movies.AbstractMovieUIFilter;

/**
 * this class is used for a new movies filter
 * 
 * @author Manuel Laggner
 */
public class MovieNewMoviesFilter extends AbstractMovieUIFilter {

  @Override
  public String getId() {
    return "movieNewMovies";
  }

  @Override
  public String getFilterValueAsString() {
    return null;
  }

  @Override
  public void setFilterValue(Object value) {
  }

  @Override
  public boolean accept(Movie movie) {
    return movie.isNewlyAdded();
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("movieextendedsearch.newmovies")); //$NON-NLS-1$
  }

  @Override
  protected JComponent createFilterComponent() {
    return null;
  }
}
