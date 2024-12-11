package services;

import pojos.DoctorsNote;
import pojos.MedicalRecord;
import pojos.Treatment;
import signals.ACC;
import signals.EMG;
import signals.Signals;
import utils.CsvHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MedicalRecordService {

    private static final String FILE_PATH = "C:\\Users\\Ana\\IdeaProjects\\EssentialTremor_Server\\data\\MedicalRecord.csv";
    //private static final String ACC_FILE = "C:\\Users\\Laura Gil\\Desktop\\Uni\\Telemedicina\\EssentialTremor_Server2\\data\\acc_signals.csv";
    //private static final String EMG_FILE = "C:\\Users\\Laura Gil\\Desktop\\Uni\\Telemedicina\\EssentialTremor_Server2\\data\\emg_signals.csv";



    // Read medical Record from CSV file
    public static List<MedicalRecord> readMedicalRecords() {
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
        if (record.getAcc() != null) {
            saveACCToCsv(record.getAcc(), record.getAcc().getPath());
        }
        if (record.getEmg() != null) {
            saveEMGToCsv(record.getEmg(), record.getEmg().getPath());
        }
    }

    private static void saveACCToCsv(ACC acc, String filePath) {
        //ensureCsvHeaders(filePath, "Signal_Data,Filename,Path,Timestamp");

        List<String> lines = new ArrayList<>();
        List<Integer> signalData = acc.getSignalData();
        List<Integer> timestamps = acc.getTimestamp();

        for (int i = 0; i < signalData.size(); i++) {
            String line = String.format(
                    "%d,%s,%s,%d",
                    signalData.get(i),
                    acc.getFilename(),
                    acc.getPath(),
                    timestamps.get(i)
            );
            lines.add(line);
        }

        try {
            Files.write(Paths.get(filePath), lines, StandardOpenOption.APPEND);
            System.out.println(acc.getFilename() + " data saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing acc data to CSV file: " + e.getMessage());
        }
    }

    private static void saveEMGToCsv(EMG emg, String filePath) {
        //ensureCsvHeaders(filePath, "Signal_Data,Filename,Path,Timestamp");

        List<String> lines = new ArrayList<>();
        List<Integer> signalData = emg.getSignalData();
        List<Integer> timestamps = emg.getTimestamp();

        for (int i = 0; i < signalData.size(); i++) {
            String line = String.format(
                    "%d,%s,%s,%d",
                    signalData.get(i),
                    emg.getFilename(),
                    emg.getPath(),
                    timestamps.get(i)
            );
            lines.add(line);
        }

        try {
            Files.write(Paths.get(filePath), lines, StandardOpenOption.APPEND);
            System.out.println(emg.getFilename() + " data saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing acc data to CSV file: " + e.getMessage());
        }
    }

    private static List<Integer> parseIntegerList(String data) {
        return Arrays.stream(data.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }



    /*private static void ensureCsvHeaders(String filePath, String headers) {
        File file = new File(filePath);
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write(headers);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("Error writing headers to CSV file: " + e.getMessage());
            }
        }
    }*/



}

