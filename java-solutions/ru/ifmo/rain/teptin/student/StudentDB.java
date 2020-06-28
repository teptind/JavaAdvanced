package ru.ifmo.rain.teptin.student;

import info.kgeorgiy.java.advanced.student.AdvancedStudentGroupQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// java -cp . -p . -m info.kgeorgiy.java.advanced.student AdvancedStudentGroupQuery ru.ifmo.rain.teptin.student.StudentDB
@SuppressWarnings("unused")
public class StudentDB implements AdvancedStudentGroupQuery {
    private static final Comparator<Student> STUDENT_BY_NAME =
            Comparator.comparing(Student::getLastName)
                    .thenComparing(Student::getFirstName)
                    .thenComparing(Student::getId);

    private static final Comparator<Student> STUDENT_BY_ID =
            Comparator.comparingInt(Student::getId)
                    .thenComparing(Student::getLastName)
                    .thenComparing(Student::getFirstName);

    private static <R> List<R> gettingQuery(List<Student> students, Function<Student, R> f) {
        return students.stream().map(f).collect(Collectors.toList());
    }

    private static <R> Stream<R> mappingStream(List<Student> students, Function<Student, R> f) {
        return students.stream().map(f);
    }

    private static <R> List<Student> sortingQuery(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream().sorted(comparator).collect(Collectors.toList());
    }

    private static <R> R findingQuery(Collection<Student> students, Predicate<Student> predicate,
                                      Comparator<Student> comparator, Collector<Student, ?, R> collector) {
        return students.stream().filter(predicate).sorted(comparator).collect(collector);
    }

    private static Function<Student, String> getFullName = (Student student) ->
            String.format("%s %s", student.getFirstName(), student.getLastName());

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return gettingQuery(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return gettingQuery(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return gettingQuery(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return gettingQuery(students, getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return mappingStream(students, Student::getFirstName).sorted().collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(STUDENT_BY_ID).map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortingQuery(students, STUDENT_BY_ID);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortingQuery(students, STUDENT_BY_NAME);
    }

    private <R> List<Student> findStudentsBy(Collection<Student> students, Predicate<Student> predicate) {
        return findingQuery(students, predicate, STUDENT_BY_NAME, Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsBy(students, student -> name.equals(student.getFirstName()));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsBy(students, student -> name.equals(student.getLastName()));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudentsBy(students, student -> group.equals(student.getGroup()));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return findingQuery(students, student -> group.equals(student.getGroup()),
                Comparator.naturalOrder(),
                Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }

    // GroupQuery

    private static final Comparator<Group> GROUP_BY_NAME = Comparator.comparing(Group::getName);

    private static Stream<Group> sortedGroupStream(Collection<Student> students, Function<Map.Entry<String, List<Student>>, Group> groupMaker) {
        return students.stream().collect(Collectors.groupingBy(Student::getGroup, Collectors.toList()))
                .entrySet().stream()
                .map(groupMaker)
                .sorted(GROUP_BY_NAME);
    }

    private static List<Group> gettingQueryGroup(Collection<Student> students, Comparator<Student> comparator) {
        return sortedGroupStream(students, entry -> new Group(
                entry.getKey(),
                entry.getValue().stream()
                        .sorted(comparator)
                        .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return gettingQueryGroup(students, STUDENT_BY_NAME);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return gettingQueryGroup(students, STUDENT_BY_ID);
    }

    private String getMaxGroup(Collection<Student> students, Comparator<Group> comparator) {
        return sortedGroupStream(students, entry -> new Group(entry.getKey(), entry.getValue()))
                .max(comparator)
                .map(Group::getName)
                .orElse("");
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getMaxGroup(students,
                Comparator.comparingInt((Group g) -> g.getStudents().size()).thenComparing(GROUP_BY_NAME.reversed()));
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getMaxGroup(students,
                Comparator.comparingInt((Group g) -> getDistinctFirstNames(g.getStudents()).size()).thenComparing(GROUP_BY_NAME.reversed()));
    }

    // Advanced

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return students.stream()
                .collect(Collectors.groupingBy(getFullName, Collectors.mapping(Student::getGroup, Collectors.toSet())))
                .entrySet().stream()
                .max(Map.Entry.<String, Set<String>>comparingByValue(Comparator.comparingInt(Set::size))
                    .thenComparing(Map.Entry.comparingByKey(String::compareTo)))
                .map(Map.Entry::getKey)
                .orElse("");
    }


    private static <R> List<R> gettingQueryWithIndices(List<Student> students, Function<Student, R> f, int[] indices) {
        return Arrays.stream(indices).mapToObj(students::get)
                .map(f)
                .collect(Collectors.toList());
    }

    private <R> List<R> getAsList(Collection<R> students) {
        return new ArrayList<>(students);
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return gettingQueryWithIndices(getAsList(students), Student::getFirstName, indices);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return gettingQueryWithIndices(getAsList(students), Student::getLastName, indices);
    }

    @Override
    public List<String> getGroups(Collection<Student> students, int[] indices) {
        return gettingQueryWithIndices(getAsList(students), Student::getGroup, indices);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return gettingQueryWithIndices(getAsList(students), getFullName, indices);
    }
}
