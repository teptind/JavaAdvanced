package ru.ifmo.rain.teptin.i18n.models;

import ru.ifmo.rain.teptin.i18n.DataTypeInfo;

import java.text.BreakIterator;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

public class NumberModel extends BaseModel {
    public NumberModel(String fileData, int pos, Locale locale) throws ParseException {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        Number number = numberFormat.parse(fileData, new ParsePosition(pos));
        if (number == null) {
            throw new ParseException("", pos);
        }
        value = number.doubleValue();
    }

    @Override
    public String getAsString(Locale locale) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        return numberFormat.format(value);
    }
}
