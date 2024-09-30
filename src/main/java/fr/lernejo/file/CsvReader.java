package fr.lernejo.file;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvFilePath))) {
            // Parse start and end dates
            LocalDateTime startDate = LocalDateTime.parse(startDateStr + "T00:00");
            LocalDateTime endDate = LocalDateTime.parse(endDateStr + "T00:00");

            // Map of metric to the corresponding CSV column
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

            BigDecimal sum = BigDecimal.ZERO;
            BigDecimal min = BigDecimal.valueOf(Double.MAX_VALUE);
            BigDecimal max = BigDecimal.valueOf(Double.MIN_VALUE);
            int count = 0;

            String line;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

            // Skip the first 4 lines (preamble + headers)
            for (int i = 0; i < 4; i++) {
                reader.readLine();
            }

            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                LocalDateTime timestamp = LocalDateTime.parse(columns[0], formatter);

                // Filter by date range and day/night
                if (timestamp.isBefore(startDate) || !timestamp.isBefore(endDate)) {
                    continue;
                }

                int isDay = Integer.parseInt(columns[7]);
                if ((dayFilter && isDay == 0) || (!dayFilter && isDay == 1)) {
                    continue;
                }

                // Collect the metric value
                BigDecimal value = new BigDecimal(columns[metricColumnIndex]);

                // Perform aggregation incrementally to save memory
                sum = sum.add(value);
                if (value.compareTo(min) < 0) {
                    min = value;
                }
                if (value.compareTo(max) > 0) {
                    max = value;
                }
                count++;
            }

            // Calculate the final result based on the aggregation type
            BigDecimal result = BigDecimal.ZERO;
            switch (aggregationType.toUpperCase()) {
                case "SUM":
                    result = sum;
                    break;
                case "AVG":
                    if (count == 0) {
                        result = BigDecimal.ZERO;
                    } else {
                        result = sum.divide(BigDecimal.valueOf(count), 15, RoundingMode.HALF_UP);
                    }
                    break;
                case "MIN":
                    result = min.compareTo(BigDecimal.valueOf(Double.MAX_VALUE)) == 0 ? BigDecimal.ZERO : min;
                    break;
                case "MAX":
                    result = max.compareTo(BigDecimal.valueOf(Double.MIN_VALUE)) == 0 ? BigDecimal.ZERO : max;
                    break;
                default:
                    System.out.println("Invalid aggregation type");
                    System.exit(4);
                    return;
            }

            // Format the result appropriately
            String formattedResult;
            if (aggregationType.equalsIgnoreCase("AVG")) {
                // For AVG, use more precision
                formattedResult = result.toPlainString();
            } else if (result.compareTo(BigDecimal.valueOf(1e7)) >= 0) {
                // For SUM/MIN/MAX with scientific notation
                formattedResult = String.format("%.7E", result).replace("E+0", "E");
            } else {
                // Standard decimal format without unnecessary zeros
                formattedResult = result.stripTrailingZeros().toPlainString();
            }

            // Display result with unit
            String unit = getUnitForMetric(metric);
            System.out.printf("%s %s%n", formattedResult, unit);

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
