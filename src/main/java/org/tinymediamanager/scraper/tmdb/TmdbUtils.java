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
package org.tinymediamanager.scraper.tmdb;

import org.tinymediamanager.scraper.entities.MediaType;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.FindResults;
import com.uwetrottmann.tmdb2.enumerations.ExternalSource;

/**
 * the class TmdbUtils is used to share some common utils across the TMDB scraper package
 * 
 * @author Manuel Laggner
 * @since 4.0
 */
class TmdbUtils {
  private TmdbUtils() {
    // private constructor for utility classes
  }

  /**
   * get the TMDB id via the IMDB id
   * 
   * @param api
   *          the TMDB API
   * @param type
   *          the {@link MediaType}, because TMDB shares the same ids between movies and TV shows :(
   * @param imdbId
   *          the IMDB id
   * @return the TMDB id or 0 if nothing has been found
   * @throws Exception
   *           any Exception thrown
   */
  static int getTmdbIdFromImdbId(Tmdb api, MediaType type, String imdbId) throws Exception {
    FindResults findResults = api.findService().find(imdbId, ExternalSource.IMDB_ID, null).execute().body();
    // movie
    if (findResults != null && findResults.movie_results != null && !findResults.movie_results.isEmpty()
        && (type == MediaType.MOVIE || type == MediaType.MOVIE_SET)) {
      return findResults.movie_results.get(0).id;
    }

    // tv show
    if (findResults != null && findResults.tv_results != null && !findResults.tv_results.isEmpty()
        && (type == MediaType.TV_SHOW || type == MediaType.TV_EPISODE)) {
      return findResults.tv_results.get(0).id;
    }

    return 0;
  }

  /**
   * get the TMDB if via the TVDB id
   *
   * @param api
   *          the TMDB API
   * @param tvdbId
   *          the TVDB id
   * @return the TMDB id or 0 if nothing has been found
   * @throws Exception
   *           any Exception thrown
   */
  static int getTmdbIdFromTvdbId(Tmdb api, int tvdbId) throws Exception {
    FindResults findResults = api.findService().find(tvdbId, ExternalSource.TVDB_ID, null).execute().body();
    if (findResults != null && findResults.tv_results != null && !findResults.tv_results.isEmpty()) {
      return findResults.tv_results.get(0).id;
    }

    return 0;
  }
}
