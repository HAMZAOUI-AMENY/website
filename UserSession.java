public class UserSession {
    private static UserSession instance;
    private int matricule;

    UserSession(int matricule) {
        this.matricule = matricule;
    }

    public static UserSession getInstance(int matricule) {
        if (instance == null) {
            instance = new UserSession(matricule);
        }
        return instance;
    }

    public static UserSession getInstance() {
        return instance;
    }
    
    public int getMatricule() {
        return matricule;
    }

    public void clearSession() {
        instance = null;
    }
}
