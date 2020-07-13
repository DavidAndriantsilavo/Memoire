package mg.didavid.firsttry.Models;

public class InfosUser {
    private String Uid, nom, prenom, motDePasse, email;
    private String phoneNo;
    private boolean sexe; // 1 = male and 0 = female

    public InfosUser() { }

    public InfosUser(String Uid, String nom, String prenom, String motDePasse, String email, String phoneNo, boolean sexe) {
        this.Uid = Uid;
        this.nom = nom;
        this.prenom = prenom;
        this.motDePasse = motDePasse;
        this.email = email;
        this.phoneNo = phoneNo;
        this.sexe = sexe;
    }

    public String getUid() {
        return Uid;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public boolean isSexe() {
        return sexe;
    }
}
