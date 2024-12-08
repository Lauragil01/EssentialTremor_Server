package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHash {
    public static String hashPassword(String password) {
        try {
            // Obtener una instancia de MessageDigest para el algoritmo MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Convertir la contraseña en bytes y aplicar el hash
            byte[] hashBytes = md.digest(password.getBytes());

            // Convertir los bytes en un formato legible (hexadecimal)
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString(); // Retornar el hash como String
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return password; // Si ocurre un error, devolver la contraseña sin hashear
        }
    }
}
