package ru.ifmo.rain.teptin.i18n;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.*;
import java.util.*;

public class TextStatistics {
    public static void main(String[] args) {
        final String USG = "TextStatistics <input locale> <output locale> <input file> <output file>";
        if (args == null || Arrays.asList(args).contains(null) || args.length != 4) {
            System.err.println(USG);
        } else {
            try {
                collectStatisticsToHTML(args[0], args[1], args[2], args[3]);
            } catch (IllegalArgumentException | IOException e){
                System.err.println("An error occurred. Message: " + e.getMessage());
            }
        }
    }

    public static Locale getLocaleByName(String localeName) throws IllegalArgumentException {
        return Arrays.stream(Locale.getAvailableLocales())
                .filter(l -> l.toString().equals(localeName))
                .findAny()
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("The locale \"%s\" is unsupported", localeName)));
    }

    public static Map<String, Map<String, String>> collectStatisticsToHTML(String inputLocaleName,
                                                String outputLocaleName,
                                                String inputFileName,
                                                String outputFileName) throws IOException {
        Locale inputLocale = getLocaleByName(inputLocaleName);
        Locale outputLocale = getLocaleByName(outputLocaleName);

        try {
            String fileData = Files.readString(Paths.get(inputFileName));
            Map<String, Map<String, String>> fullStat = Statistics.getFullStatistics(fileData, inputLocale, outputLocale);

            List<String> htmlArgs = new ArrayList<>();

            ResourceBundle bundle = PropertyResourceBundle.getBundle("ru.ifmo.rain.teptin.i18n.bundle.text_statistics", outputLocale);
            htmlArgs.add("File Statistics");
            htmlArgs.add(String.format("%s ", bundle.getString("summary_statistics")));
            for (String dataTypeName : fullStat.keySet()) {
                htmlArgs.add(String.format("%s ", bundle.getString(String.format("%s_statistics", dataTypeName))));
                for (String statType : fullStat.get(dataTypeName).keySet()) {
                    htmlArgs.add(String.format("%s %s",
                            bundle.getString(String.format("%s_%s", dataTypeName, statType)),
                            fullStat.get(dataTypeName).get(statType)));
//                    System.out.println(String.format("%s_%s %s", dataTypeName, statType, fullStat.get(dataTypeName).get(statType)));
                }
            }
            htmlArgs.add(String.format("%s %s", bundle.getString("analyzedFile"), Paths.get(inputFileName).getFileName()));
            generateHTML(htmlArgs, outputFileName);
            return fullStat;
//            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("ru", "RU"));
//            System.out.println(numberFormat.format(5));
//            Number number = numberFormat.parse("5,00 ₽ pizza", new ParsePosition(0));
        } catch (IOException e) {
            throw new IOException("An error has occurred while reading or writing " + e.getMessage());
        }
    }

    private static void generateHTML(List<String> htmlArgs, String outputFileName) throws IOException {
        String patternString = Files.readString(Paths.get("./ru/ifmo/rain/teptin/i18n/html/template.html"));
        Files.writeString(Paths.get(outputFileName), MessageFormat.format(patternString, htmlArgs.toArray()));
    }
}
