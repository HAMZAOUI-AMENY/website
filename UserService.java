import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

public class UserService {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    private static final String DB_URL = "jdbc:mysql://localhost:3306/evenements";
    private static final String USER = "root";
    private static final String PASSWORD = "Mysql24";

    public boolean authenticateUser(int matricule, String motdepasse) throws SQLException {
        String hashedPassword = PasswordUtil.hashPassword(motdepasse);
        String query = "SELECT COUNT(*) FROM user WHERE matricule = ? AND motdepasse = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            logger.log(Level.INFO, "Authenticating user with matricule: {0}", matricule);

            statement.setInt(1, matricule);
            statement.setString(2, hashedPassword);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    logger.log(Level.INFO, "Number of users found: {0}", count);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL error during user authentication", e);
            throw e;
        }
        return false;
    }

    public String getUserProfile(int matricule) throws SQLException {
        String query = "SELECT profil FROM user WHERE matricule = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            logger.log(Level.INFO, "Fetching profile for user with matricule: " + matricule);

            statement.setInt(1, matricule);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String profile = resultSet.getString("profil");
                    logger.log(Level.INFO, "Profile found: " + profile);
                    return profile;
                } else {
                    logger.log(Level.WARNING, "No profile found for matricule: " + matricule);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL error during fetching user profile", e);
            throw e;
        }
        return null;
    }

    public boolean addOperation(int matricule, String location, String operationType, String barcode) {
        String query = "INSERT INTO operations (User_ID, dateOperation, typeOperation, code_barres, location) VALUES (?, NOW(), ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, matricule);
            statement.setString(2, operationType);
            statement.setString(3, barcode);
            statement.setString(4, location);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addUser(int matricule, String nom, String profil, String motdepasse) throws SQLException {
        String hashedPassword = PasswordUtil.hashPassword(motdepasse);
        String query = "INSERT INTO user (matricule, nom, profil, motdepasse) VALUES (?, ?, ?, ?)";

        logger.log(Level.INFO, "Attempting to add user with matricule: {0}, name: {1}, profile: {2}",
                   new Object[]{matricule, nom, profil});

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, matricule);
            statement.setString(2, nom);
            statement.setString(3, profil);
            statement.setString(4, hashedPassword);

            logger.log(Level.INFO, "Executing query: " + statement.toString());
            int rowsAffected = statement.executeUpdate();
            logger.log(Level.INFO, "Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL error during user addition", e);
            throw e;
        }
    }

    public JSONObject getLastOperationStatus(String barcode) throws SQLException {
        String query = "SELECT typeOperation, location FROM operations WHERE code_barres = ? ORDER BY dateOperation DESC LIMIT 1";
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, barcode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String operationType = resultSet.getString("typeOperation");
                    String location = resultSet.getString("location");
                    JSONObject json = new JSONObject();
                    json.put("status", "success");
                    json.put("barcode", barcode);
                    json.put("operationType", operationType);
                    json.put("location", location);
                    return json;
                } else {
                    JSONObject json = new JSONObject();
                    json.put("status", "not_found");
                    return json;
                }
            }
        }
    }
}
