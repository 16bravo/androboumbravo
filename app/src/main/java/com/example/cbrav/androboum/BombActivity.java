package com.example.cbrav.androboum;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.RecoverySystem;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class BombActivity extends AppCompatActivity {

    Context context;
    CountDownTimer timer;
    int progress = 0;
    final int initTime = 10000;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AndroBoumApp.getBomber().setCallback(null);
        Log.v("AndroBoumApp","On destroy");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bomb);

        context = this;
        AndroBoumApp.setContext(this);

        // on obtient l'intent utilisé pour l'appel
        Intent intent = getIntent();

        // vient-on d'une notification ?
        boolean notification = intent.getBooleanExtra("notification",false);
        if (notification) {
            if (AndroBoumApp.getBomber().timer != null) AndroBoumApp.getBomber().timer.cancel();
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final TextView textView = (TextView) findViewById(R.id.cible);
        final TextView textViewOther = (TextView) findViewById(R.id.textViewOther);
        final Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AndroBoumApp.getBomber().renvoyerBomb();
            }
        });


        AndroBoumApp.setCallback(new Bomber.BomberInterface() {
            @Override
            // on est ciblé par une bombe
            public void userBombed() {
                // on annule le timer en cours
                if (timer != null) timer.cancel();

                // on affiche le bouton pour renvoyer la bombe
                button.setVisibility(View.VISIBLE);

                // on note "attaquant" dans l'intitulé
                textViewOther.setText(context.getResources().getString(R.string.source));

                // on note l'email dans le cible
                textView.setText(AndroBoumApp.getBomber().getOther().getEmail());

                // on calcule le temps qui reste
                long timeleft = Bomber.timetoboum - (System.currentTimeMillis() - Bomber.bombedTime);

                // on initialise la barre de progression
                progressBar.setMax((int) (timeleft / 1000));
                progressBar.setProgress(0);
                progress = 0;
                // et on déclenche le countdown
                timer=new CountDownTimer(timeleft,1000) {


                    @Override
                    public void onTick(long millisUntilFinished) {
                        progress++;
                        progressBar.setProgress(progress);
                    }

                    @Override
                    public void onFinish() {
                        // la bombe a explosé chez nous
                        progress++;
                        progressBar.setProgress(progress);
                        Toast toast = Toast.makeText(context, context.getResources().getString(R.string.lost), Toast.LENGTH_SHORT);
                        toast.show();
                        // on perd un point
                        AndroBoumApp.getBomber().addToScore(-1);
                        AndroBoumApp.getBomber().removeBomb();
                        // on enlève le bouton pour renvoyer la bombe
                        button.setVisibility(View.INVISIBLE);
                    }
                };
                timer.start();

            }

            @Override
            // on a posé une bombe chez quelqu'un
            public void userBomber() {
                // on annule le timer en cours
                if (timer != null) timer.cancel();

                // on cache le bouton pour renvoyer la bombe
                button.setVisibility(View.INVISIBLE);

                // on note "cible" dans l'intitulé
                textViewOther.setText(context.getResources().getString(R.string.target));
                // on note l'email dans le cible
                textView.setText(AndroBoumApp.getBomber().getOther().getEmail());

                // on calcule le temps qui reste
                long timeleft = Bomber.timetoboum - (System.currentTimeMillis() - Bomber.bombedTime);

                // On initilise la barre de progression et on démarre le compte à rebours...
                progressBar.setMax((int) (timeleft/1000));
                progressBar.setProgress(0);
                progress = 0;

                timer=new CountDownTimer(timeleft,1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        progress++;
                        progressBar.setProgress(progress);

                    }

                    @Override
                    public void onFinish() {
                        progress++;
                        progressBar.setProgress(progress);
                        // la bombe a explosé chez la cible
                        Toast toast = Toast.makeText(context, context.getResources().getString(R.string.win), Toast.LENGTH_SHORT);
                        toast.show();
                        AndroBoumApp.getBomber().removeBomb();
                        // on gagne un point
                        AndroBoumApp.getBomber().addToScore(1);
                    }
                };
                timer.start();

            }
        });
    }
}
