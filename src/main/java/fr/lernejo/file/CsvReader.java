package fr.lernejo.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

    public static void main(String[] args) {
        if (args.length != 5) {
            if (args.length == 0) {
                System.out.println("Missing argument");
                System.exit(3);
            } else {
                System.out.println("Too many arguments");
                System.exit(4);
            }
        }

        String filePath = args[0];
        String startDateStr = args[1];
        String endDateStr = args[2];
        String metric = args[3];
        String aggregation = args[4];

        try {
            LocalDate startDate = LocalDate.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            double result = processCsv(filePath, startDate, endDate, metric, aggregation);
            String unit = getUnit(metric);
            System.out.println(formatResult(result, unit));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    private static double processCsv(String filePath, LocalDate startDate, LocalDate endDate, String metric, String aggregation) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip the first 3 lines (headers)
            br.readLine();
            br.readLine();
            br.readLine();
            String header = br.readLine();
            String[] headers = header.split(",");

            int metricIndex = getMetricIndex(headers, metric);
            double aggregationValue = 0;
            List<Double> values = new ArrayList<>();
            String line;

            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                LocalDate date = LocalDate.parse(fields[0].substring(0, 10));
                double value = Double.parseDouble(fields[metricIndex]);

                if (date.isEqual(startDate) || date.isAfter(startDate) && date.isBefore(endDate)) {
                    values.add(value);
                }
            }

            return calculateAggregation(values, aggregation);
        }
    }

    private static int getMetricIndex(String[] headers, String metric) {
        switch (metric) {
            case "temperature_2m":
                return 1;
            case "pressure_msl":
                return 3;
            case "wind_speed_10m":
                return 5;
            case "direct_normal_irradiance_instant":
                return 8;
            default:
                throw new IllegalArgumentException("Invalid metric: " + metric);
        }
    }

    private static double calculateAggregation(List<Double> values, String aggregation) {
        switch (aggregation) {
            case "SUM":
                return values.stream().mapToDouble(Double::doubleValue).sum();
            case "AVG":
                return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            case "MIN":
                return values.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            case "MAX":
                return values.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            default:
                throw new IllegalArgumentException("Invalid aggregation: " + aggregation);
        }
    }

    private static String getUnit(String metric) {
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
                throw new IllegalArgumentException("Invalid metric: " + metric);
        }
    }

    private static String formatResult(double value, String unit) {
        DecimalFormat df = new DecimalFormat("0.#####"); // Adjust the format as needed
        if (value == 0) {
            return "0.0 " + unit; // Handle special case for zero
        }
        // Determine the format: scientific or decimal
        if (Math.abs(value) >= 1e7 || Math.abs(value) < 1) {
            return String.format("%.8E %s", value, unit); // Scientific notation
        } else {
            return df.format(value) + " " + unit; // Decimal format
        }
    }
}
