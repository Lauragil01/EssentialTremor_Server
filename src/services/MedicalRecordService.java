package services;

import bITalino.BitalinoDemo;
import pojos.DoctorsNote;
import pojos.MedicalRecord;
import pojos.Treatment;
import signals.ACC;
import signals.EMG;
import signals.Signals;
import utils.CsvHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MedicalRecordService {

    private static final String FILE_PATH = "data/MedicalRecord.csv";


    // Read medical Record from CSV file
    public static List<MedicalRecord> readMedicalRecords() {
        ensureFileExists();
        List<MedicalRecord> records = new ArrayList<>();
        List<String[]> rows = CsvHandler.readFromCsv(FILE_PATH);

        for (String[] row : rows) {
            if (row.length >= 6) {
                String patientName = row[0];
                String patientSurname = row[1];
                int age = Integer.parseInt(row[2]);
                double weight = Double.parseDouble(row[3]);
                int height = Integer.parseInt(row[4]);
                List<String> symptoms = Arrays.asList(row[5].split(";"));
                Boolean geneticBackground=Boolean.parseBoolean(row[6]);

                MedicalRecord record =new MedicalRecord(patientName, patientSurname, age, weight, height, symptoms,geneticBackground);
                record.setGenetic_background(geneticBackground);
                records.add(record);
            }
        }
        return records;
    }


    public static void saveMedicalRecordToCsv(MedicalRecord record) {
        ensureFileExists();
        List<String> data = Arrays.asList(
                record.getPatientName(),
                record.getPatientSurname(),
                String.valueOf(record.getAge()),
                String.valueOf(record.getWeight()),
                String.valueOf(record.getHeight()),
                String.join(";", record.getSymptoms()),
                String.valueOf(record.getGenetic_background())
        );

        CsvHandler.writeToCsv(FILE_PATH, data);
        System.out.println("Medical record saved to " + FILE_PATH);

        // Guardar señales en archivos separados
        if (record.getAcc() != null && record.getEmg() != null) {
            saveDataToFile(record.getPatientName(), record.getAcc(), record.getEmg());
        }

    }

    private static String saveDataToFile(String patientName, ACC acc, EMG emg) {
        LocalDateTime moment = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = moment.format(formatter);

        Path folderPath = Paths.get("BITalinoData");
        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            Logger.getLogger(BitalinoDemo.class.getName()).log(Level.SEVERE, "Error creating directory", e);
        }

        String fileName = "BITalinoData/" + patientName + "_" + timestamp + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Patient Name: " + patientName + "\n");
            writer.write("Date and Time: " + moment + "\n\n");

            writer.write("EMG Data:\n");
            for (int i = 0; i < emg.getSignalData().size(); i++) {
                writer.write("Time: " + emg.getTimestamp().get(i) + ", Signal: " + emg.getSignalData().get(i) + "\n");
            }

            writer.write("\nACC Data:\n");
            for (int i = 0; i < acc.getSignalData().size(); i++) {
                writer.write("Time: " + acc.getTimestamp().get(i) + ", Signal: " + acc.getSignalData().get(i) + "\n");
            }

            System.out.println("Data saved to " + fileName);
        } catch (IOException e) {
            Logger.getLogger(BitalinoDemo.class.getName()).log(Level.SEVERE, "Error writing file", e);
        }
        return fileName;
    }

    private static void ensureFileExists() {
        try {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent()); // Crear directorios si no existen
                Files.createFile(path); // Crear archivo vacío
                System.out.println("MedicalRecord.csv created at: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create MedicalRecord.csv: " + e.getMessage());
        }
    }


}

