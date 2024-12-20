package services;

import pojos.Patient;
import pojos.User;
import utils.CsvHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PatientService {

    public static final String FILE_PATH = "data/Patient.csv";

    public static boolean validatePatient(String username, String hashPassword) {
        ensureFileExists();
        List<String[]> patients = CsvHandler.readFromCsv(FILE_PATH);

        for (String[] patient : patients) {
            if (patient.length >= 3) {
                if (patient[0].equals(username) && patient[1].equals(hashPassword)) {
                    //System.out.println("Login successful as patient.");
                    return true;
                }
            }
        }
        return false;
    }



    // Save patient in CVS file
    public static void savePatient(Patient patient) {
        List<String> patientData = List.of(
                patient.getUser().getUsername(),
                patient.getUser().getPassword(), // set hashed password
                patient.getName(),
                patient.getSurname(),
                String.valueOf(patient.getGenetic_background()));

        CsvHandler.writeToCsv(FILE_PATH, patientData);
        System.out.println("Patient saved to Patient.csv " );
    }


    // Read patients from CVS file
    public static List<Patient> readPatients() {
        ensureFileExists();
        List<Patient> patients = new ArrayList<>();
        List<String[]> rows = CsvHandler.readFromCsv(FILE_PATH);

        for (String[] row : rows) {
            if (row.length >= 3) {
                String name = row[0];
                String surname = row[1];
                Boolean geneticBackground = Boolean.parseBoolean(row[2]);
                patients.add(new Patient(name, surname, geneticBackground));
            }
        }
        return patients;
    }
    public static boolean isUsernameTaken(String username) {
        ensureFileExists();
        List<String[]> patients = CsvHandler.readFromCsv(FILE_PATH);

        for (String[] patient : patients) {
            if (patient.length>=1 && patient[0].equals(username)) {
                return true;//Username found
            }
        }
        return false;//not found
    }

    public static Patient getPatientByUsername(String username) {
        ensureFileExists();
        List<String[]> patients = CsvHandler.readFromCsv(FILE_PATH);

        for (String[] patientData : patients) {
            if (patientData[0].equals(username)) {
                String hashedPassword = patientData[1];
                String name = patientData[2];
                String surname = patientData[3];
                boolean geneticBackground = Boolean.parseBoolean(patientData[4]);
                Patient patient=new Patient(name, surname, geneticBackground, new User(username, hashedPassword));
                if (patient.getMedicalRecords() == null) {
                    patient.setMedicalRecord(new ArrayList<>()); // not null
                }
                return patient;
            }
        }

        // not found
        return null;
    }

    private static void ensureFileExists() {
        try {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent()); // Crear directorios si no existen
                Files.createFile(path); // Crear archivo vacío
                System.out.println("Patient.csv created at: " + path.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Patient.csv: " + e.getMessage());
        }
    }


}

