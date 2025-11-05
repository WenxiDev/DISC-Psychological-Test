package com.example.tdisc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreen extends AppCompatActivity {

    private static final long DOUBLE_BACK_PRESS_DURATION = 2000; //Durasi Klik Tombol Back di Hp
    private long backPressedTime;//Mendeteksi Tombol Back di Hp Ditekan Pada Durasi yang Ditentukan
    FirebaseAuth firebaseAuth;
    Boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Authentifikasi Email & Password
        firebaseAuth = FirebaseAuth.getInstance();

        //Hide Status Bar
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        //Set Color Status Bar
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(Color.parseColor("#EBFCFB"));

        /*
        DatabaseReference dbAdmin = FirebaseDatabase.getInstance().getReference("DatabaseAdmin");
        dbAdmin.child("Admin1").setValue("wenxiwen60@gmail.com");
        dbAdmin.child("Admin2").setValue("ditypsikologi@gmail.com");
         */
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Get UID Email
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String loginEmail = currentUser.getEmail();
            if (isNetworkAvailable()) {
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
                                    startActivity(new Intent(SplashScreen.this, Kelola_Soal.class));
                                    finish();
                                    break;
                                }
                            }
                            if (!isAdmin) {
                                String UID = currentUser.getUid();
                                //Read Realtime Database User
                                DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);
                                dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            //Read Realtime Database User
                                            DatabaseReference dbLogin = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);
                                            dbLogin.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        startActivity(new Intent(SplashScreen.this, Menu_Utama.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(SplashScreen.this, "Database Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                                        finish();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Toast.makeText(SplashScreen.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            Toast.makeText(SplashScreen.this, "Database Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SplashScreen.this, MainActivity.class));
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(SplashScreen.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SplashScreen.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                bottomDialog();
            }
        } else {
            if (isNetworkAvailable()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                        finish();
                    }
                }, 1500);
            } else {
                bottomDialog();
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void bottomDialog() {
        Button cobalagi;
        final Dialog bottomDialog = new Dialog(this);
        bottomDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottomDialog.setContentView(R.layout.bottomsheet_dialog);

        bottomDialog.show();
        bottomDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        bottomDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bottomDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.setCancelable(false);
        bottomDialog.setCanceledOnTouchOutside(false);

        //Button Coba Lagi
        cobalagi = bottomDialog.findViewById(R.id.cobalagi);
        cobalagi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialog.dismiss();
                if (isNetworkAvailable()) {
                    bottomDialog.dismiss();
                    onStart();
                } else {
                    bottomDialog.show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + DOUBLE_BACK_PRESS_DURATION > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(this, "Ketuk Lagi Untuk Keluar", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();//Mendeteksi Tombol Back Apakah Ditekan Kembali Pada Waktu yang Telah Ditentukan
    }
}