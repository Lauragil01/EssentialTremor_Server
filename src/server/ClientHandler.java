package server;

import pojos.*;
import services.MedicalRecordService;
import services.PatientService;
import signals.ACC;
import signals.EMG;
import utils.PasswordHash;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class ClientHandler implements Runnable{
        private final Socket clientSocket;
        private  BufferedReader bufferedReader;
        private  PrintWriter printWriter;
        private InputStream inputStream;
        private final Doctor doctor;

        public ClientHandler(Socket clientSocket, Doctor doctor) {
            this.clientSocket = clientSocket;
            this.doctor=doctor;
        }

        @Override
        public void run() {
            try {

                inputStream=clientSocket.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                printWriter = new PrintWriter(clientSocket.getOutputStream(), true);

                String clientRequest;
                while ((clientRequest = bufferedReader.readLine()) != null) {
                    System.out.println("Received request: " + clientRequest);

                    if (clientRequest.startsWith("REGISTER_PATIENT|")) {
                        processPatientData(clientRequest, printWriter);
                    } else if (clientRequest.startsWith("LOGIN|")) {
                        processLoginPatient(clientRequest, printWriter);
                    } else {
                        printWriter.println("ERROR|Unknown request type.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                releaseResources();
            }
        }
    private void releaseResources() {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (printWriter != null) printWriter.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, "Error releasing resources", e);
        }
    }


    //log in patient--> processing data sent
    private void processLoginPatient(String loginData, PrintWriter writer) {
        String[] parts = loginData.split("\\|");

        //for(String p:parts){
           // System.out.println(p+ " ");
        //}

        if (parts.length != 3) {
            writer.println("ERROR|Invalid login format. Use 'username|password'");
            return;
        }

        String username = parts[1];
        String password = parts[2];

        try {
            if (!PatientService.isUsernameTaken(username)) {
                writer.println("ERROR|Username does not exist. Please register first.");
                return;
            }

            String hashedPassword = PasswordHash.hashPassword(password);
            Patient patient = PatientService.getPatientByUsername(username);

            if (patient == null) {
                writer.println("ERROR|Patient not found in fields.");
                return;
            }
            if (PatientService.validatePatient(username, hashedPassword)) {
                String response = String.format("SUCCESS|%s|%s|%s|%s|%s",
                        patient.getName(),
                        patient.getSurname(),
                        patient.getGenetic_background(),
                        patient.getUser().getUsername(),
                        patient.getUser().getPassword());
                writer.println(response);
                writer.println("SUCCESS|Login successful. Welcome, " + username + "!");

            } else {
                writer.println("ERROR|Invalid password. Please try again.");
            }
        } catch (Exception e) {
            writer.println("ERROR|An unexpected error occurred: " + e.getMessage());
            System.out.println("Error during patient login: " + e.getMessage());
        }
    }
    private String serializePatient(Patient patient) {
        return patient.getUser().getUsername() + "|" +
                patient.getName() + "|" +
                patient.getSurname() + "|" +
                patient.getGenetic_background();
    }

    //reception of data of the patient's REGISTRATION and write them in csvFile
    public void processPatientData(String patientData, PrintWriter writer) {
        // Format data of the client:
        // "REGISTER_PATIENT|username|password|name|surname|geneticBackground"
        String[] data = patientData.split("\\|");

        if (data.length == 6 && "REGISTER_PATIENT".equals(data[0])) {
            String username = data[1];
            String password = data[2]; // Password without hash
            String name = data[3];
            String surname = data[4];
            boolean geneticBackground = Boolean.parseBoolean(data[5]);

            try {
                // Unique user
                if (PatientService.isUsernameTaken(username)) {
                    writer.println("Error: Username already exists.");
                    return;
                }
                // Hash password
                String hashedPassword = PasswordHash.hashPassword(password);
                User user=new User(username, hashedPassword);
                // Patient object
                Patient patient = new Patient();
                patient.setUser(user);
                patient.setName(name);
                patient.setSurname(surname);
                patient.setGenetic_background(geneticBackground);
                // save patient in CSV
                PatientService.savePatient(patient);
                writer.println("SUCCESS| Patient registered successfully: " + patient.getUser());

            } catch (Exception e) {
                writer.println("ERROR|Failed to register patient:  " + e.getMessage());
                System.out.println("Error processing patient data: "+e.getMessage());
            }
        } else {
            writer.println("ERROR|Incorrect data format or missing fields.");
        }
    }

    private void processMedicalRecord(String medicalRecordData) {
        try {
            // Deserializar el registro médico
            MedicalRecord medicalRecord = deserializeMedicalRecord(medicalRecordData);

            if (medicalRecord == null) {
                printWriter.println("ERROR|Invalid medical record data.");
                return;
            }

            // Procesar el registro médico con el Doctor
            Doctor doctor = new Doctor();
            DoctorsNote note = doctor.generateDoctorsNote(medicalRecord);
            Treatment treatment = doctor.prescribeTreatment(medicalRecord);

            // Guardar los datos en el archivo CSV
            MedicalRecordService.saveMedicalRecord(medicalRecord, note, treatment);

            // Responder al cliente con las notas y tratamiento
            String response = serializeDoctorsResponse(note, treatment);
            printWriter.println("SUCCESS|" + response);
        } catch (Exception e) {
            printWriter.println("ERROR|An error occurred while processing the medical record.");
            System.err.println("Error processing medical record: " + e.getMessage());
        }
    }

    private MedicalRecord deserializeMedicalRecord(String data) {
        try {
            String[] fields = data.split("\\|");

            if (fields.length < 9) {
                return null; // Validar que haya suficientes campos
            }

            // Datos básicos del paciente
            String patientName = fields[0];
            String patientSurname = fields[1];
            int age = Integer.parseInt(fields[2]);
            double weight = Double.parseDouble(fields[3]);
            int height = Integer.parseInt(fields[4]);
            List<String> symptoms = Arrays.asList(fields[5].split(","));

            // Crear ACC y EMG usando el constructor parcial
            List<Integer> accSignalData = parseIntegerList(fields[6]);
            List<Integer> accTimestamps = parseIntegerList(fields[7]); // Asumimos timestamps separados
            ACC acc = new ACC(accSignalData, accTimestamps);

            List<Integer> emgSignalData = parseIntegerList(fields[8]);
            List<Integer> emgTimestamps = parseIntegerList(fields[9]); // Asumimos timestamps separados
            EMG emg = new EMG(emgSignalData, emgTimestamps);

            // Información genética
            boolean geneticBackground = Boolean.parseBoolean(fields[10]);

            // Crear el objeto MedicalRecord
            return new MedicalRecord(age, weight, height, symptoms, acc, emg, geneticBackground, patientName, patientSurname);
        } catch (Exception e) {
            System.err.println("Error deserializing medical record: " + e.getMessage());
            return null;
        }
    }

    private List<Integer> parseIntegerList(String data) {
        return Arrays.stream(data.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }


    private String serializeDoctorsResponse(DoctorsNote note, Treatment treatment) {
        return "Doctors Note: " + note.getNotes() + ", Treatment: " + treatment.getDescription();
    }






}
