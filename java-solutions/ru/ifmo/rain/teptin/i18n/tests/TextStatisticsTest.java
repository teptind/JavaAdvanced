package ru.ifmo.rain.teptin.i18n.tests;

import org.junit.Assert;
import org.junit.Test;
import ru.ifmo.rain.teptin.i18n.TextStatistics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;

import static org.junit.Assert.fail;

public class TextStatisticsTest {
    String[] inputLocaleNames = {"ru_RU", "en_US", "ru_UA", "en_CA", "cs_CZ", "ar_BH"};
    String[] outputLocaleNames = {"ru_RU", "en_US"};
    private Map<String, Map<String, String>> getTestMap(String inputLocaleName, String outputLocalName, String inputFileName, String outputFileName) {
        try {
            return TextStatistics.collectStatisticsToHTML(
                    inputLocaleName,
                    outputLocalName,
                    inputFileName,
                    outputFileName);
        } catch (IOException e) {
            fail("Cannot collect statistics " + e.getMessage());
            return null;
        }
    }
    private void SimpleLineTest(String inputLocaleName, String outputLocalName) {
        var res = getTestMap(inputLocaleName, outputLocalName,
                "./ru/ifmo/rain/teptin/i18n/tests/constant/test1.txt",
                "./ru/ifmo/rain/teptin/i18n/html/test1.html");
        assert res != null;
        Assert.assertEquals(res.get("line").get("number"), "19");
        Assert.assertEquals(res.get("line").get("distinct_number"), "17");
    }

    @Test
    public void LineTest() {
        SimpleLineTest("ru_RU", "en_US");
        SimpleLineTest("en_GB", "ru_RU");
        SimpleLineTest("ar_BH", "en_US");
    }
    @Test
    public void ArabicWordsTest() {
        var res_ru = getTestMap("ar_BH", "ru_RU",
                "./ru/ifmo/rain/teptin/i18n/tests/constant/test1.txt",
                "./ru/ifmo/rain/teptin/i18n/html/test1.html");
        var res_en = getTestMap("ar_BH", "en_US",
                "./ru/ifmo/rain/teptin/i18n/tests/constant/test1.txt",
                "./ru/ifmo/rain/teptin/i18n/html/test1.html");

        assert res_ru != null;
        Assert.assertEquals(res_ru.get("word").get("distinct_number"), "162");
        Assert.assertEquals(res_ru.get("word").get("number"), "218");
        Assert.assertEquals(res_ru.get("word").get("max"), "это");
        Assert.assertEquals(res_ru.get("word").get("average"), "4,477");

        assert res_en != null;
        Assert.assertEquals(res_en.get("word").get("average"), "4.477");
    }
    @Test
    public void CurrencyTest() {
        for (String inputLocaleName : inputLocaleNames) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(TextStatistics.getLocaleByName(inputLocaleName));
            StringBuilder fileData = new StringBuilder();
            for (double i = 0; i <= 11; i += 0.25) {
                fileData.append(currencyFormat.format(i)).append(" text ").append(System.lineSeparator());
            }
            try {
                Files.writeString(Paths.get("./ru/ifmo/rain/teptin/i18n/tests/test_currency.txt"), fileData);
                for (String outputLocaleName : outputLocaleNames) {
                    var res = getTestMap(inputLocaleName, outputLocaleName,
                            "./ru/ifmo/rain/teptin/i18n/tests/test_currency.txt",
                            "./ru/ifmo/rain/teptin/i18n/html/test_currency.html");
                    NumberFormat outFormat = NumberFormat.getNumberInstance(TextStatistics.getLocaleByName(outputLocaleName));
                    assert res != null;
                    Assert.assertEquals(outFormat.format(5.5), res.get("money").get("average"));
                }
            } catch (IOException e) {
                fail("Cannot make currency file " + e.getMessage());
                return;
            }
        }
    }
    private static List<Date> getDatesBetween(Date startDate, Date endDate) {
        List<Date> datesInRange = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);

        Calendar endCalendar = new GregorianCalendar();
        endCalendar.setTime(endDate);
        endCalendar.add(Calendar.DATE, 1);

        while (calendar.before(endCalendar)) {
            Date result = calendar.getTime();
            datesInRange.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        return datesInRange;
    }

    @Test
    public void DateTest() {
        for (String inputLocaleName : inputLocaleNames) {
            DateFormat dateFormatRUS = DateFormat.getDateInstance(DateFormat.SHORT, TextStatistics.getLocaleByName("ru_RU"));
            DateFormat dateFormatIn = DateFormat.getDateInstance(DateFormat.SHORT, TextStatistics.getLocaleByName(inputLocaleName));
            Date startDate = dateFormatRUS.parse("28.06.2020", new ParsePosition(0));
            Date endDate = dateFormatRUS.parse("06.07.2020", new ParsePosition(0));
            Date midDate = dateFormatRUS.parse("02.07.2020", new ParsePosition(0));
            List<Date> datesBetween = getDatesBetween(startDate, endDate);
            StringBuilder fileData = new StringBuilder();
            for (Date date : datesBetween) {
                fileData.append(dateFormatIn.format(date)).append(" text ").append(System.lineSeparator());
            }
            try {
                Files.writeString(Paths.get("./ru/ifmo/rain/teptin/i18n/tests/test_date.txt"), fileData);
                for (String outputLocaleName : outputLocaleNames) {
                    var res = getTestMap(inputLocaleName, outputLocaleName,
                            "./ru/ifmo/rain/teptin/i18n/tests/test_date.txt",
                            "./ru/ifmo/rain/teptin/i18n/html/test_date.html");
                    DateFormat dateFormatOut = DateFormat.getDateInstance(DateFormat.SHORT, TextStatistics.getLocaleByName(outputLocaleName));
                    assert res != null;
                    Assert.assertEquals(dateFormatOut.format(midDate), res.get("date").get("average"));
                }
            } catch (IOException e) {
                fail("Cannot make currency file " + e.getMessage());
                return;
            }
        }
    }
}
