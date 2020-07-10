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
package org.tinymediamanager.scraper.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.tinymediamanager.scraper.util.CacheMap;
import org.tinymediamanager.scraper.util.Pair;

import okhttp3.Headers;

/**
 * The class InMemoryCachedUrl is used to cache some sort of Urls (e.g. when they are accessed several times in a short period)<br />
 * this cache caches HTTP responses up to 600 secs
 *
 * @author Manuel Laggner
 */
public class InMemoryCachedUrl extends Url {
  public static final CacheMap<CachedRequest, CachedResponse> CACHE = new CacheMap<>(60, 10);

  public InMemoryCachedUrl(String url) throws MalformedURLException {
    this.url = url;
    if (url.contains("|")) {
      splitHeadersFromUrl();
    }

    // morph to URI to check syntax of the url
    try {
      this.uri = morphStringToUri(url);
    }
    catch (URISyntaxException e) {
      throw new MalformedURLException(url);
    }
  }

  @Override
  public InputStream getInputStream() throws IOException, InterruptedException {
    CachedRequest cachedRequest = new CachedRequest(url, headersRequest);

    CachedResponse cachedResponse = CACHE.get(cachedRequest);
    if (cachedResponse == null) {
      // need to fetch it with a real request
      Url url = new Url(this.url);
      url.headersRequest = headersRequest;
      try (InputStream is = url.getInputStream();
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          GZIPOutputStream gzip = new GZIPOutputStream(outputStream)) {
        if (is == null) {
          return null;
        }

        IOUtils.copy(is, gzip);
        gzip.finish(); // finish writing of the gzip output stream

        // and now fill the CachedRequest object with the result
        cachedResponse = new CachedResponse(url, outputStream.toByteArray());
        if (url.responseCode >= 200 && url.responseCode < 300) {
          CACHE.put(cachedRequest, cachedResponse);
        }
      }
    }

    responseCode = cachedResponse.responseCode;
    responseMessage = cachedResponse.responseMessage;
    responseCharset = cachedResponse.responseCharset;
    responseContentType = cachedResponse.responseContentType;
    responseContentLength = cachedResponse.responseContentLength;

    headersResponse = cachedResponse.headersResponse;
    headersRequest.addAll(cachedResponse.headersRequest);

    return new GZIPInputStream(new ByteArrayInputStream(cachedResponse.content));
  }

  public static void clearCache() {
    CACHE.cleanup(true);
  }

  /**
   * is this url/header combination already cached?
   *
   * @return true/false
   */
  public boolean isCached() {
    CachedRequest cachedRequest = new CachedRequest(url, headersRequest);
    return CACHE.get(cachedRequest) != null;
  }

  private static class CachedRequest {
    final String                     url;
    final List<Pair<String, String>> headersRequest;

    public CachedRequest(String url, List<Pair<String, String>> headersRequest) {
      this.url = url;
      this.headersRequest = headersRequest;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null || getClass() != o.getClass())
        return false;
      CachedRequest that = (CachedRequest) o;
      return url.equals(that.url) && headersRequest.equals(that.headersRequest);
    }

    @Override
    public int hashCode() {
      return Objects.hash(url, headersRequest);
    }
  }

  /**
   * A inner class for representing cached entries
   */
  private static class CachedResponse {
    final byte[]                     content;

    final int                        responseCode;
    final String                     responseMessage;
    final Charset                    responseCharset;
    final String                     responseContentType;
    final long                       responseContentLength;

    final Headers                    headersResponse;
    final List<Pair<String, String>> headersRequest = new ArrayList<>();

    CachedResponse(Url url, byte[] content) {
      this.content = content;

      this.responseCode = url.responseCode;
      this.responseMessage = url.responseMessage;
      this.responseCharset = url.responseCharset;
      this.responseContentType = url.responseContentType;
      this.responseContentLength = url.responseContentLength;

      this.headersResponse = url.headersResponse;
      this.headersRequest.addAll(url.headersRequest);
    }
  }
}
