import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class addUser {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter matricule:");
        int matricule = Integer.parseInt(scanner.nextLine());

        System.out.println("Enter name:");
        String nom = scanner.nextLine();

        System.out.println("Enter profile (admin/user):");
        String profil = scanner.nextLine();

        System.out.println("Enter password:");
        String plainPassword = scanner.nextLine();
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        try {
            insertUser(matricule, nom, profil, hashedPassword);
            System.out.println("User added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    public static void insertUser(int matricule, String nom, String profil, String hashedPassword) throws SQLException {
        String insertQuery = "INSERT INTO User (matricule, nom, profil, motdepasse) VALUES (?, ?, ?, ?)";

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(insertQuery)) {
            preparedStatement.setInt(1, matricule);
            preparedStatement.setString(2, nom);
            preparedStatement.setString(3, profil);
            preparedStatement.setString(4, hashedPassword);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Rows inserted: " + rowsAffected);
        }
    }
}
