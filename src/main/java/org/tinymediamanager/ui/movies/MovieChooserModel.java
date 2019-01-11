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
package org.tinymediamanager.ui.movies;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.movie.MovieHelpers;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieScraperMetadataConfig;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieTrailer;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaTrailer;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.exceptions.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.mediaprovider.IMovieArtworkProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieTrailerProvider;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class MovieChooserModel.
 * 
 * @author Manuel Laggner
 */
public class MovieChooserModel extends AbstractModelObject {
  private static final ResourceBundle   BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger           LOGGER           = LoggerFactory.getLogger(MovieChooserModel.class);
  public static final MovieChooserModel emptyResult      = new MovieChooserModel();

  private final Movie                   movieToScrape;
  private MediaScraper                  metadataProvider = null;
  private List<MediaScraper>            artworkScrapers  = null;
  private List<MediaScraper>            trailerScrapers  = null;

  private MediaLanguages                language         = null;
  private MediaSearchResult             result           = null;
  private MediaMetadata                 metadata         = null;

  private float                         score            = 0;
  private String                        title            = "";
  private String                        originalTitle    = "";
  private String                        overview         = "";
  private String                        year             = "";
  private String                        combinedName     = "";
  private String                        posterUrl        = "";
  private String                        tagline          = "";
  private List<Person>                  castMembers      = new ArrayList<>();
  private boolean                       scraped          = false;

  public MovieChooserModel(Movie movie, MediaScraper metadataProvider, List<MediaScraper> artworkScrapers, List<MediaScraper> trailerScrapers,
      MediaSearchResult result, MediaLanguages language) {
    this.movieToScrape = movie;
    this.metadataProvider = metadataProvider;
    this.artworkScrapers = artworkScrapers;
    this.trailerScrapers = trailerScrapers;
    this.result = result;
    this.language = language;

    score = result.getScore();
    setTitle(result.getTitle());
    setOriginalTitle(result.getOriginalTitle());
    if (result.getYear() != 0) {
      setYear(Integer.toString(result.getYear()));
    }
    setCombinedName();
  }

  /**
   * create the empty search result.
   */
  private MovieChooserModel() {
    setTitle(BUNDLE.getString("chooser.nothingfound")); //$NON-NLS-1$
    movieToScrape = null;
    combinedName = title;
  }

  public void setTitle(String title) {
    String oldValue = this.title;
    this.title = StrgUtils.getNonNullString(title);

    // Capitalize first letter of title if setting is set!
    if (MovieModuleManager.SETTINGS.getCapitalWordsInTitles()) {
      this.title = WordUtils.capitalize(this.title);
    }

    firePropertyChange("title", oldValue, this.title);
  }

  public void setOriginalTitle(String originalTitle) {
    String oldValue = this.originalTitle;
    this.originalTitle = StrgUtils.getNonNullString(originalTitle);
    firePropertyChange("originalTitle", oldValue, this.originalTitle);
  }

  public void setOverview(String overview) {
    String oldValue = this.overview;
    this.overview = StrgUtils.getNonNullString(overview);
    firePropertyChange("overview", oldValue, this.overview);
  }

  public void setCastMembers(List<Person> castMembers) {
    this.castMembers.clear();
    this.castMembers.addAll(castMembers);
    firePropertyChange("castMembers", null, castMembers);
  }

  public float getScore() {
    return score;
  }

  public String getTitle() {
    return title;
  }

  public String getOriginalTitle() {
    return originalTitle;
  }

  public String getOverview() {
    return overview;
  }

  public List<Person> getCastMembers() {
    return castMembers;
  }

  public String getPosterUrl() {
    return posterUrl;
  }

  public void setPosterUrl(String newValue) {
    String oldValue = posterUrl;
    posterUrl = StrgUtils.getNonNullString(newValue);

    firePropertyChange("posterUrl", oldValue, newValue);
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    String oldValue = this.year;
    this.year = year;
    firePropertyChange("year", oldValue, this.year);
  }

  public void setCombinedName() {
    String oldValue = this.combinedName;
    if (StringUtils.isNotBlank(getYear())) {
      this.combinedName = getTitle() + " (" + getYear() + ")";
    }
    else {
      this.combinedName = getTitle();
    }
    firePropertyChange("combinedName", oldValue, this.combinedName);
  }

  public String getCombinedName() {
    return combinedName;
  }

