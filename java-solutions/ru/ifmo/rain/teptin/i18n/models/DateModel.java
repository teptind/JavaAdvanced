package ru.ifmo.rain.teptin.i18n.models;

import ru.ifmo.rain.teptin.i18n.DataTypeInfo;

import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;

public class DateModel extends BaseModel {
    private final Date date;

    public DateModel(String fileData, int pos, Locale locale) throws ParseException {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        date = dateFormat.parse(fileData, new ParsePosition(pos));
        if (date == null) {
            throw new ParseException("", pos);
        }
        value = (double)(date.getTime() / 1000L);
    }

    @Override
    public String getAsString(Locale locale) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        return dateFormat.format(date);
    }
}
