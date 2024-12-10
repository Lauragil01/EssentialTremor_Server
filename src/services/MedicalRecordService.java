package services;

import pojos.DoctorsNote;
import pojos.MedicalRecord;
import pojos.Treatment;
import utils.CsvHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MedicalRecordService {

    private static final String FILE_PATH = "C:\\Users\\Laura Gil\\Desktop\\Uni\\Telemedicina\\EssentialTremor_Server2\\data\\MedicalRecord.csv";

    // Save medical record in CSV file
   /* public static void saveMedicalRecord(MedicalRecord record) {
        CsvHandler.writeToCsv(FILE_PATH, Arrays.asList(
                record.getPatientName(),
                record.getPatientSurname(),
                String.valueOf(record.getAge()),
                String.valueOf(record.getWeight()),
                String.valueOf(record.getHeight()),
                String.valueOf(record.getGenetic_background()),
                String.join(";", record.getSymptoms()))); // Unir síntomas en un solo String
    }*/

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
                Boolean geneticBackground=Boolean.parseBoolean(row[5]);
                List<String> symptoms = Arrays.asList(row[6].split(";"));

                MedicalRecord record =new MedicalRecord(patientName, patientSurname, age, weight, height,geneticBackground, symptoms);
                record.setGenetic_background(geneticBackground);
                records.add(record);
            }
        }
        return records;
    }

    public static void saveMedicalRecord(MedicalRecord record, DoctorsNote note, Treatment treatment) {
        List<String> data = Arrays.asList(
                record.getPatientName(),
                record.getPatientSurname(),
                String.valueOf(record.getAge()),
                String.valueOf(record.getWeight()),
                String.valueOf(record.getHeight()),
                String.join(",", record.getSymptoms()),
                /*record.getAcc().toString(),
                record.getEmg().toString(),
                note.getNotes(),
                treatment.getDescription()*/
                record.getAcc() != null ? record.getAcc().getFilename() : "N/A",
                record.getEmg() != null ? record.getEmg().getFilename() : "N/A",
                note != null ? note.getNotes() : "No notes",
                treatment != null ? treatment.getDescription() : "No treatment"
        );

        CsvHandler.writeToCsv(FILE_PATH, data);
    }


}

