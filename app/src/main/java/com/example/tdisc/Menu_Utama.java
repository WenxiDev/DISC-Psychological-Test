package com.example.tdisc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class Menu_Utama extends AppCompatActivity {

    private static final long DOUBLE_BACK_PRESS_DURATION = 2000; //Durasi Klik Tombol Back di Hp
    private long backPressedTime;//Mendeteksi Tombol Back di Hp Ditekan Pada Durasi yang Ditentukan
    CircleImageView fotoProfile;
    PieChart pieChart;
    TextView greeting, username, hint;
    Button mulaiTes, detailTes, mengerti;
    Dialog loadingDialog, tutorialDialog;
    //Parameter LCG
    private long LCG_A;
    private long LCG_C;
    private long LCG_M;
    private long seed = System.currentTimeMillis();
    DatabaseReference dbSoal = FirebaseDatabase.getInstance().getReference().child("BankSoal");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_utama);

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

        //Loading Dialog
        tutorialDialog = new Dialog(this);
        tutorialDialog.setContentView(R.layout.dialog_tutorial);
        tutorialDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        tutorialDialog.setCancelable(false);
        tutorialDialog.setCanceledOnTouchOutside(false);
        Window window = tutorialDialog.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        //Histori Tes
        historiTes();

        //Get UID User From Email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();

        //Hint Jika Data Tes Tidak Ada
        hint = findViewById(R.id.hint);

        //Realtime Database
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);

        //Nama User
        FirebaseUser loginUsername = FirebaseAuth.getInstance().getCurrentUser();
        String email = loginUsername.getEmail();
        String namaEmail = email.substring(0, email.indexOf("@"));
        username = findViewById(R.id.username);
        dbUser.child("Nama").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String namaSnapshot = snapshot.getValue(String.class);
                    if (namaSnapshot.equals("null")) {
                        username.setText("Hai "+namaEmail);
                    } else {
                        username.setText("Hai "+namaSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Menu_Utama.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });

        //Kata Ucapan
        Calendar calendar = Calendar.getInstance();
        int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        greeting = findViewById(R.id.greeting);
        if (timeOfDay >= 5 && timeOfDay < 12) {
            greeting.setText("Selamat Pagi");
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            greeting.setText("Selamat Siang");
        } else if (timeOfDay >= 16 && timeOfDay < 18) {
            greeting.setText("Selamat Sore");
        } else {
            greeting.setText("Selamat Malam");
        }

        //Load Foto Profile Dengan Download URL
        fotoProfile = findViewById(R.id.fotoProfil);
        dbUser.child("UrlFoto").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fotoUrl = snapshot.getValue(String.class);
                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        Picasso.get().load(fotoUrl).into(fotoProfile);
                    }
                    if (fotoUrl.equals("null")) {
                        fotoProfile.setImageResource(R.drawable.no_profil);
                    }
                } else {
                    Toast.makeText(Menu_Utama.this, "Database Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Menu_Utama.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });

        //Show dialog tutorial untuk pengguna awal
        SharedPreferences sharedPreferences = getSharedPreferences("TutorialPrefs", MODE_PRIVATE);
        boolean seenTutorial = sharedPreferences.getBoolean("hasSeenTutorial", false);
        if (!seenTutorial) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tutorialDialog.show();
                }
            }, 3000);
        }

        //Button Tutorial
        fotoProfile = tutorialDialog.findViewById(R.id.fotoProfil);
        fotoProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor keterangan = sharedPreferences.edit();
                keterangan.putBoolean("hasSeenTutorial", true);
                keterangan.apply();
                tutorialDialog.dismiss();
                startActivity(new Intent(Menu_Utama.this, Profile.class));
            }
        });
        mengerti = tutorialDialog.findViewById(R.id.btnMengerti);
        mengerti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor keterangan = sharedPreferences.edit();
                keterangan.putBoolean("hasSeenTutorial", true);
                keterangan.apply();
                tutorialDialog.dismiss();
            }
        });

        //Button Profile
        fotoProfile = findViewById(R.id.fotoProfil);
        fotoProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Menu_Utama.this, Profile.class));
            }
        });

        //Jumlah Tes
        dbUser.child("JumlahTes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer jumlahTes = snapshot.getValue(Integer.class);

                    if (jumlahTes < 1) {
                        hint.setVisibility(View.VISIBLE);
                    } else {
                        //Data PieChart
                        dbUser.child("HasilTes").child("Tes"+jumlahTes).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    float dominanceValue = snapshot.child("Dominance").getValue(Float.class);
                                    float influenceValue = snapshot.child("Influence").getValue(Float.class);
                                    float complianceValue = snapshot.child("Compliance").getValue(Float.class);
                                    float steadinessValue = snapshot.child("Steadiness").getValue(Float.class);

                                    pieChartGrafik(dominanceValue, influenceValue, complianceValue, steadinessValue);

                                    //Button Lihat Detail Tes
                                    detailTes = findViewById(R.id.detailTes);
                                    detailTes.setVisibility(View.VISIBLE);
                                    detailTes.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(Menu_Utama.this, Hasil_Test.class);
                                            intent.putExtra("hasilTes", jumlahTes);
                                            startActivity(intent);
                                        }
                                    });
                                } else {
                                    Toast.makeText(Menu_Utama.this, "Database Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(Menu_Utama.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(Menu_Utama.this, "Database Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Menu_Utama.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });

        //Button Mulai Tes
        mulaiTes = findViewById(R.id.mulaiTes);
        mulaiTes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Create Paket Soal
                DatabaseReference paketSoal = FirebaseDatabase.getInstance().getReference().child("DatabaseUser").child(UID).child("PaketSoal");
                paketSoal.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            loadingDialog.show();
                            //Pengacakan LCG
                            determineLCG();
                        } else {
                            bottomDialog();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Menu_Utama.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void pieChartGrafik(float dominanceValue, float influenceValue, float complianceValue, float steadinessValue) {

        //Value Grafik
        pieChart = findViewById(R.id.pieChart);
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(dominanceValue, "Dominance"));
        entries.add(new PieEntry(influenceValue, "Influence"));
        entries.add(new PieEntry(complianceValue, "Compliance "));
        entries.add(new PieEntry(steadinessValue, "Steadiness"));

        //Custom Color
        int[] customColors = {Color.rgb(231, 33, 46), Color.rgb(247, 198, 69), Color.rgb(1, 115, 188), Color.rgb(81, 191, 166)};
        List<Integer> colors = new ArrayList<>();
        for (int color : customColors) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(15f);

        PieData pieData = new PieData(dataSet);

        hint.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
        pieData.setValueFormatter(new PercentFormatter());
        pieChart.setData(pieData);
        pieChart.invalidate();

        //Hilangkan Teks Deskripsi
        pieChart.getDescription().setEnabled(false);

        ///Set Radius Tengah PieChart
        float transparentCircleRadius = 40f;
        float holeRadius = 35f;
        pieChart.setHoleRadius(holeRadius);
        pieChart.setTransparentCircleRadius(transparentCircleRadius);

        // Disable Legend
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        //Animasi Tampil Pie Chart
        pieChart.animateXY(550, 550);
    }

    private class PercentFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            //Format Koma Belakang + Symbol %
            return String.valueOf(value).replaceAll("\\.?0*$", "") + "%";
        }
    }

    private void bottomDialog() {
        Button close, lanjutkan;
        BottomSheetDialog bottomDialogMulai = new BottomSheetDialog(Menu_Utama.this, R.style.DialogAnimation);
        bottomDialogMulai.setContentView(R.layout.bottomsheet_mulaitest);
        bottomDialogMulai.setCanceledOnTouchOutside(true);

        //Set Background Transparant
        bottomDialogMulai.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //Set Animasi Pop Up
        bottomDialogMulai.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        bottomDialogMulai.show();

        //Button Close
        close = bottomDialogMulai.findViewById(R.id.closeDialog);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialogMulai.dismiss();
            }
        });

        //Button Lanjutkan Tes
        lanjutkan = bottomDialogMulai.findViewById(R.id.lanjutkanTes);
        lanjutkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomDialogMulai.dismiss();
                startActivity(new Intent(Menu_Utama.this, Menu_Test.class));
                finish();
            }
        });
    }

    private void determineLCG() {
        dbSoal.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                LCG_M = snapshot.getChildrenCount();

                //Determine LCG_C
                long candidate_C = 2;

                while (true) {
                    if (gcd(candidate_C, LCG_M) == 1) {
                        LCG_C = candidate_C;
                        break;
                    }
                    candidate_C++;
                }

                //Determine LCG_A
                long candidate_A = 2;

                //Temukan semua faktor prima dari LCG_M
                Set<Long> faktorPrima = new HashSet<>();
                for (long i = 2; i <= LCG_M; i++) {
                    while (LCG_M % i == 0) {
                        faktorPrima.add(i);
                        LCG_M /= i;
                    }
                }

                while (true) {
                    boolean valid = true;
                    for (long factor : faktorPrima) {
                        if ((candidate_A - 1) % factor != 0) {
                            valid = false;
                            break;
                        }
                    }
                    if (valid) {
                        LCG_A = candidate_A;
                        break;
                    }
                    candidate_A++;
                }
                LCG_M = snapshot.getChildrenCount();
                generateRandomNumberInRange(LCG_M, LCG_A, LCG_C);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                return;
            }
        });
    }

    //Algoritma Euclidean untuk menghitung Faktor Persekutuan Terbesar (FPB)
    private long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private void generateRandomNumberInRange(long LCG_M,long LCG_A, long LCG_C) {
        System.out.println("LCG_M "+LCG_M);
        System.out.println("LCG_A "+LCG_A);
        System.out.println("LCG_C "+LCG_C);
        System.out.println("Seed Awal"+ ":"+seed);

        for (int i = 1; i <= 40; i++) {
            long randomNumber;

            randomNumber = (LCG_A * seed + LCG_C) % LCG_M;

            //Memastikan Hasil LCG Positif
            randomNumber = Math.abs(randomNumber);
            getSoal(randomNumber, i);
            seed = randomNumber;

            System.out.println("Random Number"+"Ke"+i+ ":"+randomNumber);

            if (i >= 40) {
                loadingDialog.dismiss();
                bottomDialog();
            }
        }
    }

    private void getSoal(long randomNumber, int i) {
        //Get UID User From Email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();

        dbSoal.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int indexSoal = 0;
                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    if (indexSoal == randomNumber) {
                        String questionText = questionSnapshot.child("Soal").getValue(String.class);
                        String optionA = questionSnapshot.child("Option A").getValue(String.class);
                        String optionB = questionSnapshot.child("Option B").getValue(String.class);
                        String optionC = questionSnapshot.child("Option C").getValue(String.class);
                        String optionD = questionSnapshot.child("Option D").getValue(String.class);
                        String dominance = questionSnapshot.child("KunciJawaban").child("Dominance").getValue(String.class);
                        String influence = questionSnapshot.child("KunciJawaban").child("Influence").getValue(String.class);
                        String steadiness = questionSnapshot.child("KunciJawaban").child("Steadiness").getValue(String.class);
                        String compliance = questionSnapshot.child("KunciJawaban").child("Compliance").getValue(String.class);

                        DatabaseReference paketSoal = FirebaseDatabase.getInstance().getReference().child("DatabaseUser").child(UID).child("PaketSoal");
                        paketSoal.child(String.valueOf(i)).child("Soal").setValue(questionText);
                        paketSoal.child(String.valueOf(i)).child("Option A").setValue(optionA);
                        paketSoal.child(String.valueOf(i)).child("Option B").setValue(optionB);
                        paketSoal.child(String.valueOf(i)).child("Option C").setValue(optionC);
                        paketSoal.child(String.valueOf(i)).child("Option D").setValue(optionD);
                        paketSoal.child(String.valueOf(i)).child("KunciJawaban").child("Dominance").setValue(dominance);
                        paketSoal.child(String.valueOf(i)).child("KunciJawaban").child("Influence").setValue(influence);
                        paketSoal.child(String.valueOf(i)).child("KunciJawaban").child("Steadiness").setValue(steadiness);
                        paketSoal.child(String.valueOf(i)).child("KunciJawaban").child("Compliance").setValue(compliance);
                        break;
                    }
                    indexSoal++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                return;
            }
        });
    }

    private void historiTes() {
        //Get UID User From Email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();

        //Realtime Database
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);

        dbUser.child("HistoriTes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    dbUser.child("HistoriTes").child("Dominance").setValue(0);
                    dbUser.child("HistoriTes").child("Influence").setValue(0);
                    dbUser.child("HistoriTes").child("Steadiness").setValue(0);
                    dbUser.child("HistoriTes").child("Compliance").setValue(0);
                    dbUser.child("HistoriTes").child("WaktuTes").setValue(2400000);
                    dbUser.child("HistoriTes").child("NomorUrutSoal").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Menu_Utama.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
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