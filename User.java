public class User {
    private int matricule;
    private String nom;
    private String profil;
    private String motdepasse;

    public User(int matricule, String nom, String profil, String motdepasse) {
        this.matricule = matricule;
        this.nom = nom;
        this.profil = profil;
        this.motdepasse = motdepasse;
    }

    // Getters
    public int getMatricule() {
        return matricule;
    }

    public String getNom() {
        return nom;
    }

    public String getProfil() {
        return profil;
    }
    public User(String nom, String motdepasse) {
        this.nom = nom;
        this.motdepasse = motdepasse;
    }
    public String getMotdepasse() {
        return motdepasse;
    }

    // Setters
    public void setMatricule(int matricule) {
        this.matricule = matricule;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public void setMotdepasse(String motdepasse) {
        this.motdepasse = motdepasse;
    }
}
