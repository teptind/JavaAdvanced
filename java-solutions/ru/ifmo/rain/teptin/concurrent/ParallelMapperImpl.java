package ru.ifmo.rain.teptin.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.nCopies;

//  java -cp . -p . -m info.kgeorgiy.java.advanced.mapper scalar ru.ifmo.rain.teptin.concurrent.IterativeParallelism

public class ParallelMapperImpl implements ParallelMapper {
    private static class QueueSynchronized<T> {
        private final Queue<T> data;

        private QueueSynchronized() {
            data = new ArrayDeque<>();
        }

        private QueueSynchronized(Queue<T> data) {
            this.data = data;
        }

        int size() {
            return data.size();
        }

        boolean isEmpty() {
            return data.isEmpty();
        }

        void add(T val) {
            synchronized (data) {
                data.add(val);
                data.notify();
            }
        }

        T poll() throws InterruptedException {
            synchronized (data) {
                while (data.isEmpty()) {
                    data.wait();
                }
                return data.poll();
            }
        }
    }

    private final QueueSynchronized<Runnable> tasks;
    private List<Thread> workers;

    public ParallelMapperImpl(int threadsNum) {
        validateInput(threadsNum);
        tasks = new QueueSynchronized<>();
        final Runnable task = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    doTask();
                }
            } catch (InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        });
        workers = new ArrayList<>();
        for (int i = 0; i < threadsNum; ++i) {
            workers.add(new Thread(task));
        }
        workers.forEach(Thread::start);
    }

    private void doTask() throws InterruptedException {
        final Runnable task;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            task = tasks.poll();
            tasks.notifyAll();
        }
        task.run();
    }

    private void validateInput(int threadsNum) {
        if (threadsNum <= 0) {
            throw new IllegalArgumentException("The number of threads must be positive");
        }
    }

    private static final int TASKS_MAX = 1000;

    private static class TasksListSyncronized<E> {
        private List<E> data;
        private int cnt = 0;

        TasksListSyncronized(int size) {
            data = new ArrayList<>(Collections.nCopies(size, null));
        }

        synchronized void setAllTasks(final int pos, E elem) {
            data.set(pos, elem);
            cnt++;
            if (cnt == data.size()) {
                notifyAll();
            }
        }

        synchronized List<E> getAllTasks() throws InterruptedException {
            while (cnt < data.size()) {
                wait();
            }
            return data;
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        TasksListSyncronized<R> tasksList = new TasksListSyncronized<>(args.size());
        List<RuntimeException> runtimeExceptions = new ArrayList<>();
        int argsSize = args.size();
        for (int[] i = {0}; i[0] < argsSize; ++i[0]) {
            synchronized (tasks) {
                int isync = i[0];
                while (tasks.size() >= TASKS_MAX) {
                    tasks.wait();
                }
                tasks.add(() -> {
                    R value = null;
                    try {
                        value = f.apply(args.get(isync));
                    } catch (RuntimeException e) {
                        synchronized (runtimeExceptions) {
                            runtimeExceptions.add(e);
                        }
                    }
                    tasksList.setAllTasks(isync, value);
                });
                tasks.notifyAll();
            }
        }

        if (!runtimeExceptions.isEmpty()) {
            final RuntimeException resultException = new RuntimeException("Runtime exceptions occurred during mapping");
            runtimeExceptions.forEach(resultException::addSuppressed);
            throw resultException;
        }
        return tasksList.getAllTasks();
    }

    @Override
    public void close() {
        workers.forEach(Thread::interrupt);
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException ignored) {}
        }
    }
}
