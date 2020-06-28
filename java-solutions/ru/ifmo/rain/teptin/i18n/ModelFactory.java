package ru.ifmo.rain.teptin.i18n;

import ru.ifmo.rain.teptin.i18n.models.*;

import java.text.ParseException;
import java.util.Locale;

public class ModelFactory {
    public static BaseModel getTextModel(String str, Locale locale, DataTypeInfo.DataType type) {
        if (type == DataTypeInfo.DataType.LINE) {
            return new LineModel(str, locale);
        }
        String s = str.trim();
        if (s.isEmpty() || s.isBlank()) {
            return null;
        }
        if (type == DataTypeInfo.DataType.WORD && Character.isLetter(s.charAt(0))) {
            return new WordModel(s, locale);
        }
        if (type == DataTypeInfo.DataType.SENTENCE) {
            return new SentenceModel(s, locale);
        }
        return null;
    }
}
