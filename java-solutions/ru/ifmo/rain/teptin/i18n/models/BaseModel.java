package ru.ifmo.rain.teptin.i18n.models;

import ru.ifmo.rain.teptin.i18n.DataTypeInfo;
import ru.ifmo.rain.teptin.i18n.ModelFactory;

import java.text.BreakIterator;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseModel implements Comparable<BaseModel> {
    protected double value;

    public double getValue() {
        return value;
    }

    @Override
    public int compareTo(BaseModel o) {
        return Double.compare(value, o.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseModel baseModel = (BaseModel) o;
        return Double.compare(baseModel.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public abstract String getAsString(Locale locale);
}
