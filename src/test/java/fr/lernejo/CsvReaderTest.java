package fr.lernejo;

import fr.lernejo.file.CsvReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

public class CsvReaderTest {

    private Path tempFile;

    // Set up the temporary CSV file before each test
    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("test", ".csv");
        String csvContent = """
            timestamp,temperature_2m,humidity,pressure_msl,precipitation,wind_speed_10m,cloud_cover,direct_normal_irradiance_instant,is_day
            2023-01-01T12:00,12.5,80,1013.2,0.0,15.6,60,500,1
            2023-01-01T00:00,7.1,85,1009.8,0.1,8.4,100,0,0
            2023-01-02T12:00,10.8,75,1015.3,0.0,10.1,50,300,1
            2023-01-02T00:00,5.4,90,1010.2,0.0,5.8,100,0,0
            """;
        Files.write(tempFile, csvContent.getBytes());
    }

    // Clean up the temporary file after each test
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testValidCsvWithSum() {
        String[] args = {tempFile.toString(), "2023-01-01", "2023-01-02", "temperature_2m", "DAY", "SUM"};
        int exitCode = CsvReader.process(args);
        assertEquals(0, exitCode);
        // You can also assert the printed output here, if needed
    }

    @Test
    void testInvalidArguments() {
        String[] args = {};
        int exitCode = CsvReader.process(args);
        assertEquals(1, exitCode);
    }

    @Test
    void testInvalidMetric() {
        String[] args = {tempFile.toString(), "2023-01-01", "2023-01-31", "invalid_metric", "DAY", "SUM"};
        int exitCode = CsvReader.process(args);
        assertEquals(2, exitCode);
    }

    @Test
    void testInvalidAggregationType() {
        String[] args = {tempFile.toString(), "2023-01-01", "2023-01-31", "temperature_2m", "DAY", "INVALID"};
        int exitCode = CsvReader.process(args);
        assertEquals(4, exitCode);
    }

    @Test
    void testFileNotFound() {
        String[] args = {"nonexistent.csv", "2023-01-01", "2023-01-31", "temperature_2m", "DAY", "SUM"};
        int exitCode = CsvReader.process(args);
        assertEquals(5, exitCode);
    }

    // Additional test for the "AVG" aggregation
    @Test
    void testValidCsvWithAverage() {
        String[] args = {tempFile.toString(), "2023-01-01", "2023-01-02", "temperature_2m", "DAY", "AVG"};
        int exitCode = CsvReader.process(args);
        assertEquals(0, exitCode);
    }
}

