package com.udacity.webcrawler.main;

import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Inject
  private WebCrawler crawler;

  @Inject
  private Profiler profiler;

  private void run() throws Exception {
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

    CrawlResult result = crawler.crawl(config.getStartPages());
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);

    //checks the config file for the result path and if empty will print to the console
    if (config.getResultPath().isEmpty()) {
      try(Writer writer = new OutputStreamWriter(System.out);) {
        resultWriter.write(writer);
      }catch (Exception ex) {
        System.out.println(ex.getLocalizedMessage());
      }
      // if a file is found it will save to the file in this block
    } else {
      Path path = Path.of(config.getResultPath());
      resultWriter.write(path);
    }
    //checks the config file for the profile path and if empty will print to the console
    if (config.getProfileOutputPath().isEmpty()){
      try (Writer writer = new OutputStreamWriter(System.out);) {
        profiler.writeData(writer);
      }catch (Exception ex) {
        System.out.println(ex.getLocalizedMessage());
      }
      // if a file is found it will save to the file in this block
    }else {
      Path path = Path.of(config.getProfileOutputPath());
      profiler.writeData(path);
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }

    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
    new WebCrawlerMain(config).run();
  }
}
