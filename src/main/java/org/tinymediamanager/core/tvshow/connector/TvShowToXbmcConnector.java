/*
 * Copyright 2012 - 2019 Manuel Laggner
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

package org.tinymediamanager.core.tvshow.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.tvshow.entities.TvShow;

/**
 * the class TvShowToXbmcConnector is used to write a legacy XBMC compatible NFO file
 *
 * @author Manuel Laggner
 */
public class TvShowToXbmcConnector extends TvShowGenericXmlConnector {
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowToXbmcConnector.class);

  public TvShowToXbmcConnector(TvShow tvShow) {
    super(tvShow);
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected void addOwnTags() {
  }
}
