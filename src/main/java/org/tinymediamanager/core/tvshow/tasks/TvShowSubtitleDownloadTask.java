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
package org.tinymediamanager.core.tvshow.tasks;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tasks.DownloadTask;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

/**
 * This class handles the download and additional unpacking of a subtitle
 * 
 * @author Manuel Laggner
 */
public class TvShowSubtitleDownloadTask extends DownloadTask {

  private final TvShowEpisode episode;

  public TvShowSubtitleDownloadTask(String url, Path toFile, TvShowEpisode episode2) {
    super(url, toFile);
    this.episode = episode2;
  }

  @Override
  protected void doInBackground() {
    // let the DownloadTask handle the whole download
    super.doInBackground();

    MediaFile mf = new MediaFile(file);

    if (mf.getType() != MediaFileType.SUBTITLE) {
      String basename = FilenameUtils.getBaseName(file.getFileName().toString());

      // try to decompress
      try {
        byte[] buffer = new byte[1024];

        ZipInputStream is = new ZipInputStream(new FileInputStream(file.toFile()));

        // get the zipped file list entry
        ZipEntry ze = is.getNextEntry();

        while (ze != null) {
          String zipEntryFilename = ze.getName();
          String extension = FilenameUtils.getExtension(zipEntryFilename).toLowerCase(Locale.ROOT);

          // check is that is a valid file type
          if (!Globals.settings.getSubtitleFileType().contains("." + extension)) {
            ze = is.getNextEntry();
            continue;
          }

          Path destination = file.getParent().resolve(basename + "." + extension);
          FileOutputStream os = new FileOutputStream(destination.toFile());

          int len;
          while ((len = is.read(buffer)) > 0) {
            os.write(buffer, 0, len);
          }

          os.close();
          mf = new MediaFile(destination);

          // only take the first subtitle
          break;
        }
        is.closeEntry();
        is.close();

        Utils.deleteFileSafely(file);
      }
      catch (Exception ignored) {
      }
    }

    mf.gatherMediaInformation();
    episode.removeFromMediaFiles(mf); // remove old (possibly same) file
    episode.addToMediaFiles(mf); // add file, but maybe with other MI values
    episode.saveToDb();
  }
}
