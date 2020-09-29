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
package org.tinymediamanager.ui.panels;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.TmmDateFormat;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.TmmUILayoutStore;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.components.table.TmmTableFormat;
import org.tinymediamanager.ui.components.table.TmmTableModel;
import org.tinymediamanager.ui.renderer.RightAlignTableCellRenderer;

import ca.odell.glazedlists.EventList;
import net.miginfocom.swing.MigLayout;

/**
 * The Class MediaFilesPanel.
 * 
 * @author Manuel Laggner
 */
public abstract class MediaFilesPanel extends JPanel {
  private static final long           serialVersionUID = -4929581173434859034L;
  private static final Logger         LOGGER           = LoggerFactory.getLogger(MediaFilesPanel.class);
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages");

  private TmmTable                    tableFiles;

  private final EventList<MediaFile>  mediaFileEventList;

  public MediaFilesPanel(EventList<MediaFile> mediaFiles) {
    this.mediaFileEventList = mediaFiles;

    initComponents();
  }

  private void initComponents() {
    setLayout(new MigLayout("insets 0", "[450lp,grow]", "[300lp,grow]"));
    {
      TmmTableModel<MediaFile> tableModel = new TmmTableModel<>(mediaFileEventList, new MediaTableFormat());
      tableFiles = new TmmTable(tableModel);
      tableFiles.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      LinkListener linkListener = new LinkListener();
      tableFiles.addMouseListener(linkListener);
      tableFiles.addMouseMotionListener(linkListener);

      JScrollPane scrollPaneFiles = new JScrollPane();
      tableFiles.configureScrollPane(scrollPaneFiles);
      add(scrollPaneFiles, "cell 0 0,grow");

      scrollPaneFiles.setViewportView(tableFiles);
    }
  }

  public void installTmmUILayoutStore(String parentId) {
    if (StringUtils.isNotBlank(parentId)) {
      tableFiles.setName(parentId + ".mediaFilesTable");
      TmmUILayoutStore.getInstance().install(tableFiles);
    }
  }

  public void adjustColumns() {
    tableFiles.adjustColumnPreferredWidths(6);
  }

  /**
   * get the actual media entity holding this list of media files
   *
   * @return the media entity
   */
  public abstract MediaEntity getMediaEntity();

  private static class MediaTableFormat extends TmmTableFormat<MediaFile> {
    MediaTableFormat() {
      /*
       * play/view
       */
      Column col = new Column("", "open", mediaFile -> {
        if (mediaFile.isVideo()) {
          return IconManager.PLAY;
        }
        if (mediaFile.isGraphic()) {
          return IconManager.SEARCH;
        }
        return null;
      }, ImageIcon.class);
      col.setColumnResizeable(false);
      addColumn(col);

      /*
       * filename
       */
      col = new Column(BUNDLE.getString("metatag.filename"), "filename", MediaFile::getFilename, String.class);
      addColumn(col);

      /*
       * filetype
       */
      col = new Column(BUNDLE.getString("metatag.mediafiletype"), "filetype", mediaFile -> getMediaFileTypeLocalized(mediaFile.getType()),
          String.class);
      addColumn(col);

      /*
       * file size
       */
      col = new Column(BUNDLE.getString("metatag.size"), "filesize", MediaFile::getFilesizeInMegabytes, String.class);
      col.setCellRenderer(new RightAlignTableCellRenderer());
      addColumn(col);

      /*
       * codec
       */
      col = new Column(BUNDLE.getString("metatag.codec"), "codec", MediaFile::getCombinedCodecs, String.class);
      col.setColumnResizeable(false);
      addColumn(col);

      /*
       * resolution
       */
      col = new Column(BUNDLE.getString("metatag.resolution"), "resolution", MediaFile::getVideoResolution, String.class);
      col.setColumnResizeable(false);
      addColumn(col);

      /*
       * runtime
       */
      col = new Column(BUNDLE.getString("metatag.runtime"), "runtime", MediaFile::getDurationHMS, String.class);
      col.setColumnResizeable(false);
      addColumn(col);

      /*
       * subtitle
       */
      col = new Column(BUNDLE.getString("metatag.subtitle"), "subtitle", MediaFile::getSubtitlesAsString, String.class);
      col.setColumnResizeable(false);
      addColumn(col);

      /*
       * creation date
       */
      col = new Column(BUNDLE.getString("metatag.filecreationdate"), "filecreationdate", mediaFile -> formatDate(mediaFile.getDateCreated()),
          String.class);
      col.setColumnResizeable(false);
      addColumn(col);

      /*
       * last modified date
       */
      col = new Column(BUNDLE.getString("metatag.filelastmodifieddate"), "filelastmodifieddate",
          mediaFile -> formatDate(mediaFile.getDateLastModified()), String.class);
      col.setColumnResizeable(false);
      addColumn(col);
    }

    private String getMediaFileTypeLocalized(MediaFileType type) {
      String prop = "mediafiletype." + type.name().toLowerCase(Locale.ROOT);
      return BUNDLE.getString(prop);
    }

    private String formatDate(Date date) {
      if (date == null) {
        return "";
      }

      return TmmDateFormat.MEDIUM_DATE_SHORT_TIME_FORMAT.format(date);
    }
  }

  private class LinkListener implements MouseListener, MouseMotionListener {
    @Override
    public void mouseClicked(MouseEvent arg0) {
      int col = tableFiles.columnAtPoint(arg0.getPoint());
      if (col == 0) {
        int row = tableFiles.rowAtPoint(arg0.getPoint());
        row = tableFiles.convertRowIndexToModel(row);
        MediaFile mf = mediaFileEventList.get(row);
        // open the video file in the desired player
        if (mf.isVideo()) {
          try {
            TmmUIHelper.openFile(mf.getFileAsPath());
          }
          catch (Exception e) {
            LOGGER.error("open file", e);
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, mf, "message.erroropenfile", new String[] { ":", e.getLocalizedMessage() }));
          }
        }
        // open the graphic in the lightbox
        if (mf.isGraphic()) {
          MainWindow.getInstance().createLightbox(mf.getFileAsPath().toString(), "");
        }
      }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col == 0) {
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
    }

    @Override
    public void mouseExited(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col != 0) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col != 0 && table.getCursor().getType() == Cursor.HAND_CURSOR) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      if (col == 0 && table.getCursor().getType() == Cursor.DEFAULT_CURSOR) {
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent arg0) {
    }
  }
}
