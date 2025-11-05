package com.example.tdisc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final long DOUBLE_BACK_PRESS_DURATION = 2000; //Durasi Klik Tombol Back di Hp
    private long backPressedTime;//Mendeteksi Tombol Back di Hp Ditekan Pada Durasi yang Ditentukan
    Button loginGoogle;
    FirebaseAuth firebaseAuth;
    Dialog loadingDialog;
    GoogleSignInClient googleAuth;
    Boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Loading Dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_bar);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);

        //Authentifikasi Email & Password
        firebaseAuth = FirebaseAuth.getInstance();

        //Authentifikasi Google
        loginGoogle = findViewById(R.id.loginGoogle);
        loginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.show();
                signInWithGoogle();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1013072378165-g9p3rjbv553v3klndcif3839baqe9j2o.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleAuth = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleAuth.getSignInIntent();
        startActivityForResult(signInIntent, 1001);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("Google Sign In", "firebaseAuthWithGoogle: " + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("Google Sign In", "Google Sign In Failed", e);
                loadingDialog.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        createDatabase();
                    } else {
                        Toast.makeText(this, "Login Gagal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void createDatabase() {
        //Get UID User From Email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();
        String loginEmail = currentUser.getEmail();

        //Cek Akun Admin
        DatabaseReference dbAdmin = FirebaseDatabase.getInstance().getReference("DatabaseAdmin");
        dbAdmin.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot adminSnapshot : snapshot.getChildren()) {
                        String adminEmail = adminSnapshot.getValue(String.class);

                        if (adminEmail.equals(loginEmail)) {
                            isAdmin = true;
                            loadingDialog.dismiss();
                            startActivity(new Intent(MainActivity.this, Kelola_Soal.class));
                            finish();
                            break;
                        }
                    }
                    if (!isAdmin) {
                        //Read Realtime Database
                        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser");
                        dbUser.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    //Create Realtime Database
                                    DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);
                                    dbUser.child("Nama").setValue("null");
                                    dbUser.child("TanggalLahir").setValue("null");
                                    dbUser.child("Gender").setValue("null");
                                    dbUser.child("JumlahTes").setValue(0);
                                    dbUser.child("UrlFoto").setValue("null");
                                    dbUser.child("HasilTes").child("Tes1").child("Dominance").setValue(0);
                                    dbUser.child("HasilTes").child("Tes1").child("Influence").setValue(0);
                                    dbUser.child("HasilTes").child("Tes1").child("Compliance").setValue(0);
                                    dbUser.child("HasilTes").child("Tes1").child("Steadiness").setValue(0);
                                    dbUser.child("HasilTes").child("Tes1").child("TanggalTes").setValue("null");
                                    loadingDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Login Berhasil", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, Menu_Utama.class));
                                    finish();
                                } else {
                                    loadingDialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Login Berhasil", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, Menu_Utama.class));
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + DOUBLE_BACK_PRESS_DURATION > System.currentTimeMillis()) {
            super.onBackPressed();
            finishAffinity();
        } else {
            Toast.makeText(this, "Ketuk Lagi Untuk Keluar", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();//Mendeteksi Tombol Back Apakah Ditekan Kembali Pada Waktu yang Telah Ditentukan
    }
}