import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LogDao {
    public void logAction(LogEntry logEntry) throws SQLException {
        String sql = "INSERT INTO operations (user_id, dateOperation, typeOperation, code_barres, location) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, logEntry.getUserId());
            stmt.setObject(2, logEntry.getDateOperation());
            stmt.setString(3, logEntry.getTypeOperation());
            stmt.setString(4, logEntry.getCodeBarres());
            stmt.setString(5, logEntry.getLocation());
            stmt.executeUpdate();
        }
    }
}
