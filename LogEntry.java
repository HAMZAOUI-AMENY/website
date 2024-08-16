import java.time.LocalDateTime;

public class LogEntry {
    private String userId;
    private LocalDateTime dateOperation;
    private String typeOperation;
    private String codeBarres;
    private String location;

    public LogEntry(String userId, String typeOperation, String codeBarres, String location) {
        this.userId = userId;
        this.dateOperation = LocalDateTime.now();
        this.typeOperation = typeOperation;
        this.codeBarres = codeBarres;
        this.location = location;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getDateOperation() {
        return dateOperation;
    }

    public String getTypeOperation() {
        return typeOperation;
    }

    public String getCodeBarres() {
        return codeBarres;
    }

    public String getLocation() {
        return location;
    }
}
