package ru.ifmo.rain.teptin.i18n.models;

import ru.ifmo.rain.teptin.i18n.DataTypeInfo;

import java.text.BreakIterator;
import java.text.Collator;
import java.util.Locale;

public class SentenceModel extends TextModel {
    public SentenceModel(String s, Locale locale) {
        text = s.replace(System.lineSeparator(), " ");
        value = s.length();
        collator = Collator.getInstance(locale);
    }

}
