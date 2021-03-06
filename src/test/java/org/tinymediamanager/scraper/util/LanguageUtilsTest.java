package org.tinymediamanager.scraper.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.tinymediamanager.core.BasicTest;

public class LanguageUtilsTest extends BasicTest {

  @Before
  public void setup() throws Exception {
    super.setup();
    Locale.setDefault(Locale.ENGLISH);
  }

  @Test
  public void localizedCountries() {
    assertEqual("", LanguageUtils.getLocalizedCountry());
    System.out.println(LanguageUtils.getLocalizedCountry("German", "dE"));

    // Java 8: Vereinigte Staaten von Amerika
    // Java 9: Vereinigte Staaten
    assertThat(LanguageUtils.getLocalizedCountryForLanguage(Locale.GERMAN, "USA", "en_US", "US")).startsWith("Vereinigte Staaten");
    assertThat(LanguageUtils.getLocalizedCountryForLanguage(Locale.GERMANY, "USA", "en_US", "US")).startsWith("Vereinigte Staaten");
    assertThat(LanguageUtils.getLocalizedCountryForLanguage("de", "USA", "en_US", "US")).startsWith("Vereinigte Staaten");

    assertEqual("United States", LanguageUtils.getLocalizedCountryForLanguage("en", "USA", "en_US", "US"));
    assertEqual("United States", LanguageUtils.getLocalizedCountryForLanguage("en", "Vereinigte Staaten von Amerika", "Vereinigte Staaten"));

    // Java 8: Etats-Unis
    // Java 9: États-Unis
    assertThat(LanguageUtils.getLocalizedCountryForLanguage("fr", "USA", "en_US", "US")).matches("(E|É)tats\\-Unis");
    assertEqual("West Germany", LanguageUtils.getLocalizedCountryForLanguage("de", "West Germany", "XWG"));
  }

  @Test
  public void customTest() {
    assertEqual("Basque", LanguageUtils.getEnglishLanguageNameFromLocalizedString("Basque"));
    assertEqual("Basque", LanguageUtils.getEnglishLanguageNameFromLocalizedString("baq"));
    assertEqual("Baskisch", LanguageUtils.getLocalizedLanguageNameFromLocalizedString(Locale.GERMAN, "Basque"));
    assertEqual("Baskisch", LanguageUtils.getLocalizedLanguageNameFromLocalizedString(Locale.GERMAN, "baq"));
    assertEqual("Basque", LanguageUtils.getLocalizedLanguageNameFromLocalizedString(Locale.ENGLISH, "Baskisch"));

    assertEqual("Telugu", LanguageUtils.getEnglishLanguageNameFromLocalizedString("tel"));
    assertEqual("Tamil", LanguageUtils.getEnglishLanguageNameFromLocalizedString("tam"));
    assertEqual("Telugu", LanguageUtils.getLocalizedLanguageNameFromLocalizedString(Locale.GERMAN, "tel"));
    // ??? assertEqual("Tsongaisch", LanguageUtils.getLocalizedLanguageNameFromLocalizedString(Locale.GERMAN, "tam"));
  }
}
