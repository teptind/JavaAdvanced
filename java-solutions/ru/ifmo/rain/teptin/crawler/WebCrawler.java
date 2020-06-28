package ru.ifmo.rain.teptin.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

//  java -cp . -p . -m info.kgeorgiy.java.advanced.crawler easy ru.ifmo.rain.teptin.crawler.WebCrawler

public class WebCrawler implements Crawler {

    private final Downloader downloader;
    private final int perHost;
    private final ExecutorService downloadingPool;
    private final ExecutorService extractingPool;
    private final Map<String, Semaphore> hostPermits;

    public static void main(String[] args) {
        try {
            int[] validatedArgs = getValidatedArgs(args);
            String URL = args[0];
            int depth = validatedArgs[0];
            int downloaders = validatedArgs[1];
            int extractors = validatedArgs[2];
            int perHost = validatedArgs[3];
            try (Crawler crawler = new WebCrawler(new CachingDownloader(), downloaders, extractors, perHost)) {
                crawler.download(URL, depth);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to initialize downloader: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Usage: WebCrawler <url> [depth] [downloaders] [extractors] [perHost]\n" + e.getMessage());
        }
    }

    private static int[] getValidatedArgs(String[] args) {
        int[] validatedArgs = {1, 1, 1, 1};  // default values depth = downloaders = extractors = perHost = 1
        if (args == null || args.length == 0 || args.length > 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Expected not null arguments and between 1 and 5 arguments");
        }
        IllegalArgumentException positiveArgsException =
                new IllegalArgumentException("Each argument except url must be a positive integer");
        for (int i = 1; i < 5; ++i) {
            if (args.length > i) {
                try {
                   validatedArgs[i] = Integer.parseInt(args[i]);
                   if (i > 1 && validatedArgs[i] < 1) {  //  depth can be 0
                       throw positiveArgsException;
                   }
                } catch (NumberFormatException ignored) {
                    throw positiveArgsException;
                }
            }
        }
        return validatedArgs;
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        this.downloadingPool = Executors.newFixedThreadPool(downloaders);
        this.extractingPool = Executors.newFixedThreadPool(extractors);
        this.hostPermits = new ConcurrentHashMap<>();
    }
    private void download(String url, int depth, Set<String> downloadedLinks, Map<String, IOException> errors, Phaser phaser) {
        if (depth == 0) {
            return;
        }
        Runnable dowloadingTask = () -> {
            try {
                String host = URLUtils.getHost(url);
                hostPermits.putIfAbsent(host, new Semaphore(perHost));
                try {
                    hostPermits.get(host).acquire();
                    Document document = downloader.download(url);
                    if (depth == 1) { return; }  // TODO
                    Runnable extractingTask = () -> {
                        try {
                            document.extractLinks().forEach(link -> {
                                if (downloadedLinks.add(link)) {
                                    download(link, depth - 1, downloadedLinks, errors, phaser);
                                }
                            });
                        } catch (IOException e) {
                            errors.put(url, e);
                        } finally {
                            phaser.arrive();
                        }
                    };
                    phaser.register();
                    extractingPool.submit(extractingTask);
                } catch (InterruptedException ignored) {
                } finally {
                    hostPermits.get(host).release();
                }
            } catch (IOException e) {
                errors.put(url, e);
            }
            finally {
                phaser.arrive();
            }
        };
        phaser.register();
        downloadingPool.submit(dowloadingTask);
    }
    @Override
    public Result download(String url, int depth) {
        Phaser phaser = new Phaser(1);
        Set<String> downloadedLinks = ConcurrentHashMap.newKeySet();
        downloadedLinks.add(url);
        Map<String, IOException>errors = new ConcurrentHashMap<>();
        download(url, depth, downloadedLinks, errors, phaser);
        phaser.arriveAndAwaitAdvance();
        downloadedLinks.removeAll(errors.keySet());
        return new Result(new ArrayList<>(downloadedLinks), errors);
    }

    @Override
    public void close() {
        closeExecutor(extractingPool);
        closeExecutor(downloadingPool);
    }

    private void closeExecutor(ExecutorService executorService) {
        final int mSecToAwait = 500;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(mSecToAwait, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
