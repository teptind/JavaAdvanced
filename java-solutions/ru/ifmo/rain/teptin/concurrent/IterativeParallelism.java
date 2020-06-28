package ru.ifmo.rain.teptin.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

//  java -cp . -p . -m info.kgeorgiy.java.advanced.concurrent scalar ru.ifmo.rain.teptin.concurrent.IterativeParallelism

/**
 * The Implementation of {@link ScalarIP} interface with iterative parallelism
 */
@SuppressWarnings("unused")
public class IterativeParallelism implements ScalarIP {
    private final ParallelMapper mapper;

    public IterativeParallelism() {
        this.mapper = null;
    }

    public IterativeParallelism(final ParallelMapper mapper) {
        this.mapper = mapper;
    }

    private <T> List<Stream<? extends T>> getThreadDistribution(int threads, List<? extends T> list) {
        List<Stream<? extends T>> distribution = new ArrayList<>();
        if (list.isEmpty()) {
            return distribution;
        }
        if (threads < 0) {
            throw new IllegalArgumentException("A positive number of threads is required");
        }
        int bucketSize = list.size() / threads;
        int residue = list.size() % threads;
        int currInd = 0;
        int currBucketSize;
        for (int t = 0; t < threads; t++) {
            currBucketSize = bucketSize + ((t < residue) ? 1 : 0);
            if (currBucketSize > 0) {
                distribution.add(list.subList(currInd, currInd + currBucketSize).stream());
            }
            currInd += currBucketSize;
        }
        return distribution;
    }

    private void joinThreads(List<Thread> threads) throws InterruptedException {
        InterruptedException myInterruptedException = new InterruptedException();
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e1) {
                myInterruptedException.addSuppressed(e1);
            }
        });
        if (myInterruptedException.getSuppressed().length > 0) {
            throw myInterruptedException;
        }
    }

    private <T, U, V> V runParallel(int threads, List<? extends T> list,
                                    Function<Stream<? extends T>, U> func,
                                    Function<Stream<? extends U>, V> reducer) throws InterruptedException {
        List<Stream<? extends T>> distribution = getThreadDistribution(threads, list);
        final int resultsSize = distribution.size();
        List<U> results;
        if (mapper == null) {
            List<Thread> workers = new ArrayList<>();
            results = new ArrayList<>(Collections.nCopies(resultsSize, null));
            List<InterruptedException> resultsExceptions = new ArrayList<>();
            for (int i = 0; i < resultsSize; ++i) {
                int i1 = i;
                Thread t = new Thread(
                        () -> results.set(i1, func.apply(distribution.get(i1))));
                workers.add(t);
                t.start();
            }
            joinThreads(workers);
        } else {
            results = mapper.map(func, distribution);
        }
        return reducer.apply(results.stream());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return runParallel(threads, values,
                stream -> stream.max(comparator).orElseThrow(),
                stream -> stream.max(comparator).orElseThrow());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return runParallel(threads, values,
                stream -> stream.min(comparator).orElseThrow(),
                stream -> stream.min(comparator).orElseThrow());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return runParallel(threads, values,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(Boolean::booleanValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return runParallel(threads, values,
                stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(Boolean::booleanValue));
    }
}
