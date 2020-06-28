package ru.ifmo.rain.teptin.i18n;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataTypeInfo {
    public enum DataType {
        SENTENCE, LINE, WORD, NUMBER, MONEY, DATE;
    }
    public static Map<DataType, String> dataTypeNames = Map.of(
            DataType.SENTENCE, "sentence",
            DataType.LINE, "line",
            DataType.WORD, "word",
            DataType.NUMBER, "number",
            DataType.MONEY, "money",
            DataType.DATE, "date"
    );

    public static Map<DataType, Function<Locale, BreakIterator>> simpleIterators = Map.of(
            DataType.SENTENCE, BreakIterator::getSentenceInstance,
            DataType.LINE, (Locale locale) -> null,
            DataType.WORD, BreakIterator::getWordInstance
    );
}
