package server;

import pojos.*;
import services.DoctorService;
import services.PatientService;
import utils.CsvHandler;
import utils.PasswordHash;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class MainServer {

        private static final int PORT = 12345;
        private static final String ADMIN_PASSWORD = "admin123"; // Contraseña del administrador
        private static boolean running = true; // Control del servidor
        private static Patient patient;
        private static Doctor doctor;
        private static MedicalRecord medicalRecord;

    /*public static void main(String[] args) {
        doctor = new Doctor("Virtual Doctor", " ");
        Scanner scanner = new Scanner(System.in);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            // Hilo para manejar comandos del administrador
            AdminHandler adminHandler = new AdminHandler(serverSocket, ADMIN_PASSWORD);
            Thread adminThread = new Thread(adminHandler);
            adminThread.start();

            // Mostrar el menú nada más iniciar la aplicación
            while (running) {
                showServerMenu(scanner, serverSocket);
                // Aquí, el servidor seguirá aceptando conexiones después de mostrar el menú
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected.");

                ClientHandler clientHandler = new ClientHandler(clientSocket, doctor);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        } finally {
            scanner.close();
            System.out.println("Server stopped.");
        }
    }


     */


    public static void main(String[] args) {
        doctor = new Doctor("Virtual Doctor", " ");
        Scanner scanner = new Scanner(System.in);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            // Hilo para manejar comandos del administrador
            AdminHandler adminHandler = new AdminHandler(serverSocket, ADMIN_PASSWORD);
            Thread adminThread = new Thread(adminHandler);
            adminThread.start();

            while (running) {
                System.out.println("Waiting for clients...");

                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected.");

                    ClientHandler clientHandler = new ClientHandler(clientSocket, doctor);
                    Thread thread = new Thread(clientHandler);
                    thread.start();

                    while (running) {
                        showServerMenu(scanner, serverSocket);
                    }

                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        } finally {
            scanner.close();
            System.out.println("Server stopped.");
        }
    }

    /**
     * Displays the server menu and allows the administrator to manage the server.
     *
     * @param scanner Scanner object to read user input
    // * @param serverSocket ServerSocket instance to manage server operations
     */
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
        System.out.print("\nSelect an option: ");

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
                listAllPatients(); // Call the method to list all patients
                break;
            case 2:
                System.out.println("Option 2 selected: Receive/Process Medical Record.");
                //receiveMedicalRecord(scanner);
                break;
            case 3:
                System.out.println("Option 3 selected: Show Medical Record with Plots.");
                showMedicalRecordWithPlots(); // Llamas al metodo que muestra los gráficos
                break;
            case 0:
                handleShutdown(scanner, serverSocket); // Aquí se maneja el apagado y la autenticación
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
    }

    /**
     * Handles the server shutdown process, verifying the admin password before closing the server.
     *
     * @param scanner Scanner object to read user input
     * @param serverSocket ServerSocket instance to manage server operations
     */
    private static void handleShutdown(Scanner scanner, ServerSocket serverSocket) {
        System.out.print("Enter admin password to shut down the server: ");
        String enteredPassword = scanner.nextLine();

        if (enteredPassword.equals(ADMIN_PASSWORD)) {
            System.out.println("Password verified. Shutting down the server...");
            running = false;
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        } else {
            System.out.println("Incorrect password. Shutdown aborted.");
        }
    }

    public static void setRunning(boolean isRunning) {
        running = isRunning;
    }

    private static void listAllPatients() {
        List<Patient> patients = doctor.getPatients();
        if (patients.isEmpty()) {
            System.out.println("No patients registered.");
        } else {
            System.out.println("\n--- Registered Patients ---");
            for (Patient patient : patients) {
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
        patient = doctor.choosePatient();
        if (patient == null) {
            System.out.println("No patient selected.");
            return;
        }

        // Obtener el expediente médico del paciente
        MedicalRecord medicalRecord = patient.chooseMR();
        if (medicalRecord == null) {
            System.out.println("No medical record available for the selected patient.");
            return;
        }

        // Mostrar y graficar las señales de ACC
        if (medicalRecord.getAcc() != null) {
            medicalRecord.getAcc().plotSignal();  // Graficar la señal de ACC
        } else {
            System.out.println("No ACC signal data available.");
        }

        // Mostrar y graficar las señales de EMG
        if (medicalRecord.getEmg() != null) {
            medicalRecord.getEmg().plotSignal();  // Graficar la señal de EMG
        } else {
            System.out.println("No EMG signal data available.");
        }
    }





}