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

package org.tinymediamanager.core.movie.filenaming;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.IFileNaming;

/**
 * The Enum MovieClearartNaming.
 * 
 * @author Manuel Laggner
 */
public enum MovieClearartNaming implements IFileNaming {

  /** [filename]-clearart.* */
  FILENAME_CLEARART {
    @Override
    public String getFilename(String basename, String extension) {
      return StringUtils.isNotBlank(basename) ? basename + "-clearart." + extension : "";
    }
  },

  /** clearart.* */
  CLEARART {
    @Override
    public String getFilename(String basename, String extension) {
      return "clearart." + extension;
    }
  }
}
