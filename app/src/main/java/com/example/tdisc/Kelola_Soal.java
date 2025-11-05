package com.example.tdisc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kelola_Soal extends AppCompatActivity {

    private static final long DOUBLE_BACK_PRESS_DURATION = 2000; //Durasi Klik Tombol Back di Hp
    private long backPressedTime;//Mendeteksi Tombol Back di Hp Ditekan Pada Durasi yang Ditentukan
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    private List<Question> questions;
    GoogleSignInClient googleAuth;
    Dialog loadingDialog, inputSoalDialog, logoutDialog;
    Toolbar toolbar;
    EditText soal, pgA, pgB, pgC, pgD;
    TextView jumlahSoal, jumlahA, jumlahB, jumlahC, jumlahD;
    Button simpan, cancel, ya, tidak;
    Spinner dominance, influence, steadiness, compliance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelola_soal);

        //Set Toolbar
        toolbar = findViewById(R.id.toolbar_kelolaSoal);
        setSupportActionBar(toolbar);

        //Loading Dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_bar);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);

        //User SigIn
        googleAuth = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        //Array List Soal
        questions = new ArrayList<>();

        //Recyclerview Soal
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Hitung Jumlah Soal
        DatabaseReference dbSoal = FirebaseDatabase.getInstance().getReference().child("BankSoal");
        dbSoal.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long jumlahSoal = snapshot.getChildrenCount();

                toolbar.setTitle("Jumlah Soal (" +jumlahSoal + ")");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Kelola_Soal.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });

        tampilSoal();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_kelolasoal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item. getItemId()) {
            case R.id.btn_tambahSoal:
                inputSoalDialog = new Dialog(this);
                inputSoalDialog.setContentView(R.layout.dialog_input_soal);
                inputSoalDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                inputSoalDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                inputSoalDialog.setCanceledOnTouchOutside(false);
                inputSoalDialog.show();

                //Inisiasi Item Dialog Input Soal
                soal = inputSoalDialog.findViewById(R.id.ed_soal);
                pgA = inputSoalDialog.findViewById(R.id.ed_A);
                pgB = inputSoalDialog.findViewById(R.id.ed_B);
                pgC = inputSoalDialog.findViewById(R.id.ed_C);
                pgD = inputSoalDialog.findViewById(R.id.ed_D);
                jumlahSoal = inputSoalDialog.findViewById(R.id.jumlahSoal);
                jumlahA = inputSoalDialog.findViewById(R.id.jumlah_A);
                jumlahB = inputSoalDialog.findViewById(R.id.jumlah_B);
                jumlahC = inputSoalDialog.findViewById(R.id.jumlah_C);
                jumlahD = inputSoalDialog.findViewById(R.id.jumlah_D);
                dominance = inputSoalDialog.findViewById(R.id.sp_dominance);
                influence = inputSoalDialog.findViewById(R.id.sp_influence);
                steadiness = inputSoalDialog.findViewById(R.id.sp_steadiness);
                compliance = inputSoalDialog.findViewById(R.id.sp_compliance);
                simpan = inputSoalDialog.findViewById(R.id.btn_simpan);
                cancel = inputSoalDialog.findViewById(R.id.btn_cancel);

                //Hitung Jumlah Karakter Soal
                soal.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        hitungSoal(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }

                    private void hitungSoal(String panjangSoal) {
                        int jumlahKarakterSoal = panjangSoal.length();
                        jumlahSoal.setText(String.valueOf(jumlahKarakterSoal + "/500"));
                        if (jumlahKarakterSoal > 500) {
                            soal.setError("Nama Maksimal 500 Karakter");
                        } else if (jumlahKarakterSoal < 1) {
                            soal.setError("Tidak Boleh Kosong");
                        } else {
                            soal.setError(null);
                        }
                    }
                });

                //Hitung Jumlah Karakter Option A
                pgA.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        hitungA(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }

                    private void hitungA(String panjangA) {
                        int jumlahKarakterA = panjangA.length();
                        jumlahA.setText(String.valueOf(jumlahKarakterA + "/450"));
                        if (jumlahKarakterA > 450) {
                            pgA.setError("Nama Maksimal 450 Karakter");
                        } else if (jumlahKarakterA < 1) {
                            pgA.setError("Tidak Boleh Kosong");
                        } else {
                            pgA.setError(null);
                        }
                    }
                });

                //Hitung Jumlah Karakter Option B
                pgB.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        hitungB(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }

                    private void hitungB(String panjangB) {
                        int jumlahKarakterB = panjangB.length();
                        jumlahB.setText(String.valueOf(jumlahKarakterB + "/450"));
                        if (jumlahKarakterB > 450) {
                            pgB.setError("Nama Maksimal 450 Karakter");
                        } else if (jumlahKarakterB < 1) {
                            pgB.setError("Tidak Boleh Kosong");
                        } else {
                            pgB.setError(null);
                        }
                    }
                });

                //Hitung Jumlah Karakter Option C
                pgC.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        hitungC(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }

                    private void hitungC(String panjangC) {
                        int jumlahKarakterC = panjangC.length();
                        jumlahC.setText(String.valueOf(jumlahKarakterC + "/450"));
                        if (jumlahKarakterC > 450) {
                            pgC.setError("Nama Maksimal 450 Karakter");
                        } else if (jumlahKarakterC < 1) {
                            pgC.setError("Tidak Boleh Kosong");
                        } else {
                            pgC.setError(null);
                        }
                    }
                });

                //Hitung Jumlah Karakter Option D
                pgD.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        hitungD(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }

                    private void hitungD(String panjangD) {
                        int jumlahKarakterD = panjangD.length();
                        jumlahD.setText(String.valueOf(jumlahKarakterD + "/450"));
                        if (jumlahKarakterD > 450) {
                            pgD.setError("Nama Maksimal 450 Karakter");
                        } else if (jumlahKarakterD < 1) {
                            pgD.setError("Tidak Boleh Kosong");
                        } else {
                            pgD.setError(null);
                        }
                    }
                });

                //Data Spinner
                String[] spinnerOptions = {"A", "B", "C", "D"};
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerOptions);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dominance.setAdapter(spinnerAdapter);
                influence.setAdapter(spinnerAdapter);
                steadiness.setAdapter(spinnerAdapter);
                compliance.setAdapter(spinnerAdapter);

                //Button Simpan
                simpan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Get Semua Data Input Ke String
                        String editedQuestion = soal.getText().toString();
                        int karakterSoal = editedQuestion.length();
                        String editedOptionA = pgA.getText().toString();
                        int karakterOptionA = editedOptionA.length();
                        String editedOptionB = pgB.getText().toString();
                        int karakterOptionB = editedOptionB.length();
                        String editedOptionC = pgC.getText().toString();
                        int karakterOptionC = editedOptionC.length();
                        String editedOptionD = pgD.getText().toString();
                        int karakterOptionD = editedOptionD.length();
                        String selectedDominance = dominance.getSelectedItem().toString();
                        String selectedInfluence = influence.getSelectedItem().toString();
                        String selectedSteadiness = steadiness.getSelectedItem().toString();
                        String selectedCompliance = compliance.getSelectedItem().toString();

                        if (editedQuestion.isEmpty()) {
                            soal.setError("Tidak Boleh Kosong");
                            soal.requestFocus();
                        } else if (editedOptionA.isEmpty()) {
                            pgA.setError("Tidak Boleh Kosong");
                            pgA.requestFocus();
                        } else if (editedOptionB.isEmpty()) {
                            pgB.setError("Tidak Boleh Kosong");
                            pgB.requestFocus();
                        } else if (editedOptionC.isEmpty()) {
                            pgC.setError("Tidak Boleh Kosong");
                            pgC.requestFocus();
                        } else if (editedOptionD.isEmpty()) {
                            pgD.setError("Tidak Boleh Kosong");
                            pgD.requestFocus();
                        } else if (karakterSoal > 500) {
                            soal.setError("Soal Maksimal 500 Karakter");
                            soal.requestFocus();
                        } else if (karakterOptionA > 450) {
                            pgA.setError("Maksimal 450 Karakter");
                            pgA.requestFocus();
                        } else if (karakterOptionB > 450) {
                            pgB.setError("Maksimal 450 Karakter");
                            pgB.requestFocus();
                        } else if (karakterOptionC > 450) {
                            pgC.setError("Maksimal 450 Karakter");
                            pgC.requestFocus();
                        } else if (karakterOptionD > 450) {
                            pgD.setError("Maksimal 450 Karakter");
                            pgD.requestFocus();
                        } else if (selectedDominance.equals(selectedInfluence) ||
                                selectedDominance.equals(selectedSteadiness) ||
                                selectedDominance.equals(selectedCompliance) ||
                                selectedInfluence.equals(selectedSteadiness) ||
                                selectedInfluence.equals(selectedCompliance) ||
                                selectedSteadiness.equals(selectedCompliance)) {
                            Toast.makeText(Kelola_Soal.this, "Cek Kunci Jawaban Kembali", Toast.LENGTH_SHORT).show();
                        } else {
                            editedOptionA = editedOptionA.replace("A. ", "");
                            editedOptionB = editedOptionB.replace("B. ", "");
                            editedOptionC = editedOptionC.replace("C. ", "");
                            editedOptionD = editedOptionD.replace("D. ", "");

                            //Get Semua Data Soal
                            Map<String, Object> soalBaru = new HashMap<>();
                            soalBaru.put("Soal", editedQuestion);
                            soalBaru.put("Option A", editedOptionA);
                            soalBaru.put("Option B", editedOptionB);
                            soalBaru.put("Option C", editedOptionC);
                            soalBaru.put("Option D", editedOptionD);
                            soalBaru.put("KunciJawaban/Dominance", selectedDominance);
                            soalBaru.put("KunciJawaban/Influence", selectedInfluence);
                            soalBaru.put("KunciJawaban/Steadiness", selectedSteadiness);
                            soalBaru.put("KunciJawaban/Compliance", selectedCompliance);

                            //Get Total Jumlah Soal
                            DatabaseReference dbSoal = FirebaseDatabase.getInstance().getReference().child("BankSoal");
                            dbSoal.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    long lastKey = 0;

                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        lastKey = Long.parseLong(child.getKey());
                                    }
                                    long newKey = lastKey + 1;

                                    dbSoal.child(String.valueOf(newKey)).updateChildren(soalBaru).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(Kelola_Soal.this, "Tambah Soal Berhasil", Toast.LENGTH_SHORT).show();
                                            inputSoalDialog.dismiss();
                                            tampilSoal();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(Kelola_Soal.this, "Tambah Soal Gagal", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(Kelola_Soal.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

                //Button Cancel
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inputSoalDialog.dismiss();
                    }
                });
                break;
            case R.id.btn_logout:
                logoutDialog = new Dialog(this);
                logoutDialog.setContentView(R.layout.logout_dialogbox);
                logoutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                logoutDialog.setCanceledOnTouchOutside(false);
                logoutDialog.show();

                //Inisiasi Item Dialog Log Out
                ya = logoutDialog.findViewById(R.id.btnYa);
                tidak = logoutDialog.findViewById(R.id.btnTidak);

                //Button Ya
                ya.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        logoutDialog.dismiss();
                        FirebaseAuth.getInstance().signOut();
                        googleAuth.signOut();
                        startActivity(new Intent(Kelola_Soal.this, MainActivity.class));
                        finish();
                    }
                });

                //Button Tidak
                tidak.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        logoutDialog.dismiss();
                    }
                });
                break;
        }
        return true;
    }

    private void tampilSoal() {
        loadingDialog.show();
        DatabaseReference dbSoal = FirebaseDatabase.getInstance().getReference().child("BankSoal");
        dbSoal.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questions.clear();
                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    String questionText = questionSnapshot.child("Soal").getValue(String.class);
                    String optionA = questionSnapshot.child("Option A").getValue(String.class);
                    String optionB = questionSnapshot.child("Option B").getValue(String.class);
                    String optionC = questionSnapshot.child("Option C").getValue(String.class);
                    String optionD = questionSnapshot.child("Option D").getValue(String.class);
                    String dominance = questionSnapshot.child("KunciJawaban").child("Dominance").getValue(String.class);
                    String influence = questionSnapshot.child("KunciJawaban").child("Influence").getValue(String.class);
                    String steadiness = questionSnapshot.child("KunciJawaban").child("Steadiness").getValue(String.class);
                    String compliance = questionSnapshot.child("KunciJawaban").child("Compliance").getValue(String.class);

                    Question question = new Question(questionText, optionA, optionB, optionC, optionD, dominance.charAt(0), influence.charAt(0), compliance.charAt(0), steadiness.charAt(0));
                    question.setKey(questionSnapshot.getKey());
                    questions.add(question);
                }

                if (recyclerViewAdapter == null) {
                    recyclerViewAdapter = new RecyclerViewAdapter(questions, Kelola_Soal.this);
                    recyclerView.setAdapter(recyclerViewAdapter);
                } else {
                    recyclerViewAdapter.notifyDataSetChanged();
                }

                loadingDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Kelola_Soal.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
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