package services;

import pojos.Doctor;
import utils.CsvHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DoctorService {

    //TODO: revisar ruta
    private static final String FILE_PATH = "C:/Users/Laura Gil\\Desktop\\Uni\\Telemedicina\\EssentialTremor_Server2\\data\\Doctor.csv";


    public static void registerDoctor(String username, String hashPassword){
        CsvHandler.writeToCsv(FILE_PATH, List.of(username, hashPassword));
        System.out.println("Doctor register successfully");
    }

    //verification log in
    public static boolean validateDoctor(String username, String hashPassword) {
        List<String[]> rows = CsvHandler.readFromCsv(FILE_PATH);

        for (String[] row : rows) {
            if (row.length >= 2 && row[0].equals(username) && row[1].equals(hashPassword)) {
                System.out.println("Login successful as doctor.");
                return true;
            }
        }
        return false;
    }

    // Save doctor in CSV file
    public static void saveDoctor(Doctor doctor) {
        CsvHandler.writeToCsv(FILE_PATH, Arrays.asList(
                doctor.getName(),
                doctor.getSurname()));
    }

    // Read doctors from CSV file
    public static List<Doctor> readDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        List<String[]> rows = CsvHandler.readFromCsv(FILE_PATH);

        for (String[] row : rows) {
            if (row.length >= 2) {
                String name = row[0];
                String surname = row[1];
                doctors.add(new Doctor(name, surname));
            }
        }
        return doctors;
    }
}
