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

package org.tinymediamanager.core.mediainfo;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.thirdparty.MediaInfo;
import org.tinymediamanager.thirdparty.MediaInfo.StreamKind;

/**
 * convenience class to ease the file<->mediainfo mapping
 * 
 * @author Myron Boyle
 *
 */
public class MediaInfoFile implements Comparable<MediaInfoFile> {

  private static final Logger                        LOGGER   = LoggerFactory.getLogger(MediaInfoFile.class);
  private Map<StreamKind, List<Map<String, String>>> snapshot;
  private int                                        duration = 0;
  private long                                       filesize = 0;
  private String                                     path     = "";
  private String                                     filename = "";

  public MediaInfoFile(Path file) {
    this.path = file.toAbsolutePath().getParent().toString();
    this.filename = file.getFileName().toString();
  }

  public MediaInfoFile(Path file, long filesize) {
    this(file);
    this.filesize = filesize;
  }

  public void setFilename(String filename) {
    this.filename = FilenameUtils.getName(filename);
  }

  public String getFilename() {
    return filename;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getFileExtension() {
    return FilenameUtils.getExtension(filename).toLowerCase(Locale.ROOT);
  }

  public void setFilesize(long filesize) {
    this.filesize = filesize;
  }

  public long getFilesize() {
    return filesize;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public int getDuration() {
    return duration;
  }

  public Map<StreamKind, List<Map<String, String>>> getSnapshot() {
    if (snapshot == null) {
      snapshot = Collections.emptyMap();
    }
    return snapshot;
  }

  public void setSnapshot(Map<StreamKind, List<Map<String, String>>> snapshot) {
    this.snapshot = snapshot;
    String dur = MediaFileHelper.getMediaInfo(snapshot, MediaInfo.StreamKind.General, 0, "Duration");
    if (!dur.isEmpty()) {
      try {
        this.duration = ((int) (Double.parseDouble(dur) / 1000));
      }
      catch (NumberFormatException e) {
        LOGGER.debug("Could not parse duration: {}", dur);
      }
    }

    if (this.filesize == 0) {
      String siz = MediaFileHelper.getMediaInfo(snapshot, MediaInfo.StreamKind.General, 0, "FileSize");
      if (!siz.isEmpty()) {
        try {
          this.filesize = Long.parseLong(siz);
        }
        catch (NumberFormatException e) {
          LOGGER.debug("Could not parse filezize: {}", siz);
        }
      }
    }
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   *
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    MediaInfoFile that = (MediaInfoFile) o;
    return Objects.equals(path, that.path) && Objects.equals(filename, that.filename);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, filename);
  }

  @Override
  public int compareTo(@NotNull MediaInfoFile o) {
    return getFilename().compareTo(o.getFilename());
  }
}
