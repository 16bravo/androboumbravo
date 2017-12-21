package com.example.cbrav.androboum;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Bomber {

    // mon profil
    Profil me, newme;

    // le profil de mon adversaire éventuel
    Profil other = new Profil();

    // le contexte de l'activité en cours
    Context context;

    // l'identifiant unique qu'on attribue à la notification
    private int mNotificationId = 10;

    // le timer nécessaire pour le compte à rebours
    public static CountDownTimer timer;

    // le temps qui reste avant l'explosion de la bombe
    int timeleft;

    // mémorise le moment du dépot de la bombe
    static public long bombedTime = 0;

    // combien de temps met la bombe pour exploser en ms
    static public long timetoboum = 10000;

    // interface déclarant les deux callbacks.
    public interface BomberInterface {
        void userBombed();
        void userBomber();
    }

    BomberInterface callback;

    // méthode appelée pour positionner les deux callbacks.
    // si cette méthode est appelée alors que je suis déjà un bombed ou un bomber
    // j'appelle immédiatement les callbacks.
    public void setCallback(BomberInterface callback) {
        this.callback = callback;
        if (me.getStatut() == Profil.BombStatut.BOMBED) callback.userBombed();
        if (me.getStatut() == Profil.BombStatut.BOMBER) callback.userBomber();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Profil getOther() {
        return other;
    }

    public Bomber(Context context) {
        this.context = context;
        // on va chercher notre uid
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser fuser = auth.getCurrentUser();
        // on l'utilise pour s'abonner à l'évolution des informations sur notre profil
        final FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mreference = mDatabase.getReference().child("Users").child(fuser.getUid());

        // on positionne notre statut de bombage sur IDLE
        mreference.child("statut").setValue(Profil.BombStatut.IDLE);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // une information a changé me concernant

                // j'obtiens mes nouvelles informations
                newme = dataSnapshot.getValue(Profil.class);

                // j'en extrait l'identité de l'autre.
                other.setUid(newme.getOtherUserUID());
                other.setEmail(newme.getOtherUseremail());
                // si je viens de me faire "bomber" et qu'on attend de moi un acquittement
                if (newme.getStatut() == Profil.BombStatut.AWAITING && (me == null || me.getStatut() != Profil.BombStatut.AWAITING)) {
                    // je passe dans l'état "bombé" et je fais apparaître
                    // une notification
                    updateStatut(newme.getUid(), Profil.BombStatut.BOMBED);
                    notifyBombed();
                }
                // si je deviens un bomber
                if (newme.getStatut() == Profil.BombStatut.BOMBER && (me == null || me.getStatut() != Profil.BombStatut.BOMBER)) {
                    // je note le moment du bombage
                    bombedTime = System.currentTimeMillis();
                    // j'appelle le bon callback s'il existe
                    if (callback != null) callback.userBomber();
                }
                // si je deviens un bombé
                if (newme.getStatut() == Profil.BombStatut.BOMBED && (me == null || me.getStatut() != Profil.BombStatut.BOMBED)) {
                    // je note le moment du bombage
                    bombedTime = System.currentTimeMillis();
                    // j'appelle le bon callback s'il existe
                    if (callback != null) callback.userBombed();
                }
                // je mets à jour mon profil avec les nouvelles informations
                me = newme;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mreference.addValueEventListener(postListener);
    }

    // fait apparaître une notification qui signale qu'on a été "bombé"
    private void notifyBombed() {
        // spécification de la notification
        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_priority_high_black_24dp)
                        .setContentTitle("AndroBoumApp")
                        .setContentText(context.getResources().getString(R.string.bombedText) + " " + other.getEmail() + " 5s")
                        .setVibrate(new long[]{0, 1000});
        ;

        // spécification de l'activité appelée en cas de click sur la notification
        Intent resultIntent = new Intent(context, BombActivity.class);
        resultIntent.putExtra("notification", true);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);


        // mise en place de la notification
        final NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mNotificationId, mBuilder.build());

        // on spécifie un compte à rebours
        if (timer != null) timer.cancel();
        timeleft = (int) (timetoboum / 1000);

        timer = new CountDownTimer(timetoboum, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeleft--;
                mBuilder.setContentText(context.getResources().getString(R.string.bombedText) + " " + other.getEmail() + " " + timeleft + "s");
                mNotificationManager.notify(mNotificationId, mBuilder.build());
            }

            @Override
            public void onFinish() {
                // la bombe a explosé chez la cible
                mBuilder.setContentText(context.getResources().getString(R.string.bombexplosed) + " " + other.getEmail() + " " + timeleft + "s");
                mNotificationManager.notify(mNotificationId, mBuilder.build());
                Toast toast = Toast.makeText(context, context.getResources().getString(R.string.bombexplosed), Toast.LENGTH_SHORT);
                toast.show();
                // on repasse en IDLE
                setIdle();
            }
        };

        // on démarre le compte à rebours
        timer.start();
    }

    // méthode qui met à jour le score le score de l'utilisateur connecté
    public void addToScore(int change) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        me.setScore(me.getScore() + change);
        mDatabase.child("Users").child(me.getUid()).child("score").setValue(me.getScore());
    }

    // méthode qui met à jour le statut de l'utilisateur d'id uid
    private void updateStatut(String uid, Profil.BombStatut statut) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).child("statut").setValue(statut);
    }

    // méthode qui positionne l'email et l'uid de la cible (ou de l'attaquant selon
    // qui on est.
    private void updateOther(String uid, Profil other) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(uid).child("otherUserUID").setValue(other == null ? null : other.getUid());
        mDatabase.child("Users").child(uid).child("otherUseremail").setValue(other == null ? null : other.getEmail());
    }

    public void setIdle() {
        updateStatut(me.getUid(), Profil.BombStatut.IDLE);
    }

    public void setBomb(final Profil cible, final BomberInterface callback) {

        // on ne doit pas pouvoir s'auto-cibler
        if (cible.getUid().equals(me.getUid())) {
            Toast toast = Toast.makeText(context, context.getString(R.string.cantbombme), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // si le statut de la cible n'est pas IDLE, on ne peut pas le bomber
        if (cible.getStatut() != Profil.BombStatut.IDLE) {
            Toast toast = Toast.makeText(context, context.getString(R.string.userbusy), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        // sinon on met à jour le statut de la cible sur "AWAITING" pour demander
        // un acquittement.
        updateStatut(cible.getUid(), Profil.BombStatut.AWAITING);
        updateOther(cible.getUid(), me);


        // on programme une routine pour dans deux secondes.
        // si au bout de ce délais, pas d'acquittement, on enlève le status "awaiting"
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // on repasse l'attaquant et la cible en statut IDLE
                // car pas d'acquittement de la cible
                updateStatut(cible.getUid(), Profil.BombStatut.IDLE);
                updateOther(cible.getUid(), null);
                updateStatut(me.getUid(), Profil.BombStatut.IDLE);
                updateOther(me.getUid(), null);

                Toast toast = Toast.makeText(context, context.getString(R.string.nouserresponse), Toast.LENGTH_SHORT);
                toast.show();
            }
        }, 2000);

        // on s'abonne à la mise à jour du profil cible.
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(cible.getUid());
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profil newCible = dataSnapshot.getValue(Profil.class);
                if (newCible.getStatut() == Profil.BombStatut.BOMBED) {
                    // on a recu l'acquittement, on annule a routine programmée
                    // dans deux secondes mise en place ci-dessus.
                    handler.removeCallbacksAndMessages(null);

                    // on devient un bomber
                    updateStatut(me.getUid(), Profil.BombStatut.BOMBER);
                    updateOther(me.getUid(), cible);
                    // et on appelle le callback pour prévenir l'activité
                    callback.userBomber();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        mDatabase.addValueEventListener(postListener);

    }

    // on supprime la bombe (car elle a explosé)
    public void removeBomb() {
        updateStatut(me.getUid(), Profil.BombStatut.IDLE);
        updateOther(me.getUid(), null);
    }

    // on renvoie la bombe à l'expéditeur
    public void renvoyerBomb() {
        bombedTime = System.currentTimeMillis();
        if (me == null || other.getUid() == null) return;
        // notre attaquant devient "bombé"
        updateStatut(other.getUid(), Profil.BombStatut.BOMBED);
        // on passe du statut "bombed" vers "bomber"
        updateStatut(me.getUid(), Profil.BombStatut.BOMBER);
    }
}
