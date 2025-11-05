package com.example.tdisc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Menu_Test extends AppCompatActivity {

    Button back, ya, tidak, lihatHasil;
    Dialog cancelDialog, selesaiDialog;
    TextView timer, keteranganSelesai, soal, urutanSoal;
    RadioButton optionA, optionB, optionC, optionD;
    CountDownTimer countDownTimer;
    private long TOTAL_TIMER;
    private static final long BLINK_TIMER = 1 * 60 * 1000;
    private QuestionBank questionBank;
    MediaPlayer mediaPlayer;
    LottieAnimationView imgKeterangan;
    //Hitung Point
    int dominance, influence, compliance, steadiness;
    Integer totalJawab, jumlahTes;
    double countD, countI, countS, countC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_test);

        //Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Get UID User From Email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();

        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);

        //Set Screen Selalu On
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Get Sound
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.timer_will_end);

        //Cancel Dialog
        cancelDialog = new Dialog(this);
        cancelDialog.setContentView(R.layout.cancel_test_dialogbox);
        cancelDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cancelDialog.setCanceledOnTouchOutside(false);
        cancelDialog.setCancelable(false);

        //Button Ya
        ya = cancelDialog.findViewById(R.id.btnYa);

        //Button Tidak
        tidak = cancelDialog.findViewById(R.id.btnTidak);

        //Selesai Dialog
        selesaiDialog = new Dialog(this);
        selesaiDialog.setContentView(R.layout.test_selesai_dialogbox);
        selesaiDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        selesaiDialog.setCanceledOnTouchOutside(false);
        selesaiDialog.setCancelable(false);

        //Keterangan Selesai
        keteranganSelesai = selesaiDialog.findViewById(R.id.keterangan);

        //Animasi Keterangan Selesai
        imgKeterangan = selesaiDialog.findViewById(R.id.imgKeterangan);

        //Button Lihat Hasil Tes
        lihatHasil = selesaiDialog.findViewById(R.id.cekHasil);
        lihatHasil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get Jumlah Tes
                dbUser.child("JumlahTes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Integer jumlahSnapshot = snapshot.getValue(Integer.class);
                            jumlahTes = jumlahSnapshot + 1;
                            dbUser.child("JumlahTes").setValue(jumlahTes);

                            dbUser.child("HistoriTes").child("NomorUrutSoal").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    totalJawab = snapshot.getValue(Integer.class);

                                    //Hitung Hasil Tes Dominance
                                    dbUser.child("HistoriTes").child("Dominance").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            dominance = snapshot.getValue(Integer.class);
                                            countD = (dominance * 100.0) / totalJawab;
                                            dbUser.child("HasilTes").child("Tes"+jumlahTes).child("Dominance").setValue(countD);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            return;
                                        }
                                    });

                                    //Hitung Hasil Tes Influence
                                    dbUser.child("HistoriTes").child("Influence").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            influence = snapshot.getValue(Integer.class);
                                            countI = (influence * 100.0) / totalJawab;
                                            dbUser.child("HasilTes").child("Tes"+jumlahTes).child("Influence").setValue(countI);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            return;
                                        }
                                    });

                                    //Hitung Hasil Tes Steadiness
                                    dbUser.child("HistoriTes").child("Steadiness").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            steadiness = snapshot.getValue(Integer.class);
                                            countS = (steadiness * 100.0) / totalJawab;
                                            dbUser.child("HasilTes").child("Tes"+jumlahTes).child("Steadiness").setValue(countS);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            return;
                                        }
                                    });

                                    //Hitung Hasil Tes Compliance
                                    dbUser.child("HistoriTes").child("Compliance").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            compliance = snapshot.getValue(Integer.class);
                                            countC = (compliance * 100.0) / totalJawab;
                                            dbUser.child("HasilTes").child("Tes"+jumlahTes).child("Compliance").setValue(countC);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            return;
                                        }
                                    });

                                    //Set Tanggal Tes
                                    final Calendar c = Calendar.getInstance();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                                    String formattedDate = dateFormat.format(c.getTime());
                                    dbUser.child("HasilTes").child("Tes"+jumlahTes).child("TanggalTes").setValue(formattedDate);

                                    //Set Jam Tes
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    String formattedTime = timeFormat.format(c.getTime());
                                    dbUser.child("HasilTes").child("Tes"+jumlahTes).child("JamTes").setValue(formattedTime);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    return;
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Menu_Test.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                    }
                });
                dbUser.child("HistoriTes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {dbUser.child("HistoriTes").child("Dominance").setValue(0);
                        dbUser.child("HistoriTes").child("Influence").setValue(0);
                        dbUser.child("HistoriTes").child("Steadiness").setValue(0);
                        dbUser.child("HistoriTes").child("Compliance").setValue(0);
                        dbUser.child("HistoriTes").child("WaktuTes").setValue(2400000);
                        dbUser.child("HistoriTes").child("NomorUrutSoal").setValue(0);

                        selesaiDialog.dismiss();
                        startActivity(new Intent(Menu_Test.this, Menu_Utama.class));
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Menu_Test.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //Button Kembali
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelDialog.show();

                ya.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cancelDialog.dismiss();
                        mediaPlayer.stop();
                        mediaPlayer = null;
                        //Stop Hitung Mundur
                        countDownTimer.cancel();
                        startActivity(new Intent(Menu_Test.this, Menu_Utama.class));
                        finish();
                    }
                });

                tidak.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cancelDialog.dismiss();
                    }
                });
            }
        });

        //Timer Soal
        timer = findViewById(R.id.timer);
        dbUser.child("HistoriTes").child("WaktuTes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TOTAL_TIMER = snapshot.getValue(Integer.class);

                countDownTimer = new CountDownTimer(TOTAL_TIMER, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        //Update Timer Setiap Detik
                        updateTimerText(millisUntilFinished);

                        if (millisUntilFinished <= BLINK_TIMER) {
                            if (mediaPlayer != null) {
                                mediaPlayer.start();
                            }
                            blinkRedTimer();
                        }
                    }

                    @Override
                    public void onFinish() {
                        mediaPlayer.stop();
                        timer.setText("00:00");
                        //Set Text Keterangan Selesai
                        keteranganSelesai.setText("Waktu Tes Telah Habis");
                        imgKeterangan.setAnimation(R.raw.time_out);
                        selesaiDialog.show();
                    }
                };
                countDownTimer.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Tampil Soal
        soal = findViewById(R.id.soal);
        optionA = findViewById(R.id.A);
        optionB = findViewById(R.id.B);
        optionC = findViewById(R.id.C);
        optionD = findViewById(R.id.D);

        //Disable Radio Button Selected
        optionA.setEnabled(false);
        optionB.setEnabled(false);
        optionC.setEnabled(false);
        optionD.setEnabled(false);

        //Initialize QuestionBank
        questionBank = new QuestionBank();

        //Tampilkan Urutan Soal
        urutanSoal = findViewById(R.id.urutanSoal);
        dbUser.child("HistoriTes").child("NomorUrutSoal").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer historiNomorSoal = snapshot.getValue(Integer.class);

                if (historiNomorSoal < 40){
                    //Set Textview Urutan Soal Saat Next Soal
                    urutanSoal.setText(String.valueOf(historiNomorSoal + 1 +"/40"));
                } else {
                    urutanSoal.setText("40/40");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Menu_Test.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });

        //Proteksi Get Soal Saat Membuka Halaman Tes
        dbUser.child("HistoriTes").child("NomorUrutSoal").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer historiNomorSoal = snapshot.getValue(Integer.class);

                if (historiNomorSoal < 40) {
                    //Get Soal Dari Class QuestionBank
                    questionBank.getNextQuestion(new QuestionBank.OnQuestionLoadedListener() {
                        @Override
                        public void onQuestionLoaded(Question question) {
                            loadQuestion(question);
                            //Enable Radio Button Selected
                            optionA.setEnabled(true);
                            optionB.setEnabled(true);
                            optionC.setEnabled(true);
                            optionD.setEnabled(true);
                        }
                    });
                    urutanSoal.setText(String.valueOf(historiNomorSoal + 1 +"/40"));

                } else {
                    urutanSoal.setText("40/40");
                    //Set Text Keterangan Selesai
                    keteranganSelesai.setText("Sesi Tes Telah Selesai");
                    imgKeterangan.setAnimation(R.raw.success_check);
                    //Stop Hitung Mundur
                    countDownTimer.cancel();
                    selesaiDialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Menu_Test.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTimerText(long millisUntilFinished) {
        //Get UID User From Email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();

        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);
        dbUser.child("HistoriTes").child("WaktuTes").setValue(millisUntilFinished);

        //Convert dari Millidetik ke Menit dan Detik
        long minutes = millisUntilFinished / 60000;
        long seconds = (millisUntilFinished % 60000) / 1000;

        //Format timer MM:SS
        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);

        // Update the TextView with the formatted time
        timer.setText(timeLeftFormatted);
    }

    private void blinkRedTimer() {
        int textColor = timer.getCurrentTextColor();
        if (textColor == Color.RED) {
            timer.setTextColor(Color.WHITE);
        } else {
            timer.setTextColor(Color.RED);
        }
    }

    private void loadQuestion(Question question) {
        // Display question and options in your UI components
        soal.setText(question.getQuestion());
        optionA.setText(question.getOptionA());
        optionB.setText(question.getOptionB());
        optionC.setText(question.getOptionC());
        optionD.setText(question.getOptionD());

        //Reset Radio Button Selection
        optionA.setChecked(false);
        optionB.setChecked(false);
        optionC.setChecked(false);
        optionD.setChecked(false);

        //Set Animasi Transisi Soal
        Animation fadeOut = AnimationUtils.loadAnimation(Menu_Test.this, R.anim.fade_out);
        fadeOut.setDuration(500);
        soal.startAnimation(fadeOut);
        optionA.startAnimation(fadeOut);
        optionB.startAnimation(fadeOut);
        optionC.startAnimation(fadeOut);
        optionD.startAnimation(fadeOut);

        optionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionSelected(question, 'A');
                //Disable Radio Button Selected
                optionA.setEnabled(false);
                optionB.setEnabled(false);
                optionC.setEnabled(false);
                optionD.setEnabled(false);
            }
        });

        optionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionSelected(question, 'B');
                //Disable Radio Button Selected
                optionA.setEnabled(false);
                optionB.setEnabled(false);
                optionC.setEnabled(false);
                optionD.setEnabled(false);
            }
        });

        optionC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionSelected(question, 'C');
                //Disable Radio Button Selected
                optionA.setEnabled(false);
                optionB.setEnabled(false);
                optionC.setEnabled(false);
                optionD.setEnabled(false);
            }
        });

        optionD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionSelected(question, 'D');
                //Disable Radio Button Selected
                optionA.setEnabled(false);
                optionB.setEnabled(false);
                optionC.setEnabled(false);
                optionD.setEnabled(false);
            }
        });
    }

    private void onOptionSelected(Question question, char selectedAnswer) {
        //Get UID User From Email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();

        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);

        //Cek Kunci Jawaban
        if (selectedAnswer == question.getDominance()) {
            dbUser.child("HistoriTes").child("Dominance").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    dominance = snapshot.getValue(Integer.class);
                    dominance++;
                    dbUser.child("HistoriTes").child("Dominance").setValue(dominance);
                    updateUrutanSoal();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    return;
                }
            });
        } else if (selectedAnswer == question.getInfluence()) {
            dbUser.child("HistoriTes").child("Influence").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    influence = snapshot.getValue(Integer.class);
                    influence++;
                    dbUser.child("HistoriTes").child("Influence").setValue(influence);
                    updateUrutanSoal();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    return;
                }
            });
        } else if (selectedAnswer == question.getSteadiness()) {
            dbUser.child("HistoriTes").child("Steadiness").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    steadiness = snapshot.getValue(Integer.class);
                    steadiness++;
                    dbUser.child("HistoriTes").child("Steadiness").setValue(steadiness);
                    updateUrutanSoal();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    return;
                }
            });
        } else if (selectedAnswer == question.getCompliance()) {
            dbUser.child("HistoriTes").child("Compliance").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    compliance = snapshot.getValue(Integer.class);
                    compliance++;
                    dbUser.child("HistoriTes").child("Compliance").setValue(compliance);
                    updateUrutanSoal();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    return;
                }
            });
        } else {
            Toast.makeText(this, "Jawaban Tidak Valid", Toast.LENGTH_SHORT).show();
        }

        //Hitung Jumlah Soal
        dbUser.child("HistoriTes").child("NomorUrutSoal").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer historiNomorSoal = snapshot.getValue(Integer.class);

                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (historiNomorSoal < 39) {
                            //Get Soal Dari Class QuestionBank
                            questionBank.getNextQuestion(new QuestionBank.OnQuestionLoadedListener() {
                                @Override
                                public void onQuestionLoaded(Question question) {
                                    loadQuestion(question);
                                    //Enable Radio Button Selected
                                    optionA.setEnabled(true);
                                    optionB.setEnabled(true);
                                    optionC.setEnabled(true);
                                    optionD.setEnabled(true);
                                }
                            });
                        } else {
                            //Set Text Keterangan Selesai
                            keteranganSelesai.setText("Sesi Tes Telah Selesai");
                            imgKeterangan.setAnimation(R.raw.success_check);
                            //Stop Hitung Mundur
                            countDownTimer.cancel();
                            selesaiDialog.show();
                        }
                    }
                }, 400);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Menu_Test.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUrutanSoal() {
        //Get UID User From Email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();

        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);

        dbUser.child("HistoriTes").child("NomorUrutSoal").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer historiNomorSoal = snapshot.getValue(Integer.class);

                if (historiNomorSoal < 40) {
                    dbUser.child("HistoriTes").child("NomorUrutSoal").setValue(historiNomorSoal + 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                return;
            }
        });
    }

    @Override
    public void onBackPressed() {
        cancelDialog.show();

        ya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelDialog.dismiss();
                mediaPlayer.stop();
                mediaPlayer = null;
                //Stop Hitung Mundur
                countDownTimer.cancel();
                startActivity(new Intent(Menu_Test.this, Menu_Utama.class));
                finish();
            }
        });

        tidak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelDialog.dismiss();
            }
        });
    }
}