package mg.didavid.firsttry.Models;

public class InfosUser {
    private String nom, prenom, motDePasse, email;
    private String phoneNo;
    private boolean sexeMale, sexeFemale;

    public InfosUser() { }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public boolean isSexeMale() {
        return sexeMale;
    }

    public void setSexeMale(boolean sexeMale) {
        this.sexeMale = sexeMale;
    }

    public boolean isSexeFemale() {
        return sexeFemale;
    }

    public void setSexeFemale(boolean sexeFemale) {
        this.sexeFemale = sexeFemale;
    }
}
