package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Utility class to write a {@link CrawlResult} to file.
 */
public final class CrawlResultWriter {
  private final CrawlResult result;

  /**
   * Creates a new {@link CrawlResultWriter} that will write the given {@link CrawlResult}.
   */
  public CrawlResultWriter(CrawlResult result) {
    this.result = Objects.requireNonNull(result);
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Path}.
   *
   * <p>If a file already exists at the path, the existing file should not be deleted; new data
   * should be appended to it.
   *
   * @param path the file path where the crawl result data should be written.
   */
  public void write(Path path) {
    // TODO: Fill in this method.
    //Takes the path, creates a writer, then calls the writer to write JSON
    try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)){
      write(writer);
    } catch (Exception ex) {
      System.out.println(ex.getLocalizedMessage());
    }
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Writer}.
   *
   * @param writer the destination where the crawl result data should be written.
   */
  public void write(Writer writer) {
    // TODO: Fill in this method.
    // Maps JSON to a CrawlerConfiguration object then returns object
    ObjectMapper objectMapper = new ObjectMapper();
    // AUTO_CLOSE features are interfering and are not needed here.
    objectMapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
    objectMapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    // try block used instead of throws IO exception to catch IO exceptions
    // without having to do much refactoring of code
    try {
      objectMapper.writeValue(writer, result);
    } catch (Exception ex) {
      System.out.println(ex.getLocalizedMessage());
    }
  }
}
