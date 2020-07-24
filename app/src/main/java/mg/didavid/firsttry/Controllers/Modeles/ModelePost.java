package mg.didavid.firsttry.Controllers.Modeles;

public class ModelePost {
    //spécifications du posteur
    String Uid, nomEtPrenom, pseudo, photoDeProfile, pId, pTitre, pDescription, pImage, pTemps;

    public ModelePost(){ }

    public ModelePost(String Uid, String nomEtPrenom, String pseudo, String photoDeProfile, String pId, String pTitre, String pDescription, String pImage, String pTemps) {
        this.Uid = Uid;
        this.nomEtPrenom = nomEtPrenom;
        this.pseudo = pseudo;
        this.photoDeProfile = photoDeProfile;
        this.pId = pId;
        this.pTitre = pTitre;
        this.pDescription = pDescription;
        this.pImage = pImage;
        this.pTemps = pTemps;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        this.Uid = uid;
    }

    public String getNomEtPrenom() {
        return nomEtPrenom;
    }

    public void setNomEtPrenom(String nomEtPrenom) {
        this.nomEtPrenom = nomEtPrenom;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getPhotoDeProfile() {
        return photoDeProfile;
    }

    public void setPhotoDeProfile(String photoDeProfile) {
        this.photoDeProfile = photoDeProfile;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpTitre() {
        return pTitre;
    }

    public void setpTitre(String pTitre) {
        this.pTitre = pTitre;
    }

    public String getpDescription() {
        return pDescription;
    }

    public void setpDescription(String pDescription) {
        this.pDescription = pDescription;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getpTemps() {
        return pTemps;
    }

    public void setpTemps(String pTemps) {
        this.pTemps = pTemps;
    }
}

