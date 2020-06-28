package ru.ifmo.rain.teptin.i18n.models;

import ru.ifmo.rain.teptin.i18n.DataTypeInfo;

import java.text.BreakIterator;
import java.text.Collator;
import java.util.Locale;

public class WordModel extends TextModel {
    public WordModel(String s, Locale locale) {
        this.text = s;
        value = (double)s.length();
        collator = Collator.getInstance(locale);
    }

}
