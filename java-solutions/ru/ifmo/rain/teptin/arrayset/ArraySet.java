package ru.ifmo.rain.teptin.arrayset;

import java.util.*;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {

    private final List<E> data;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Comparator<? super E> comparator) {
        this(Collections.emptyList(), comparator);
    }

    public ArraySet(Collection<? extends E> collection) {
        this(collection, null);
    }

    private Comparator<? super E> checkComparator(Comparator<? super E> comparator) {
        return Comparator.naturalOrder().equals(comparator) ? null : comparator;
    }

    private ArraySet(List<E> List, Comparator<? super E> comparator) {
        data = List;
        this.comparator = checkComparator(comparator);
    }

    public ArraySet(Collection<? extends E> collection, Comparator<? super E> comparator) {
        SortedSet sortedSet = new TreeSet<>(comparator);
        sortedSet.addAll(collection);
        data = new ArrayList<>(sortedSet);
        this.comparator = checkComparator(comparator);
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    private SortedSet<E> subSetImpl(E fromElement, E toElement, boolean inclusiveTo) {
        if (data.isEmpty()) {
            return this;
        }
        int fromIndex = findIndex(fromElement);
        int toIndex = findIndex(toElement);
        if (fromIndex > toIndex) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<E>(data.subList(fromIndex, inclusiveTo ? toIndex + 1 : toIndex), comparator);
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        return subSetImpl(fromElement, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        E first_bound = isEmpty() ? null : first();
        return subSetImpl(first_bound, toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        E last_bound = isEmpty() ? null : last();
        return subSetImpl(fromElement, last_bound, true);
    }

    @Override
    public E first() {
        checkEmpty();
        return data.get(0);
    }

    @Override
    public E last() {
        checkEmpty();
        return data.get(size() - 1);
    }

    private void checkEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
    }

    private int findIndex(E elem) {
        int index = Collections.binarySearch(data, Objects.requireNonNull(elem), comparator);
        if (index < 0) {
            return -index - 1;
        } else {
            return index;
        }
    }

    private int compare(E e1, E e2) {
        return (comparator == null) ? ((Comparable<E>) e1).compareTo(e2) : comparator.compare(e1, e2);
    }

    @Override
    public boolean contains(Object elem) {
        int index = Collections.binarySearch(data, Objects.requireNonNull((E)elem), comparator);
        return index >= 0;
    }
}

// java -cp . -p . -m info.kgeorgiy.java.advanced.arrayset SortedSet ru.ifmo.rain.teptin.arrayset.ArraySet M32342