  /**
   * Scrape meta data.
   */
  public void scrapeMetaData() {
    try {
      // poster for preview
      setPosterUrl(result.getPosterUrl());

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setResult(result);
      options.setLanguage(language.toLocale());
      options.setCountry(MovieModuleManager.SETTINGS.getCertificationCountry());
      LOGGER.info("=====================================================");
      LOGGER.info("Scraper metadata with scraper: " + metadataProvider.getMediaProvider().getProviderInfo().getId() + ", "
          + metadataProvider.getMediaProvider().getProviderInfo().getVersion());
      LOGGER.info(options.toString());
      LOGGER.info("=====================================================");
      try {
        metadata = ((IMovieMetadataProvider) metadataProvider.getMediaProvider()).getMetadata(options);
      }
      catch (ScrapeException e) {
        LOGGER.error("searchMovieFallback", e);
        MessageManager.instance.pushMessage(
            new Message(MessageLevel.ERROR, "MovieChooser", "message.scrape.metadatamoviefailed", new String[] { ":", e.getLocalizedMessage() }));
        return;
      }
      catch (MissingIdException e) {
        LOGGER.warn("missing id for scrape");
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "MovieChooser", "scraper.error.missingid"));
        return;
      }
      catch (UnsupportedMediaTypeException ignored) {
        LOGGER.warn("unsupported media type: " + metadataProvider.getMediaProvider().getProviderInfo().getId());
        return;
      }
      setOriginalTitle(metadata.getOriginalTitle());

      List<Person> castMembers = new ArrayList<>();
      int i = 0;
      for (MediaCastMember castMember : metadata.getCastMembers(MediaCastMember.CastType.DIRECTOR)) {
        castMembers.add(new Person(castMember));

        // display at max 2 directors
        if (++i >= 2) {
          break;
        }
      }

      i = 0;
      for (MediaCastMember castMember : metadata.getCastMembers(MediaCastMember.CastType.PRODUCER)) {
        castMembers.add(new Person(castMember));

        // display at max 2 producers
        if (++i >= 2) {
          break;
        }
      }

      for (MediaCastMember castMember : metadata.getCastMembers(MediaCastMember.CastType.ACTOR)) {
        castMembers.add(new Person(castMember));
      }
      setCastMembers(castMembers);
      setOverview(metadata.getPlot());
      setTagline(metadata.getTagline());

      if (StringUtils.isBlank(posterUrl) && !metadata.getMediaArt(MediaArtworkType.POSTER).isEmpty()) {
        setPosterUrl(metadata.getMediaArt(MediaArtworkType.POSTER).get(0).getPreviewUrl());
      }

