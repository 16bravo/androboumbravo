package com.example.cbrav.androboum;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Nicolas on 23/09/2017.
 */

public class AndroBoumApp extends android.app.Application {

    public static Bomber getBomber() {
        return bomber;
    }

    private static Bomber bomber;

    static public void setCallback(Bomber.BomberInterface callback) {
        bomber.setCallback(callback);
    }

    static public void setContext(Context context) {
        bomber.setContext(context);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("AndroBoumApp", "coucou");
    }

    static public void buildBomber(Context c)   {
        bomber = new Bomber(c);
    }

    // méthode utilitaire pour spécifier le statut de connexion (vrai ou faux)
    static public void setIsConnected(boolean connectStatus) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser fuser = auth.getCurrentUser();
        if (auth != null) {
            final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
            DatabaseReference mreference = mDatabase.getReference().child("Users").child(fuser.getUid());
            mreference.child("connected").setValue(connectStatus);
        }
    }


}
