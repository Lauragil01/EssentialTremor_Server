package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class AdminHandler implements Runnable {

    private final ServerSocket serverSocket;
    private final String adminPassword;

    public AdminHandler(ServerSocket serverSocket, String adminPassword) {
        this.serverSocket = serverSocket;
        this.adminPassword = adminPassword;
    }
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean keepRunning = true;

        while (keepRunning && MainServer.isRunning()) {
            keepRunning = runAdminMenu(scanner);
        }

        System.out.println("Admin thread exiting...");
    }


//runAdminMenu in AdminHandler verifies client count
// using ClientHandler.getConnectedClients().
public boolean runAdminMenu(Scanner scanner) {
    while(true) {
        System.out.println("\n@@@@ Admin Menu @@@@");
        System.out.println("1. Shut down server");
        System.out.println("2. Exit admin menu");
        System.out.print("Select an option: ");

        String option = scanner.nextLine();

        switch (option) {
            case "1":
                boolean shutdownConfirmed = handleShutdown(scanner);
                if (!shutdownConfirmed) {
                return false; // Indica que el servidor debe detenerse
                }
                break;
            case "2":
                System.out.println("Exiting admin menu.");
                return true; // Volver al menú principal
            default:
                System.out.println("Invalid option. Try again.");

        }
    }

}

    private boolean handleShutdown(Scanner scanner) {
        int connectedClients = ClientHandler.getConnectedClients();

        if (connectedClients > 0) {
            System.out.println("There are currently " + connectedClients + " client(s) connected.");
            System.out.println("Shutdown not allowed while clients are connected.");
            return true; // No cerrar el servidor
        }
        System.out.print("Are you sure you want to shut down the server? (yes/no): ");
        String confirmation = scanner.nextLine();

        if (!confirmation.equalsIgnoreCase("yes")) {
            System.out.println("Returning to admin menu...");
            return true;
        }
        //elseee

        System.out.print("Enter admin password to shut down the server: ");
        String passwordAdmin = scanner.nextLine();

        if (passwordAdmin.equals(adminPassword)) {
            System.out.println("Password verified. Shutting down the server...");
            //MainServer.stopAllClients();
            MainServer.setRunning(false);
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
            return false; // turn off server
        } else {
            System.out.println("Incorrect password. Shutdown aborted.");
            return true;
        }
    }
}






