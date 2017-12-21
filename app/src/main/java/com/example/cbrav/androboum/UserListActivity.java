package com.example.cbrav.androboum;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    // variable globale pour spécifier si on filtre ou pas
    boolean filterConnected = false;

    final List<Profil> userList = new ArrayList<>();
    MyArrayAdapter adapter;

    final Context context = this;

    private class MyArrayAdapter extends ArrayAdapter<Profil> {
        List<Profil> liste, origListe;
        FirebaseStorage storage = FirebaseStorage.getInstance();

        private MyArrayAdapter(Context context, int resource, List<Profil> liste) {
            super(context, resource, liste);
            this.liste = liste;
// on fait une copie de la liste dans une autre variable
            this.origListe = this.liste;
        }

        @Override
        public void notifyDataSetChanged() {
            if (filterConnected) {
// on alloue une nouvelle liste et on la remplit uniquement
// avec les utilisateurs connectés.
                liste = new ArrayList<>();
                for (Profil p : origListe) {
                    if (p.isConnected()) liste.add(p);
                }
// sinon on reprend la liste complète que l'on avait
// sauvegardé dans la variable origListe.
            } else liste = origListe;
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // on va chercher le bon profil dans la liste
            Profil p = liste.get(position);
            // on instancie le layout sous la forme d'un objet de type View
            View layout = View.inflate(getContext(), R.layout.profil_list_item, null);
            // on va chercher les trois composants du layout
            ImageView imageProfilView = (ImageView) layout.findViewById(R.id.imageView);
            TextView textView = (TextView) layout.findViewById(R.id.emailAutre);
            ImageView imageConnectedView = (ImageView) layout.findViewById(R.id.imageView2);
            TextView textViewScore = (TextView) layout.findViewById(R.id.score);
            // on télécharge dans le premier composant l'image du profil
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference photoRef = storage.getReference().child(p.getEmail() + "/photo.jpg");
            if (photoRef != null) {
                Glide.with(getContext()).using(new FirebaseImageLoader())
                        .load(photoRef)
                        .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.com_facebook_profile_picture_blank_square)
                        .into(imageProfilView);
            }
            // on positionne le email dans le TextView
            textView.setText(p.getEmail());

            //et le score
            textViewScore.setText(String.valueOf(p.getScore()));

            // si l'utilisateur n'est pas connecté, on rend invisible le troisième
            // composant
            if (!p.isConnected) {
                imageConnectedView.setVisibility(View.INVISIBLE);
            }
            // on retourne le layout
            return layout;
        }

        @Override
        public int getCount() {
            return liste.size();
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        Toolbar myToolbar2 = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(myToolbar2);
        ListView listeView = (ListView) findViewById(R.id.liste);
        adapter = new
                MyArrayAdapter(this, android.R.layout.simple_list_item_1, userList);
        listeView.setAdapter(adapter);
        DatabaseReference mDatabase =
                FirebaseDatabase.getInstance().getReference().child("Users");
        // on sauve le contexte dans une constante pour pourvoir s'en servir
        // dans le callback.
        listeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int
                    position, long l) {
                // code exécuté quand on clique sur un des items de la liste.
                // le paramètre position contient le numéro de l'item cliqué.
                Intent intent = new Intent(context,OtherUserActivity.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    userList.add(child.getValue(Profil.class));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.v("AndroBoum", "loadPost:onCancelled", databaseError.toException());
            }


        };

        mDatabase.addValueEventListener(postListener);

    }

    private void showFilterDialog() {
        // on crée un nouvel objet de type boite de dialogue
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // on lui affecte un titre, et une liste de choix possibles
        builder.setTitle(R.string.filter_dialog_title)
                .setSingleChoiceItems(R.array.users_filter, filterConnected ? 0 : 1, new
                        DialogInterface.OnClickListener() {
                            @Override
                            // méthode appelée quand l'utilisateur fait un choix
                            // i contient le numéro du choix
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // si le premier item a été choisie, on filtre sur
                                // uniquement les utilisateurs connectés.
                                filterConnected = (i == 0);
                                // et on signale a l'adaptateur qu'il faut remettre
                                // la liste à jour.

                                adapter.notifyDataSetChanged();
                            }
                        })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    // on a cliqué sur "ok", on ne fait rien.
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        // on crée la boite
        AlertDialog dialog = builder.create();
        // et on l'affiche
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actions2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action2_settings:
                showFilterDialog();
                return true;
            default:
                /// aucune action reconnue
                return super.onOptionsItemSelected(item);
        }
    }


}