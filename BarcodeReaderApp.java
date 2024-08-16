import java.util.Scanner;

public class BarcodeReaderApp {

    public static void main(String[] args) {
        // Ensure user session is established before proceeding
        UserSession session = UserSession.getInstance();
        if (session == null) {
            System.out.println("User session not found. Please log in first.");
            return;
        }

        String barcode = readBarcode();

        if (barcode != null && !barcode.isEmpty()) {
            System.out.println("Processing barcode: " + barcode);
            
            // Additional inputs for operation type and location
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter operation type (entrée/sortie): ");
            String operationType = scanner.nextLine();
            System.out.print("Enter location: ");
            String location = scanner.nextLine();
            
            // Store the operation in the database
            MySQLConnection.insertData(operationType, barcode, location);
            
        } else {
            System.out.println("No barcode scanned.");
        }
    }

    public static String readBarcode() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please scan a barcode:");
        
        if (scanner.hasNextLine()) {
            String barcode = scanner.nextLine();
            if (!barcode.isEmpty()) {
                System.out.println("Barcode scanned: " + barcode);
                return barcode;
            }
        }
        return null;
    }
}
