package com.example.cbrav.androboum;

/**
 * Created by cbrav on 19/12/2017.
 */

public class Profil {


    // les différents status d'un utilisateur
    enum BombStatut { IDLE, AWAITING, BOMBER, BOMBED };
    // mon email
    private String email;
    // mon statut de connexion (vrai ou faux)
    boolean isConnected;
    // mon identifiant unique
    private String uid;
    // mon statut actuel
    private BombStatut statut = BombStatut.IDLE;
    // l'identifiant de mon adversaire
    private String otherUserUID;
    // l'email de mon adversaire
    private String otherUseremail;
    // mon score
    private int score = 0;

    public Profil() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public BombStatut getStatut() {
        return statut;
    }

    public void setStatut(BombStatut statut) {
        this.statut = statut;
    }

    public String getOtherUserUID() {
        return otherUserUID;
    }

    public void setOtherUserUID(String otherUserUID) {
        this.otherUserUID = otherUserUID;
    }

    public String getOtherUseremail() {
        return otherUseremail;
    }

    public void setOtherUseremail(String otherUseremail) {
        this.otherUseremail = otherUseremail;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
