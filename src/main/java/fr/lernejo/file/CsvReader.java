package fr.lernejo.file;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CsvReader {

    public static void main(String[] args) {
        if (args.length != 6) {
            System.out.println("Usage: <path-to-csv> <start-date> <end-date> <metric> <NIGHT/DAY> <SUM/AVG/MIN/MAX>");
            System.exit(1);
        }

        String csvFilePath = args[0];
        String startDateStr = args[1];
        String endDateStr = args[2];
        String metric = args[3];
        String dayNightSelector = args[4];
        String aggregationType = args[5];

        try {
            Path path = Paths.get(csvFilePath);
            List<String> lines = Files.readAllLines(path);

            LocalDateTime startDate = LocalDateTime.parse(startDateStr + "T00:00");
            LocalDateTime endDate = LocalDateTime.parse(endDateStr + "T00:00");

            Map<String, Integer> metricColumnMap = Map.of(
                "temperature_2m", 1,
                "pressure_msl", 3,
                "wind_speed_10m", 5,
                "direct_normal_irradiance_instant", 8
            );

            if (!metricColumnMap.containsKey(metric)) {
                System.out.println("Invalid metric");
                System.exit(2);
            }

            int metricColumnIndex = metricColumnMap.get(metric);
            boolean dayFilter = dayNightSelector.equalsIgnoreCase("DAY");

            List<Double> values = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

            for (int i = 4; i < lines.size(); i++) {
                String[] columns = lines.get(i).split(",");
                LocalDateTime timestamp = LocalDateTime.parse(columns[0], formatter);
                
                if (timestamp.isBefore(startDate) || !timestamp.isBefore(endDate)) {
                    continue;
                }

                int isDay = Integer.parseInt(columns[7]);
                if ((dayFilter && isDay == 0) || (!dayFilter && isDay == 1)) {
                    continue;
                }

                double value = Double.parseDouble(columns[metricColumnIndex]);
                values.add(value);
            }

            if (values.isEmpty()) {
                System.out.println("No data for the specified criteria");
                System.exit(3);
            }

            double result;
            switch (aggregationType.toUpperCase()) {
                case "SUM":
                    result = values.stream().mapToDouble(Double::doubleValue).sum();
                    break;
                case "AVG":
                    result = values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
                    break;
                case "MIN":
                    result = values.stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN);
                    break;
                case "MAX":
                    result = values.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
                    break;
                default:
                    System.out.println("Invalid aggregation type");
                    System.exit(4);
                    return;
            }

            String unit = getUnitForMetric(metric);
            System.out.printf("%.2f %s%n", result, unit);

        } catch (IOException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
            System.exit(5);
        }
    }

    private static String getUnitForMetric(String metric) {
        switch (metric) {
            case "temperature_2m":
                return "°C";
            case "pressure_msl":
                return "hPa";
            case "wind_speed_10m":
                return "km/h";
            case "direct_normal_irradiance_instant":
                return "W/m²";
            default:
                return "";
        }
    }
}
