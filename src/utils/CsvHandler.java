package utils;
import java.io.*;
import java.util.*;

public class CsvHandler {

    // Write data in CSV file
    public static void writeToCsv(String filePath, List<String> data) {
        try (FileWriter fw = new FileWriter(filePath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            // Convertir la lista en una línea separada por comas
            String row = String.join(",", data);
            out.println(row);

        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }


    // Read data from CSV file
    public static List<String[]> readFromCsv(String filePath) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
                rows.add(values);
            }
        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
        }
        return rows;
    }
}
