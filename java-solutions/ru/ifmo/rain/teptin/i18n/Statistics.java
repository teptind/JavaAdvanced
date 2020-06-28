package ru.ifmo.rain.teptin.i18n;

import ru.ifmo.rain.teptin.i18n.models.*;

import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Statistics {
    public static <T extends BaseModel> Map<String, String> getStatisticsForModel(List<T> data,
                                                                                  Locale inputLocale,
                                                                                  Locale outputLocale,
                                                                                  DataTypeInfo.DataType type) {
        final String NO_DATA = "-----";
        Map<String, String> modelStatistics = new LinkedHashMap<>() {
            {
                put("number", "0");
                put("distinct_number", "0");
                put("min_length", NO_DATA);
                put("max_length", NO_DATA);
                put("min", NO_DATA);
                put("max", NO_DATA);
                put("average", NO_DATA);
            }
        };
        if (data.isEmpty()) {
            return modelStatistics;
        }
        modelStatistics.put("number", NumberFormat.getNumberInstance(outputLocale).format(statCount(data)));
        modelStatistics.put("distinct_number", NumberFormat.getNumberInstance(outputLocale).format(statCountDistinct(data)));
        modelStatistics.put("min_length", NumberFormat.getNumberInstance(outputLocale).format(statMinLength(data, inputLocale)));
        modelStatistics.put("max_length", NumberFormat.getNumberInstance(outputLocale).format(statMaxLength(data, inputLocale)));
        if (type == DataTypeInfo.DataType.MONEY) {
            T minModel = statMin(data);
            T maxModel = statMax(data);
            NumberFormat outNF = NumberFormat.getNumberInstance(outputLocale);
            String currencyCode = Currency.getInstance(inputLocale).getCurrencyCode();
            modelStatistics.put("min",
                    String.format("%s (%s %s)", minModel.getAsString(inputLocale),
                            outNF.format(minModel.getValue()), currencyCode));
            modelStatistics.put("max",
                    String.format("%s (%s %s)", maxModel.getAsString(inputLocale),
                            outNF.format(maxModel.getValue()), currencyCode));
            modelStatistics.put("average", outNF.format(statAverage(data)));
        } else {
            modelStatistics.put("min", statMin(data).getAsString(outputLocale));
            modelStatistics.put("max", statMax(data).getAsString(outputLocale));
            if (type == DataTypeInfo.DataType.DATE) {
                modelStatistics.put("average",
                        DateFormat.getDateInstance(DateFormat.SHORT, outputLocale)
                                .format(new Date(Math.round(statAverage(data) * 1000L))));
            } else {
                modelStatistics.put("average", NumberFormat.getNumberInstance(outputLocale).format(statAverage(data)));
            }
        }
        return modelStatistics;
    }

    public static Map<String, Map<String, String>> getFullStatistics(String fileData, Locale inputLocale, Locale outputLocale) {
        Map<String, Map<String, String>> fullModelStatistics = new LinkedHashMap<>();
        for (DataTypeInfo.DataType type : DataTypeInfo.DataType.values()) {
            fullModelStatistics.put(DataTypeInfo.dataTypeNames.get(type),
                    getStatisticsForModel(obtainData(fileData, inputLocale, type), inputLocale, outputLocale, type));
        }
        return fullModelStatistics;
    }

    public static List<BaseModel> obtainData(String fileData, Locale locale, DataTypeInfo.DataType type) {
        if (DataTypeInfo.simpleIterators.containsKey(type)) {
            List<String> rawData;
            BreakIterator iterator = DataTypeInfo.simpleIterators.get(type).apply(locale);
            if (iterator == null) {
                rawData = Arrays.asList(fileData.split(System.lineSeparator()));
            } else {
                rawData = new ArrayList<>();
                iterator.setText(fileData);
                for (int left = iterator.first(), right = iterator.next(); right != BreakIterator.DONE; left = right, right = iterator.next()) {
                    rawData.add(fileData.substring(left, right));
                }
            }
            return rawData.stream()
                    .map(s -> ModelFactory.getTextModel(s, locale, type))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        BreakIterator iterator = BreakIterator.getWordInstance(locale);
        iterator.setText(fileData);
        List<BaseModel> data = new ArrayList<>();
        int breakPoint = -1;
        for (int left = iterator.first(), right = iterator.next(); left != BreakIterator.DONE; left = right, right = iterator.next()) {
            if (left < breakPoint) {
                continue;
            }
            try {  // complicated data constructions
                BaseModel currModel;
                if (type == DataTypeInfo.DataType.MONEY) {
                    currModel = new MoneyModel(fileData, left, locale);
                } else if (type == DataTypeInfo.DataType.DATE) {
                    currModel = new DateModel(fileData, left, locale);
                } else { // NUMBER
                    currModel = new NumberModel(fileData, left, locale);
                }
                data.add(currModel);
                breakPoint = left + currModel.getAsString(locale).length();
            } catch (ParseException ignored) {}
        }
        return data;
    }

    private static <T extends BaseModel> int statCount(List<T> data) {
        return data.size();
    }

    private static <T extends BaseModel> int statCountDistinct(List<T> data) {
        return (int) data.stream().distinct().count();
    }

    private static <T extends BaseModel> int statMinLength(List<T> data, Locale locale) {
        return data.stream().map(u -> u.getAsString(locale).length()).min(Integer::compareTo).orElse(0);
    }

    private static <T extends BaseModel> int statMaxLength(List<T> data, Locale locale) {
        return data.stream().map(u -> u.getAsString(locale).length()).max(Integer::compareTo).orElse(0);
    }

    private static <T extends BaseModel> T statMin(List<T> data) {
        return Collections.min(data, T::compareTo);
    }

    private static <T extends BaseModel> T statMax(List<T> data) {
        return Collections.max(data, T::compareTo);
    }

    private static<T extends BaseModel> double statAverage(List<T> data) {
        return data.stream().mapToDouble(BaseModel::getValue).sum() / data.size();
    }
}
