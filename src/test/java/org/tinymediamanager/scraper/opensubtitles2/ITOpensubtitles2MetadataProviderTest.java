package org.tinymediamanager.scraper.opensubtitles2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.scraper.opensubtitles2.model.DownloadRequest;
import org.tinymediamanager.scraper.opensubtitles2.model.DownloadResponse;
import org.tinymediamanager.scraper.opensubtitles2.model.LoginRequest;
import org.tinymediamanager.scraper.opensubtitles2.model.LoginResponse;
import org.tinymediamanager.scraper.opensubtitles2.model.SearchResponse;

import retrofit2.Response;

public class ITOpensubtitles2MetadataProviderTest extends BasicTest {

  private static Controller controller;

  @Before
  public void setUpBeforeTest() throws Exception {
    setLicenseKey();

    controller = new Controller();
    controller.setApiKey("");
    controller.setUsername("");
    controller.setPassword("");
    controller.authenticate();
  }

  @Test
  public void testLogin() throws Exception {
    Controller controller = new Controller();
    controller.setApiKey("C7tXwfp3OSrQv1EA03d45obfGgh3oEjn");

    LoginRequest loginRequest = new LoginRequest();
    loginRequest.username = "tinymediamanager";
    loginRequest.password = "YQ8#M8mRdVkrvPENRDnL6v96";

    Response<LoginResponse> response = controller.getService().login(loginRequest).execute();
    assertThat(response.body()).isNotNull();
    assertThat(response.body().status).isEqualTo(200);
  }

  @Test
  public void testSearchByHash() throws Exception {
    // hit by movie hash
    Map<String, String> query = new TreeMap<>();
    query.put("moviehash", "8e245d9679d31e12");
    query.put("moviehash_match", "only");
    query.put("languages", "en");

    Response<SearchResponse> response = controller.getService().search(query).execute();
    assertThat(response.body()).isNotNull();
    assertThat(response.body().totalCount).isNotZero();
    assertThat(response.body().data.get(0).attributes.featureDetails.imdbId).isEqualTo("3358048");
  }

  @Test
  public void testSearchByImdbId() throws Exception {
    // hit by imdb id (IMDB id is stored as integer on opensubtitles.com)
    Map<String, String> query = new TreeMap<>();
    query.put("imdb_id", String.valueOf(OpenSubtitles2SubtitleProvider.formatImdbId("tt0103064")));
    query.put("languages", "en");

    Response<SearchResponse> response = controller.getService().search(query).execute();
    assertThat(response.body()).isNotNull();
    assertThat(response.body().totalCount).isNotZero();
    assertThat(response.body().data.get(0).attributes.featureDetails.imdbId)
        .isEqualTo(String.valueOf(OpenSubtitles2SubtitleProvider.formatImdbId("tt0103064")));
  }

  @Test
  public void testSearchByTmdbId() throws Exception {
    // hit by tmdb id
    Map<String, String> query = new TreeMap<>();
    query.put("tmdb_id", String.valueOf(280));
    query.put("languages", "en");

    Response<SearchResponse> response = controller.getService().search(query).execute();
    assertThat(response.body()).isNotNull();
    assertThat(response.body().totalCount).isNotZero();
    assertThat(response.body().data.get(0).attributes.featureDetails.imdbId)
        .isEqualTo(String.valueOf(OpenSubtitles2SubtitleProvider.formatImdbId("tt0103064")));
  }

  @Test
  public void testSearchByQuery() throws Exception {
    // hit by query
    Map<String, String> query = new TreeMap<>();
    query.put("query", "Terminator 2: Judgment Day");
    query.put("languages", "en");

    Response<SearchResponse> response = controller.getService().search(query).execute();
    assertThat(response.body()).isNotNull();
    assertThat(response.body().totalCount).isNotZero();
    assertThat(response.body().data.get(0).attributes.featureDetails.imdbId)
        .isEqualTo(String.valueOf(OpenSubtitles2SubtitleProvider.formatImdbId("tt0103064")));
  }

  @Test
  public void testDownload() throws Exception {
    DownloadRequest request = new DownloadRequest();
    request.file_id = 1616158;

    Response<DownloadResponse> response = controller.getService().download(request).execute();
    assertThat(response.body()).isNotNull();
    assertThat(response.body().link).isNotEmpty();
    assertThat(response.body().requestsConsumed).isGreaterThan(0);
  }
}
