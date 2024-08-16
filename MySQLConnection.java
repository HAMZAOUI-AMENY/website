import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class MySQLConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/evenements";
    private static final String USER = "root";
    private static final String PASSWORD = "Mysql24";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void retrieveData() {
        String selectQuery = "SELECT * FROM operations";

        try (Connection conn = getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(selectQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String userId = resultSet.getString("User_Id");
                String date = resultSet.getString("DateOperation");
                String operationType = resultSet.getString("TypeOperation");
                String barcode = resultSet.getString("code_barres");
                String location = resultSet.getString("Location");

                System.out.println(userId + ", " + date + ", " + operationType + ", " + barcode + ", " + location);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void retrieveDataByCodeBarres(String codeBarres) {
        String selectQuery = "SELECT * FROM operations WHERE code_barres = ? ORDER BY DateOperation DESC LIMIT 1";
    
        try (Connection conn = getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(selectQuery)) {
    
            preparedStatement.setString(1, codeBarres);
            ResultSet resultSet = preparedStatement.executeQuery();
    
            if (resultSet.next()) {
                String userId = resultSet.getString("User_Id");
                String date = resultSet.getString("DateOperation");
                String operationType = resultSet.getString("TypeOperation");
                String location = resultSet.getString("Location");
    
                if ("entrée".equalsIgnoreCase(operationType)) {
                    System.out.println("Entered " + location);
                } else if ("sortie".equalsIgnoreCase(operationType)) {
                    System.out.println("Has left " + location);
                } else {
                    System.out.println("Unknown operation type: " + operationType);
                }
            } else {
                System.out.println("No operations found for code_barres: " + codeBarres);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void insertData(String operationType, String barcode, String location) {
        String insertQuery = "INSERT INTO operations (User_Id, dateOperation, typeOperation, code_barres, Location) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            
            int userId = UserSession.getInstance().getMatricule(); // Assuming getUsername() returns the matricule as int
            preparedStatement.setInt(1, userId);
            preparedStatement.setObject(2, LocalDateTime.now());
            preparedStatement.setString(3, operationType);
            preparedStatement.setString(4, barcode);
            preparedStatement.setString(5, location);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Rows inserted: " + rowsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}
