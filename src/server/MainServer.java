package server;

import pojos.*;
import services.PatientService;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Scanner;

public class MainServer {

        private static final int PORT = 12345;
        private static final String ADMIN_PASSWORD = "admin123"; // Contraseña del administrador
        private static final int PORT_MULTICAST = 8888; //  multicast port
        private static final String MULTICAST_GROUP = "230.0.0.0";

        private static boolean running = true; // Control del servidor

        private static Doctor doctor;



    public static Doctor getDoctor() {
        return doctor;
    }

    public static void main(String[] args) {
        doctor = new Doctor("Virtual Doctor", " ");
        Scanner scanner = new Scanner(System.in);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            // Hilo multicast
            new Thread(() -> sendMulticast(PORT)).start();


            // Hilo client connections
            Thread clientHandlerThread = new Thread(() -> {
                while (running) {

                    try {
                        System.out.println("Waiting for clients...");
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Client connected.");

                        ClientHandler clientHandler = new ClientHandler(clientSocket, doctor);

                        Thread thread = new Thread(clientHandler);
                        thread.start();


                    } catch (IOException e) {
                        if (!running) { // Salir if server off
                            System.out.println("Error handling client connection: " + e.getMessage());
                            break;
                        }
                        System.out.println("Error handling client connection: " + e.getMessage());
                    }
                }
            });
            clientHandlerThread.start();

            while(running){
                showServerMenu(scanner, serverSocket);

            }
            clientHandlerThread.join();

        } catch (IOException |InterruptedException e) {
            System.err.println("Error starting server: " + e.getMessage());
        } finally {
            scanner.close();
            System.out.println("Server stopped.");
        }
    }
    private static void sendMulticast(int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            String message = "SERVER|" + InetAddress.getLocalHost().getHostAddress() + ":" + port;

            while (running) {
                DatagramPacket packet = new DatagramPacket(
                        message.getBytes(),
                        message.length(),
                        group,
                        PORT_MULTICAST
                );
                socket.send(packet);
               // System.out.println("Multicast sent: " + message);
                Thread.sleep(5000); // sending each 5 secs
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error in multicast: " + e.getMessage());
        }
    }

    private static void showServerMenu(Scanner scanner, ServerSocket serverSocket) {
        System.out.println("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("@@                                                                  @@");
        System.out.println("@@          Welcome! Server opened, listening for patients          @@");
        System.out.println("@@                 1. List all patients                             @@");
        System.out.println("@@                 2. Receive/Process Medical Record                @@");
        System.out.println("@@                 3. Show Medical Record with Plots                @@");
        System.out.println("@@                 0. Shutdown Server                               @@");
        System.out.println("@@                                                                  @@");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.print("\nSelect an option: \n");

        int option;
        try {
            option = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        switch (option) {
            case 1:
                System.out.println("Option 1 selected: List all patients.");
                listLivePatients();
                listCsvPatients();
                break;
            case 2:
                System.out.println("Option 2 selected: Receive/Process Medical Record.");
                System.out.println("Waiting to process medical records...");
                //receiveMedicalRecord(scanner);
                break;
            case 3:
                System.out.println("Option 3 selected: Show Medical Record with Plots.");
                showMedicalRecordWithPlots();
                break;
            case 0:
                System.out.println("Option 0 selected: Shutdown Server."); // Aquí se maneja el apagado y la autenticación
                AdminHandler adminHandler = new AdminHandler(serverSocket, ADMIN_PASSWORD);
                boolean continueRunning = adminHandler.runAdminMenu(scanner);
                if (!continueRunning) {
                    running = false;
                }
                break;

            default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
    }

    public static boolean isRunning() {
        return running;
    }

    public static void setRunning(boolean status) {
        running = status;
    }
    private static void listLivePatients() {

        List<Patient> patients = doctor.getPatients();
        if (patients.isEmpty()) {
            System.out.println("No patients registered in this sesion.");
        } else {
            System.out.println("\n--- Registered Patients during this session ---");
            for (Patient patient : patients) {
                System.out.println(patient.getName() + " " + patient.getSurname());
            }
        }
    }
    private static void listCsvPatients() {
        List<Patient> csvPatients = PatientService.readPatients();

        if (csvPatients.isEmpty()) {
            System.out.println("No patients found in the system (CSV).");
        } else {
            System.out.println("\n--- Patients Registered in the System (CSV) ---");
            for (Patient patient : csvPatients) {
                System.out.println(patient.getName() + " " + patient.getSurname());
            }
        }
    }

    private static void showMedicalRecordWithPlots() {
        System.out.println("Displaying medical record with plots...");

        // Verificar que el objeto doctor esté inicializado
        if (doctor == null) {
            System.out.println("Doctor instance is not initialized.");
            return;
        }

        // Elegir al paciente
        Patient patient= doctor.choosePatient();

        if (patient == null) {
            System.out.println("No patient selected.");
            return;
        }

        // medicalRecord of the patient
        MedicalRecord medicalRecord = doctor.chooseMR();
        if (medicalRecord == null) {
            System.out.println("No medical record available for the selected patient.");
            return;
        }
        //doctor.showInfoMedicalRecord(medicalRecord);
        medicalRecord.showAcc();
        medicalRecord.showEMG();
    }



}