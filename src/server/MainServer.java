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

    /*public static void main(String[] args) {
        serverSocket = null;
        printWriter = null;
        bufferedReader = null;

        try {
            //Create socket
            serverSocket = new ServerSocket(12345);

            //TODO wait for connections
            try {
                while (true) {
                    System.out.println("Waiting for clients...");
                    clientSocket = serverSocket.accept();
                    System.out.println("Client connected.");
                    //TODO: Thread
                    //new Thread(new Patient(clientSocket)).start();
                    printWriter = new PrintWriter(MainServer.clientSocket.getOutputStream(), true);
                    bufferedReader = new BufferedReader(new InputStreamReader(MainServer.clientSocket.getInputStream()));

                    String patientData = bufferedReader.readLine();
                    processPatientData(patientData);


                    int option;
                    try {
                        control = true;
                        while (control) {
                            System.out.println("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                            System.out.println("@@                                                                  @@");
                            System.out.println("@@          Welcome! server opened, listening for patients          @@");
                            System.out.println("@@                 1. List of patients                              @@");
                            System.out.println("@@                 2. List medical record                           @@");
                            System.out.println("@@                                                                  @@");
                            System.out.println("@@                Press 0 to close the Server                       @@");
                            System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                            System.out.print("\nSelect an option: ");

                            try {
                                option = sc.nextInt();
                            } catch (InputMismatchException e) {
                                System.out.println("Invalid input. Please enter a number.");
                                sc.next(); // Clear the invalid input
                                continue; // Restart the loop
                            }
                            switch (option) {
                                case 1:
                                    break;
                                case 2:
                                    break;
                                case 0:
                                    conexion = false;
                                    control = false;
                                    break;
                                default:
                                    System.out.println("  NOT AN OPTION \n");
                                    break;
                            }
                        }


                    } catch (NumberFormatException e) {
                        System.out.println("  NOT A NUMBER. Closing application... \n");
                        sc.close();
                    }


                }
            } catch (IOException ex) {
                System.out.println("Error with the connection");
                ex.printStackTrace();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            releaseResourcesServer(serverSocket);
            sc.close();
        }
    }*/
        private static final int PORT = 12345;
        private static final String ADMIN_PASSWORD = "admin123"; // Contraseña del administrador
        private static boolean running = true; // Control del servidor



    public static void main(String[] args) {
        Doctor doctor=new Doctor("Virtual Doctor", " ");

        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);
            System.out.println("Waiting for clients...");

            AdminHandler adminHandler = new AdminHandler(serverSocket, ADMIN_PASSWORD);
            Thread adminThread = new Thread(adminHandler);
            adminThread.start();

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected.");

                    ClientHandler clientHandler = new ClientHandler(clientSocket, doctor);
                    Thread thread = new Thread(clientHandler);
                    thread.start();
                } catch (IOException e) {
                    System.err.println("Error client connection: " + e.getMessage());
                    running = false;//Detener el servidor si hay error
                }
            }

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }finally{
            System.out.println("Server stopped");
        }
    }

}
/*


        public static void main(String[] args) {
            Doctor doctor=new Doctor("Virtual Doctor", " ");
            //boolean running=true;
            Scanner sc = new Scanner(System.in);

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {

                System.out.println("Server is running on port " + PORT + "...");
                System.out.println("Waiting for clients...");

                // Crear un hilo para manejar comandos del administrador usando clase personalizada
                AdminHandler adminHandler = new AdminHandler(sc, serverSocket);
                Thread adminThread = new Thread(adminHandler);
                adminThread.start();

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Client connected."+ clientSocket.getInetAddress());

                        ClientHandler clientHandler = new ClientHandler(clientSocket, doctor);
                        Thread clientThread = new Thread(clientHandler);
                        clientThread.start();

                    } catch (IOException e) {
                        System.err.println("Error client connection: " + e.getMessage());
                        running = false;
                    }
                }
            }catch(IOException ex){
                System.out.println("Error starting server: "+ ex.getMessage());
            }finally{
                sc.close();
                System.out.println("Server stopped");
            }
        }



    private static void releaseResourcesServer(ServerSocket serverSocket) {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("Server resources released. Goodbye!");
            } catch (IOException e) {
                System.err.println("Error closing server resources: " + e.getMessage());
            }
        }
    }




   /* private static void showServerMenu(ServerSocket serverSocket) {
        System.out.println("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("@@                                                                  @@");
        System.out.println("@@                       Server Administration Menu                 @@");
        System.out.println("@@                 1. List all patients                             @@");
        System.out.println("@@                 2. Exit                                          @@");
        System.out.println("@@                                                                  @@");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.print("\nSelect an option: ");

        try {
            int choice = sc.nextInt();
            sc.nextLine(); // Consumir el salto de línea

            switch (choice) {
                case 1:
                    listAllPatients();
                    break;
                case 2:
                    System.out.println("Exiting server...");
                    releaseResourcesServer(serverSocket);
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid number.");
            sc.next(); // Consumir entrada no válida
        }
    }


    //TODO log in correctly--> Pedir al cliente otra vez que pida log in al paciente while(invalid credentials)




    //TODO: REVISIÓN METODO
    public static void menuUser(User u) throws IOException {
        int option;
        MedicalRecord mr = null;
        Doctor doctor = null;
        //TODO: SUSTITUTION TO FILE
        // doctor = doctorManager.getDoctorByUserId(u.getId()); //TODO meter doctor

        while (true) {
            printMenuDoctor();
            try {
                option = sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.next(); // Clear the invalid input
                continue; // Restart the loop
            }

            switch (option) {
                case 1: {
                    mr = doctor.receiveMedicalRecord(clientSocket, bufferedReader);
                    if (mr != null) {
                        //TODO: SUSTITUTION TO FILE--
                        //medicalRecordManager.addMedicalRecord(mr);
                    }
                    break;
                }
                case 2: {
                    if (mr != null) {
                        //doctor.showInfoMedicalRecord(mr);
                        //TODO option to create doctor note
                        //DoctorsNote dn = chooseToDoDoctorNotes(mr);
                        //mr.getDoctorsNotes().add(dn);
                        //chooseToSendDoctorNotes(dn);
                    } else {
                        System.out.println("No medical record detected, please select option one");
                        break;
                    }
                }
            }
        }

    }

    public static void printMenuDoctor() {
        System.out.println("\n@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.println("@@                                                                  @@");
        System.out.println("@@                 Welcome.                                         @@");
        System.out.println("@@                 1. Receive medical record                        @@");
        System.out.println("@@                 2. Show medical record                           @@");
        System.out.println("@@                 0. Exit                                          @@");
        System.out.println("@@                                                                  @@");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        System.out.print("\nSelect an option: ");
    }*/

    /*public static DoctorsNote chooseToDoDoctorNotes(MedicalRecord mr) {
        System.out.println("\nDo you want to create a doctors note? (y/n)");
        String option = sc.nextLine();
        DoctorsNote dn = null;
        if (option.equalsIgnoreCase("y")) {
            dn = doctor.createDoctorsNote(mr);
            if (dn != null) {
                //doctorNotesManager.addDoctorNote(dn); // Inserción en la base de datos
            }
        } else if (!option.equalsIgnoreCase("y") || option.equalsIgnoreCase("n")) {
            System.out.println("Not a valid option, try again...");
            chooseToDoDoctorNotes(mr);
        }
        return dn;
    }
*/






    //****************MENU TESTS*******************************
    /*public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is running on port 12345...");

            while (true) {
                // Aceptar conexión del cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected.");

                try (
                        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter printWriter1 = new PrintWriter(clientSocket.getOutputStream(), true)){
                    // Leer solicitud del cliente
                    String clientRequest = reader.readLine();
                    System.out.println("Received request: " + clientRequest);

                    // Procesar la solicitud
                    if (clientRequest.startsWith("REGISTER_PATIENT")) {
                        processPatientData(clientRequest);
                    } else if (clientRequest.startsWith("LOGIN_PATIENT")) {
                        processLoginPatient(clientRequest);
                    } else {
                        printWriter1.println("ERROR|Unknown request type.");
                    }
                } catch (IOException e) {
                    System.err.println("Error processing client request: " + e.getMessage());
                } finally {
                    // Cerrar el socket del cliente después de procesar la solicitud
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }



}
*/