import java.sql.*;

public class OperationService {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/evenements";
    private static final String USER = "root";
    private static final String PASSWORD = "Mysql24";

    public void addOperation(String location, String operationType, String barcode) throws SQLException {
        String query = "INSERT INTO operations (location, typeOperation, code_barres) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, location);
            stmt.setString(2, operationType);
            stmt.setString(3, barcode);
            stmt.executeUpdate();
        }
    }

    public String getAllOperations() throws SQLException {
        StringBuilder response = new StringBuilder();
        String query = "SELECT * FROM operations";
        
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                response.append("Operation ID: ").append(rs.getInt("User_ID"))
                        .append(", Location: ").append(rs.getString("location"))
                        .append(", Type: ").append(rs.getString("typeOperation"))
                        .append(", Barcode: ").append(rs.getString("code_barres"))
                        .append("\n");
            }
        }
        
        return response.toString();
    }
   

    public String searchOperation(String barcode) throws SQLException {
        StringBuilder response = new StringBuilder();
        String query = "SELECT * FROM operations WHERE code_barres = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, barcode);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    response.append("Operation ID: ").append(rs.getInt("User_ID"))
                            .append(", Location: ").append(rs.getString("location"))
                            .append(", Type: ").append(rs.getString("typeOperation"))
                            .append(", Barcode: ").append(rs.getString("code_barres"))
                            .append("\n");
                }
            }
        }
        return response.toString();
    }
}
