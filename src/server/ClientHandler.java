package server;

import pojos.Doctor;
import pojos.Patient;
import pojos.User;
import services.PatientService;
import utils.PasswordHash;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientHandler implements Runnable{
        private final Socket clientSocket;
        private  BufferedReader bufferedReader;
        private  PrintWriter printWriter;
        private InputStream inputStream;
        private Doctor doctor;

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

                    if (clientRequest.startsWith("REGISTER_PATIENT")) {
                        processPatientData(clientRequest);
                    } else if (clientRequest.startsWith("LOGIN_PATIENT")) {
                        processLoginPatient(clientRequest);
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
    private void processLoginPatient(String loginData) {
        String[] parts = loginData.split("\\|");

        if (parts.length != 2) {
            printWriter.println("ERROR|Invalid login format. Use 'username|password'");
            return;
        }

        String username = parts[0];
        String password = parts[1];

        try {
            if (!PatientService.isUsernameTaken(username)) {
                printWriter.println("ERROR|Username does not exist. Please register first.");
                return;
            }

            String hashedPassword = PasswordHash.hashPassword(password);
            if (PatientService.validatePatient(username, hashedPassword)) {
                printWriter.println("SUCCESS|Login successful. Welcome, " + username + "!");
            } else {
                printWriter.println("ERROR|Invalid password. Please try again.");
            }
        } catch (Exception e) {
            printWriter.println("ERROR|An unexpected error occurred: " + e.getMessage());
            System.err.println("Error during patient login: " + e.getMessage());
        }
    }

    //reception of data of the patient's REGISTRATION and write them in csvFile
    public void processPatientData(String patientData) {
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
                    printWriter.println("Error: Username already exists.");
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
                printWriter.println("SUCCESS| Patient registered successfully: " + patient.getUser());

            } catch (Exception e) {
                printWriter.println("ERROR|Failed to register patient:  " + e.getMessage());
                System.out.println("Error processing patient data: "+e.getMessage());
            }
        } else {
            printWriter.println("ERROR|Incorrect data format or missing fields.");
        }
    }



}
