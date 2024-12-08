package services;

import pojos.Patient;
import utils.CsvHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PatientService {

    public static final String FILE_PATH = "C:\\Users\\Laura Gil\\Desktop\\Uni\\Telemedicina\\EssentialTremor_Server2\\data\\Patient.csv";

    public static boolean validatePatient(String username, String hashPassword) {
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
        System.out.println("Patient saved to Patient.csv: " );
    }


    // Read patients from CVS file
    public static List<Patient> readPatients() {
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
        List<String[]> patients = CsvHandler.readFromCsv(FILE_PATH);

        for (String[] patient : patients) {
            if (patient.length>=1 && patient[0].equals(username)) {
                return true;//Username found
            }
        }
        return false;//not found
    }

}

