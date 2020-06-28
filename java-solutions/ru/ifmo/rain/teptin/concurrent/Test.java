package ru.ifmo.rain.teptin.concurrent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        List<Integer> numList = new ArrayList<>();
        numList.add(51);
        numList.add(14);
        numList.add(11);
        numList.add(12);
        numList.add(31);
        numList.add(21);
        numList.add(12);
        numList.add(37);
        IterativeParallelism myScalar = new IterativeParallelism();
        myScalar.maximum(3, numList, Comparator.naturalOrder());
    }
}
