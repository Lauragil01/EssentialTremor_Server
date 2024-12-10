package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

public class AdminHandler implements Runnable {

    private final ServerSocket serverSocket;
    private final String adminPassword;

    public AdminHandler(ServerSocket serverSocket, String adminPassword) {
        this.serverSocket = serverSocket;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print("Enter 'admin' to access the admin menu: ");
                String input = reader.readLine();

                if (input.equalsIgnoreCase("admin")) {
                    if (authenticateAdmin(reader)) {
                        adminMenu(reader);
                    } else {
                        System.out.println("Incorrect password. Access denied.");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
        }
    }

    private boolean authenticateAdmin(BufferedReader reader) {
        try {
            System.out.print("Enter admin password: ");
            String enteredPassword = reader.readLine();
            return enteredPassword.equals(adminPassword);
        } catch (IOException e) {
            System.err.println("Error reading password: " + e.getMessage());
            return false;
        }
    }

    private void adminMenu(BufferedReader reader) {
        boolean adminRunning = true;
        while (adminRunning) {
            System.out.println("\n@@@@ Admin Menu @@@@");
            System.out.println("1. Shut down server");
            System.out.println("2. Exit menu");

            try {
                System.out.print("Select an option: ");
                String option = reader.readLine();

                switch (option) {
                    case "1":
                        if (checkClientsBeforeShutdown()) { // Verifico si hay clientes antes de permitir cerrar el servidor
                            if (confirmShutdown(reader)) {
                                shutdownServer();
                                adminRunning = false;
                            }
                        } else {
                            System.out.println("Cannot shut down the server. There are clients currently connected.");
                        }
                        break;
                    case "2":
                        System.out.println("Exiting admin menu.");
                        adminRunning = false;
                        break;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            } catch (IOException e) {
                System.err.println("Error reading admin option: " + e.getMessage());
            }
        }
    }
    private boolean checkClientsBeforeShutdown() {
        int connectedClients = ClientHandler.getConnectedClients();
        if (connectedClients > 0) {
            System.out.println("Currently connected clients: " + connectedClients);
            return false; // Hay clientes conectados, no se puede apagar el servidor
        }
        return true; // No hay clientes conectados, se puede apagar el servidor
    }

    private boolean confirmShutdown(BufferedReader reader) {
        try {
            System.out.print("Are you sure you want to shut down the server? (write y/n for yes or no): ");
            String confirmation = reader.readLine();
            return confirmation.equalsIgnoreCase("y");
        } catch (IOException e) {
            System.err.println("Error reading shutdown confirmation: " + e.getMessage());
            return false;
        }
    }

    private void shutdownServer() {
        System.out.println("Shutting down server...");
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }
}
