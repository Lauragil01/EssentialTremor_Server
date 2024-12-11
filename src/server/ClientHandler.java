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
        private static int connectedClients = 0; // Contador de clientes conectados
        private final Socket clientSocket;
        private  BufferedReader bufferedReader;
        private  PrintWriter printWriter;
        private InputStream inputStream;
        private final Doctor doctor;

        private static final String ACC_FILE = "C:\\Users\\Laura Gil\\Desktop\\Uni\\Telemedicina\\EssentialTremor_Server2\\data\\acc_signals.csv";
        private static final String EMG_FILE = "C:\\Users\\Laura Gil\\Desktop\\Uni\\Telemedicina\\EssentialTremor_Server2\\data\\emg_signals.csv";


    public ClientHandler(Socket clientSocket, Doctor doctor) {
            this.clientSocket = clientSocket;
            this.doctor=doctor;

            // Incrementar contador al aceptar la conexión
            incrementConnectedClients();
            System.out.println("Client connected. Total clients: " + getConnectedClients());

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

                    if (clientRequest.equalsIgnoreCase("DISCONNECT")) {
                        System.out.println("Client requested to disconnect.");
                        break; // Salir del bucle para cerrar la conexión
                    }
                    if (clientRequest.startsWith("REGISTER_PATIENT|")) {
                        processPatientData(clientRequest, printWriter);
                    } else if (clientRequest.startsWith("LOGIN|")) {
                        processLoginPatient(clientRequest, printWriter);
                    } else if (clientRequest.startsWith("MEDICAL_RECORD|")) {
                        // Processing MR
                        String medicalRecordData = clientRequest.replaceFirst("MEDICAL_RECORD\\|", "");
                        processMedicalRecord(medicalRecordData);
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


    private void incrementConnectedClients() {
        connectedClients++;
    }

    private void decrementConnectedClients() {
        connectedClients--;
    }

    public static int getConnectedClients() {
        return connectedClients;
    }

    private void releaseResources() {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (printWriter != null) printWriter.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();

            // Decrementar contador al liberar recursos
            decrementConnectedClients();
            System.out.println("Client disconnected. Total clients: " + getConnectedClients());

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
            // convert to object
            MedicalRecord medicalRecord = deserializeMedicalRecord(medicalRecordData);

            if (medicalRecord == null) {
                printWriter.println("ERROR|Invalid medical record data.");
                return;
            }

            // Process medical Record adding doctor
            Doctor doctor = new Doctor();
            DoctorsNote note = doctor.generateDoctorsNote(medicalRecord);
            Treatment treatment = doctor.prescribeTreatment(medicalRecord);

            // data Medical Record to csv including signals ACC and emg
            MedicalRecordService.saveMedicalRecordToCsv(medicalRecord);

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
            String[] fields = data.split(",");

            if (fields.length < 9) { // enoughf fields
                return null;
            }

            // Patient data in order
            String patientName = fields[0];
            String patientSurname = fields[1];
            int age = Integer.parseInt(fields[2]);
            double weight = Double.parseDouble(fields[3]);
            int height = Integer.parseInt(fields[4]);

            // Parse Symptoms
            List<String> symptoms = Arrays.asList(fields[5].split(";"));

            // Parse Genetic Background
            boolean geneticBackground = Boolean.parseBoolean(fields[6]);

            // Parse ACC Data
            List<Integer> accTimestamps = parseIntegerList(fields[7]);
            List<Integer> accSignalData = parseIntegerList(fields[8]);
            ACC acc = new ACC(accSignalData, "BITalino_ACC_123.txt",ACC_FILE , accTimestamps);
            // Parse EMG Data
            List<Integer> emgSignalData = parseIntegerList(fields[9]);
            EMG emg = new EMG(emgSignalData, "BITalino_EMG_123.txt", EMG_FILE, accTimestamps);


            MedicalRecord record = new MedicalRecord(patientName, patientSurname, age, weight, height, symptoms, geneticBackground);

            record.setAcceleration(acc);
            record.setEmg(emg);

            return record;
        } catch (Exception e) {
            System.err.println("Error deserializing medical record: " + e.getMessage());
            return null;
        }
    }

    private static List<Integer> parseIntegerList(String data) {
        return Arrays.stream(data.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }



    private String serializeDoctorsResponse(DoctorsNote note, Treatment treatment) {
        String notes = note != null ? note.getNotes() : "No notes provided";
        String treatmentDescription = treatment != null ? treatment.getDescription() : "No treatment prescribed";
        return notes + "|" + treatmentDescription;
    }










}
