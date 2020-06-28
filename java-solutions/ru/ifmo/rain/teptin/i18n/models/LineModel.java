package ru.ifmo.rain.teptin.i18n.models;

import ru.ifmo.rain.teptin.i18n.DataTypeInfo;

import java.text.Collator;
import java.util.Locale;

public class LineModel extends TextModel {
    public LineModel(String s, Locale locale) {
        text = s;
        value = (double)s.length();
        collator = Collator.getInstance(locale);
    }

}
