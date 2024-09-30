package fr.lernejo.file;

import java.io.*;
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

            double sum = 0.0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
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
                double value = Double.parseDouble(columns[metricColumnIndex]);

                // Perform aggregation incrementally to save memory
                sum += value;
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
                count++;
            }

            // Calculate the final result based on the aggregation type
            double result = 0.0;
            switch (aggregationType.toUpperCase()) {
                case "SUM":
                    result = sum;
                    break;
                case "AVG":
                    result = count == 0 ? 0 : sum / count;
                    break;
                case "MIN":
                    result = min == Double.MAX_VALUE ? 0 : min;
                    break;
                case "MAX":
                    result = max == Double.MIN_VALUE ? 0 : max;
                    break;
                default:
                    System.out.println("Invalid aggregation type");
                    System.exit(4);
                    return;
            }

            // Format the result appropriately
            String formattedResult;
            if (aggregationType.equalsIgnoreCase("AVG")) {
                // For AVG, use the required precision
                formattedResult = String.format("%.15f", result);
            } else if (result >= 1e7) {
                // For SUM/MIN/MAX with scientific notation
                formattedResult = String.format("%.7E", result).replace("E+0", "E").replace("E+0", "E");
            } else {
                // Standard decimal format without unnecessary zeros
                formattedResult = String.format("%.1f", result).replaceAll("0*$", "").replaceAll("\\.$", "");
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
