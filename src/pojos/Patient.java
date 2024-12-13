package pojos;

import java.io.*;
import java.util.*;


public class Patient {
    private String name;
    private String surname;
    private Boolean genetic_background;

    private User user;
    private List<MedicalRecord> medicalRecords;

    public Patient() {
    }

    public Patient(String name, String surname, Boolean genBack) {
        this.name = name;
        this.surname = surname;
        this.genetic_background = genBack;
        this.medicalRecords = new ArrayList<MedicalRecord>();
    }
    public Patient (String name, String surname, boolean geneticBackground,  User user){
        this.name=name;
        this.surname= surname;
        this.genetic_background=geneticBackground;
        this.user=user;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public Boolean getGenetic_background() {
        return genetic_background;
    }

    public void setGenetic_background(Boolean genetic_background) {
        this.genetic_background = genetic_background;
    }

    public List<MedicalRecord> getMedicalRecords() {
        if (medicalRecords == null) {
            medicalRecords = new ArrayList<>(); // Asegura que no sea null
        }
        return medicalRecords;
    }

    public void setMedicalRecord(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords;
    }


    private void seeDoctorNotes(MedicalRecord medicalRecord) {
        List<DoctorsNote> notes=medicalRecord.getDoctorsNotes();

        if(notes.isEmpty()){
            System.out.println("There is not doctor notes available for this Medical Record.");
            return;
        }
        System.out.println("Doctor notes in this medical record");
        for(int i=0; i<notes.size();i++){
            System.out.println((i + 1)+ ": "+notes.get(i).getNotes());
        }
    }

    public String toString(){
        return "Patient --> " +
                "Name: "+ this.name + " " +
                "Surname: "+ this.surname+ " "+
                "Genetic background: "+ this.genetic_background+
                " ";
    }






    /*public static void main(String[] args) throws IOException {
        Patient p = new Patient("a", "a", Boolean.TRUE);
        p.openRecord();
        /*for (int i=0; i<p.getMedicalRecords().size();i++){
            System.out.println(p.getMedicalRecords().get(i));
        }
        MedicalRecord mr = p.chooseMR();
        p.sendMedicalRecord(mr);
    }*/


    /*public static void main(String[] args) {
        // Start a mock server in a new thread
        new Thread(() -> startMockServer()).start();

        // Delay waiting to the Server
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Test the sendMedicalRecord method
        try (Socket socket = new Socket("localhost", 9000)) {
            // Create  MedicalRecord data
            MedicalRecord record = new MedicalRecord(
                    "Ana", "Losada", 23, 60, 170,
                    Arrays.asList("Tremor", "Fatigue"),
                    new ACC(Arrays.asList(1, 2, 3), Arrays.asList(10, 20, 30)),
                    new EMG(Arrays.asList(4, 5, 6), Arrays.asList(10, 20, 30)),
                    true
            );

            //Patient instance and test the method
            Patient patient = new Patient("Ana", "Losada", true);
            patient.sendMedicalRecord(record, socket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Mock server to test the function
    private static void startMockServer() {
        try (ServerSocket serverSocket = new ServerSocket(9000)) {
            System.out.println("Mock server started, waiting for connection...");
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                System.out.println("Client connected. Receiving data...");
                String receivedData;
                while ((receivedData = reader.readLine()) != null) {
                    System.out.println("Received: " + receivedData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }*/
}
