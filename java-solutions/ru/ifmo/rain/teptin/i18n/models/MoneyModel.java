package ru.ifmo.rain.teptin.i18n.models;

import ru.ifmo.rain.teptin.i18n.DataTypeInfo;

import java.text.BreakIterator;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.Locale;

public class MoneyModel extends BaseModel {
    public MoneyModel(String fileData, int pos, Locale locale) throws ParseException {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        Number number = currencyFormat.parse(fileData, new ParsePosition(pos));
//        System.out.println(String.format("text: %s\npos: %d\n" + number + "\n", fileData, pos));
        if (number == null) {
            throw new ParseException("", pos);
        }
        value = number.doubleValue();
    }

    @Override
    public String getAsString(Locale locale) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        return currencyFormat.format(value);
    }
}
