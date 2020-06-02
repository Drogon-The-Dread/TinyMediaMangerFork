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

package org.tinymediamanager.ui.moviesets.settings;

import java.util.ResourceBundle;

import org.tinymediamanager.ui.settings.TmmSettingsNode;

/**
 * the class {@link MovieSetSettingsNode} provides all settings pages for movie sets
 * 
 * @author Manuel Laggner
 */
public class MovieSetSettingsNode extends TmmSettingsNode {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages");

  public MovieSetSettingsNode() {
    super(BUNDLE.getString("Settings.moviesets"), new MovieSetSettingsPanel());

    addChild(new TmmSettingsNode(BUNDLE.getString("Settings.images"), new MovieSetImageSettingsPanel()));
  }
}
