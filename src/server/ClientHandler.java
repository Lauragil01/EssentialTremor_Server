package server;

import pojos.Patient;
import pojos.User;
import services.PatientService;
import utils.PasswordHash;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientHandler implements Runnable{
        private Socket clientSocket;
        private  BufferedReader bufferedReader;
        private  PrintWriter printWriter;
        private InputStream inputStream;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
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
                releaseResources(inputStream,clientSocket);
            }
        }
    private void releaseResources(InputStream inputStream, Socket socket) {
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, "Error closing InputStream", ex);
        }
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, "Error closing Socket", ex);
        }
    }


    //log in patient--> processing data sent
    public void processLoginPatient(String loginData) throws  IOException{
        // Los datos del cliente llegan en formato: "username|password"
        if(loginData==null || !loginData.contains("|")){
            printWriter.println("Error invalid log in format");
        }
        System.out.println("received login data" +loginData);

        String[] parts = loginData.split("\\|");

        // validation format
        if (parts.length != 2) {
            printWriter.println("ERROR|Invalid login format. Use 'username|password'");
            return;
        }

        String username = parts[0].trim();
        String password = parts[1].trim();

        try {

            // Validar las credenciales usando PatientService
            boolean exits = PatientService.isUsernameTaken(username);

            if (!exits) {
                printWriter.println("ERROR|Username does not exist.Please register first. ");
                return;
            }
            // Hashear la contraseña antes de validarla
            String hashedPassword = PasswordHash.hashPassword(password);
            Boolean isValid=PatientService.validatePatient(username, hashedPassword);

            if(isValid){
                printWriter.println("SUCCESS| Login successful. Welcome "+username);
            }else{
                printWriter.println("ERROR| Invalid password. Please try again.");
            }

        } catch (Exception e) {
            printWriter.println("ERROR|An unexpected error occurred: " + e.getMessage());
            System.out.println("Error during patient login: " + e.getMessage());
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
                    System.out.println("Error: Username already exists.");
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

                System.out.println("Patient registered successfully: " + patient.getUser());
            } catch (Exception e) {
                System.err.println("Error processing patient data: " + e.getMessage());
            }
        } else {
            System.err.println("Error: Incorrect data format or missing fields.");
        }
    }



}
