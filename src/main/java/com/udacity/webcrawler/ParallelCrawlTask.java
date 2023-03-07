package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class ParallelCrawlTask  extends RecursiveTask<Void> {

    private final String url;
    private final Instant deadline;
    private final int maxDepth;
    private final ConcurrentMap<String, Integer> counts;
    private final ConcurrentSkipListSet<String> visitedUrls;
    private final Clock clock;
    private final com.udacity.webcrawler.parser.PageParserFactory parserFactory;
    private final List<Pattern> ignoredUrls;

    public static Lock lock = new ReentrantLock();

    // Crawl task created to start new task with the fork join pool
    public ParallelCrawlTask(String url,
                             Instant deadline,
                             int maxDepth,
                             ConcurrentMap<String, Integer> counts,
                             ConcurrentSkipListSet<String> visitedUrls,
                             Clock clock,
                             PageParserFactory parserFactory,
                             List<Pattern> ignoredUrls) {
        this.url = url;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
        this.clock = clock;
        this.parserFactory = parserFactory;
        this.ignoredUrls = ignoredUrls;
    }

    // modeled after the sequential crawl internal but adjusted for parallelism
    @Override
    protected Void compute() {
        // checks if the depth has already been met of time is up
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            // return nulls required to prevent errors
            return null;
        }
        // checks if the url is a match to a url that should not run
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return null;
            }
        }
        // checks if url has already been visited
        try {
            lock.lock();
            if (visitedUrls.contains(url)) {
                return null;
            }
            visitedUrls.add(url);
        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
        }finally {
            lock.unlock();
        }
        PageParser.Result result = parserFactory.get(url).parse();
        for (ConcurrentMap.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
            if (counts.containsKey(e.getKey())) {
                counts.put(e.getKey(), e.getValue() + counts.get(e.getKey()));
            } else {
                counts.put(e.getKey(), e.getValue());
            }
        }
        // creates subtasks to submit to the fork join pool
        List<ParallelCrawlTask> subtasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            subtasks.add(new ParallelCrawlTask(
                    link, deadline, maxDepth - 1, counts, visitedUrls, clock, parserFactory,ignoredUrls
            ));
            invokeAll(subtasks);
        }
        return null;
    }
}