      setScraped(true);
    }
    catch (Exception e) {
      LOGGER.error("scrapeMedia", e);
      MessageManager.instance.pushMessage(
          new Message(MessageLevel.ERROR, "MovieChooser", "message.scrape.threadcrashed", new String[] { ":", e.getLocalizedMessage() }));
    }
  }

  public MediaMetadata getMetadata() {
    return metadata;
  }

  public boolean isDuplicate() {
    if (this == emptyResult) {
      return false;
    }

    for (Movie movie : MovieList.getInstance().getMovies()) {
      if (movieToScrape == movie) {
        continue;
      }

      Object id = movie.getId(result.getProviderId());
      if (id != null && id.toString().equals(result.getId())) {
        return true;
      }
    }
    return false;
  }

  public boolean isScraped() {
    return scraped;
  }

  private void setScraped(boolean newvalue) {
    boolean oldValue = scraped;
    scraped = newvalue;
    firePropertyChange("scraped", oldValue, newvalue);
  }

  public void setTagline(String newValue) {
    String oldValue = this.tagline;
    this.tagline = newValue;
    firePropertyChange("tagline", oldValue, newValue);
  }

  public String getTagline() {
    return tagline;
  }

  public void startArtworkScrapeTask(Movie movie, MovieScraperMetadataConfig config) {
    TmmTaskManager.getInstance().addUnnamedTask(new ArtworkScrapeTask(movie, config));
  }

  public void startTrailerScrapeTask(Movie movie) {
    TmmTaskManager.getInstance().addUnnamedTask(new TrailerScrapeTask(movie));
  }

  private class ArtworkScrapeTask extends TmmTask {
    private Movie                      movieToScrape;
    private MovieScraperMetadataConfig config;

    public ArtworkScrapeTask(Movie movie, MovieScraperMetadataConfig config) {
      super(BUNDLE.getString("message.scrape.artwork") + " " + movie.getTitle(), 0, TaskType.BACKGROUND_TASK);
      this.movieToScrape = movie;
      this.config = config;
    }

    @Override
    protected void doInBackground() {
      if (!scraped) {
        return;
      }

      List<MediaArtwork> artwork = new ArrayList<>();

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setArtworkType(MediaArtworkType.ALL);
      options.setMetadata(metadata);
      options.setId(MediaMetadata.IMDB, String.valueOf(metadata.getId(MediaMetadata.IMDB)));
      try {
        options.setTmdbId(Integer.parseInt(String.valueOf(metadata.getId(MediaMetadata.TMDB))));
      }
      catch (Exception e) {
        options.setTmdbId(0);
      }
      options.setLanguage(MovieModuleManager.SETTINGS.getImageScraperLanguage().toLocale());
      options.setCountry(MovieModuleManager.SETTINGS.getCertificationCountry());
      options.setFanartSize(MovieModuleManager.SETTINGS.getImageFanartSize());
      options.setPosterSize(MovieModuleManager.SETTINGS.getImagePosterSize());

      // scrape providers till one artwork has been found
      for (MediaScraper artworkScraper : artworkScrapers) {
        IMovieArtworkProvider artworkProvider = (IMovieArtworkProvider) artworkScraper.getMediaProvider();
        try {
          artwork.addAll(artworkProvider.getArtwork(options));
        }
        catch (ScrapeException e) {
          LOGGER.error("getArtwork", e);
          MessageManager.instance.pushMessage(
              new Message(MessageLevel.ERROR, movieToScrape, "message.scrape.movieartworkfailed", new String[] { ":", e.getLocalizedMessage() }));
        }
        catch (MissingIdException ignored) {
        }
      }

      // at last take the poster from the result
      if (StringUtils.isNotBlank(getPosterUrl())) {
        MediaArtwork ma = new MediaArtwork(result.getProviderId(), MediaArtworkType.POSTER);
        ma.setDefaultUrl(getPosterUrl());
        ma.setPreviewUrl(getPosterUrl());
        artwork.add(ma);
      }

      movieToScrape.setArtwork(artwork, config);
    }

  }

  private class TrailerScrapeTask extends TmmTask {
    private Movie movieToScrape;

    public TrailerScrapeTask(Movie movie) {
      super(BUNDLE.getString("message.scrape.trailer") + " " + movie.getTitle(), 0, TaskType.BACKGROUND_TASK);
      this.movieToScrape = movie;
    }

    @Override
    protected void doInBackground() {
      if (!scraped) {
        return;
      }

      List<MovieTrailer> trailer = new ArrayList<>();

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setMetadata(metadata);
      options.setId(MediaMetadata.IMDB, String.valueOf(metadata.getId(MediaMetadata.IMDB)));
      try {
        options.setTmdbId(Integer.parseInt(String.valueOf(metadata.getId(MediaMetadata.TMDB))));
      }
      catch (Exception e) {
        options.setTmdbId(0);
      }
      options.setLanguage(language.toLocale());
      options.setCountry(MovieModuleManager.SETTINGS.getCertificationCountry());

      // scrape trailers
      for (MediaScraper trailerScraper : trailerScrapers) {
        try {
          IMovieTrailerProvider trailerProvider = (IMovieTrailerProvider) trailerScraper.getMediaProvider();
          List<MediaTrailer> foundTrailers = trailerProvider.getTrailers(options);
          for (MediaTrailer mediaTrailer : foundTrailers) {
            MovieTrailer movieTrailer = new MovieTrailer(mediaTrailer);
            trailer.add(movieTrailer);
          }
        }
        catch (ScrapeException e) {
          LOGGER.error("getTrailers", e);
          MessageManager.instance.pushMessage(
              new Message(MessageLevel.ERROR, "MovieChooser", "message.scrape.movietrailerfailed", new String[] { ":", e.getLocalizedMessage() }));
        }
        catch (MissingIdException | UnsupportedMediaTypeException ignored) {
        }
      }

      // add local trailers!
      for (MediaFile mf : movieToScrape.getMediaFiles(MediaFileType.TRAILER)) {
        LOGGER.debug("adding local trailer " + mf.getFilename());
        MovieTrailer mt = new MovieTrailer();
        mt.setName(mf.getFilename());
        mt.setProvider("downloaded");
        mt.setQuality(mf.getVideoFormat());
        mt.setInNfo(false);
        mt.setUrl(mf.getFile().toUri().toString());
        trailer.add(0, mt); // add as first
      }

      movieToScrape.setTrailers(trailer);
      movieToScrape.saveToDb();
      movieToScrape.writeNFO();

      // start automatic movie trailer download
      MovieHelpers.startAutomaticTrailerDownload(movieToScrape);
    }
  }
}
