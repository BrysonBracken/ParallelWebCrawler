package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A static utility class that loads a JSON configuration file.
 */
public final class ConfigurationLoader {

  private final Path path;

  /**
   * Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}.
   */
  public ConfigurationLoader(Path path) {
    this.path = Objects.requireNonNull(path);
  }

  /**
   * Loads configuration from this {@link ConfigurationLoader}'s path
   *
   * @return the loaded {@link CrawlerConfiguration}.
   */
  public CrawlerConfiguration load() {
    // Takes JSON, passes it to the read function, then returns that config
    try (Reader reader = Files.newBufferedReader(path)){
      return read(reader);
    } catch (Exception ex) {
      System.out.println(ex.getLocalizedMessage());
      return null;
    }
  }

  /**
   * Loads crawler configuration from the given reader.
   *
   * @param reader a Reader pointing to a JSON string that contains crawler configuration.
   * @return a crawler configuration
   */
  public static CrawlerConfiguration read(Reader reader) {
    // Maps JSON to a CrawlerConfiguration object then returns object
    ObjectMapper objectMapper = new ObjectMapper();
    // AUTO_CLOSE_SOURCE is interfering and is not needed here.
    objectMapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
    // try block used instead of throws IO exception to catch IO exceptions
    // without having to do much refactoring of code
    try {
      return objectMapper.readValue(reader, CrawlerConfiguration.Builder.class).build();
    } catch (Exception ex) {
      System.out.println(ex.getLocalizedMessage());
      return null;
    }
  }
}
