package org.tinymediamanager.core.movie;

import java.util.ArrayList;

import org.junit.Test;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.movie.entities.Movie;

public class MovieRenamerTest extends BasicMovieTest {

  @Test
  public void special() {
    assertEqual("jb - the bla", MovieRenamer.replaceInvalidCharacters("jb: the bla"));
    assertEqual("jb  - the bla", MovieRenamer.replaceInvalidCharacters("jb : the bla"));
    assertEqual("2-22", MovieRenamer.replaceInvalidCharacters("2:22"));
    assertEqual("2 -22", MovieRenamer.replaceInvalidCharacters("2 :22"));

    // we do not strip path separators here
    assertEqual("weird \\\\-/ movie", MovieRenamer.replaceInvalidCharacters("weird \"\\\\:<>|/?* movie"));
  }

  @Test
  public void testRename() throws Exception {
    // MediaInfoUtils.loadMediaInfo(); // no MI on buildserver
    copyResourceFolderToWorkFolder("samples");

    Movie m = new Movie();
    m.setTitle("The Dish");
    m.setYear(2000);
    MediaFile mf = new MediaFile(getWorkFolder().resolve("samples").resolve("thx_scarface-DWEU.vob"));

    // mf.gatherMediaInformation();
    mf.setVideoCodec("MPEG");
    mf.setVideoHeight(480);
    mf.setVideoWidth(720);
    ArrayList<MediaFileAudioStream> audl = new ArrayList<>();
    MediaFileAudioStream aud = new MediaFileAudioStream();
    aud.setAudioChannels(6);
    aud.setCodec("AC3");
    aud.setLanguage("en");
    audl.add(aud);
    mf.setAudioStreams(audl);

    aud = new MediaFileAudioStream();
    aud.setAudioChannels(2);
    aud.setCodec("MP3");
    aud.setLanguage("de");
    audl.add(aud);
    mf.setAudioStreams(audl);

    m.addToMediaFiles(mf);

    assertEqual("The Dish (2000) MPEG-480p AC3-6ch",
        MovieRenamer.createDestinationForFilename("${title} (${year}) ${videoCodec}-${videoFormat} ${audioCodec}-${audioChannels}", m));
    assertEqual("The Dish (2000)", MovieRenamer.createDestinationForFoldername("${title} (${year})", m));
    assertEqual("_The Dish (2000)", MovieRenamer.createDestinationForFoldername("${_,title,} (${year})", m));
    assertEqual("The Dish (2000)", MovieRenamer.createDestinationForFoldername(".${title} (${year})", m));
    assertEqual("The Dish (2000)", MovieRenamer.createDestinationForFoldername("-${title} (${year})-", m));
    assertEqual("2000-2009", MovieRenamer.createDestinationForFoldername("${decadeLong}", m));
    assertEqual("2000s", MovieRenamer.createDestinationForFoldername("${decadeShort}", m));
  }
}
