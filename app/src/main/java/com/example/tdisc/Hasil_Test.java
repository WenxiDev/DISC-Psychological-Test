package com.example.tdisc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Hasil_Test extends AppCompatActivity {

    Button back, simpanHasilTes, lengkapiData;
    TextView hint, keterangan, tanggal, jenisKepribadian, karakter, pekerjaan, sifatUmum;
    Dialog loadingDialog, lengkapiDataDialog;
    Spinner pilihHasil;
    PieChart pieChart;
    HorizontalScrollView informasi;
    Integer maxValueCount = 0;
    String maxLabel;
    LottieAnimationView imgKeterangan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hasil_test);

        //Hide Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Get UID User From Email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();

        //Realtime Database
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);

        //Loading Dialog
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_bar);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);

        //Button Kembali
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Button Download Hasil Tes
        simpanHasilTes = findViewById(R.id.simpanHasil);

        //Hint Jika Data Tes Tidak Ada
        hint = findViewById(R.id.hint);

        //Keterangan Grafik
        keterangan = findViewById(R.id.keteranganGrafik);

        //Tanggal Tes
        tanggal = findViewById(R.id.tanggalTes);

        //Informasi Kepribadian
        informasi = findViewById(R.id.informasi);
        jenisKepribadian = findViewById(R.id.jenisKepribadian);
        karakter = findViewById(R.id.karakter);
        pekerjaan = findViewById(R.id.pekerjaan);
        sifatUmum = findViewById(R.id.sifatUmum);

        //Spinner Pilih Hasil
        pilihHasil = findViewById(R.id.spinnerHasil);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dbUser.child("JumlahTes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    adapter.clear();
                    Integer jumlahTes = snapshot.getValue(Integer.class);

                    //Set Hint Spinner
                    adapter.add("Pilih Hasil Tes");

                    if (jumlahTes < 1) {
                        hint.setText("Data Tes Tidak Ada\nSilahkan Lakukan Tes Terlebih Dahulu");
                        pilihHasil.setEnabled(false);
                    } else {
                        pilihHasil.setEnabled(true);
                        for (int i = 1; i <= jumlahTes; i++) {
                            adapter.add("Hasil Tes Ke " + i);
                        }
                    }

                    //Lihat Detail Tes
                    Intent intent = getIntent();
                    Integer tesTerbaru = intent.getIntExtra("hasilTes", 0);

                    if (tesTerbaru != 0) {
                        //Set Item Selection di Spinner
                        adapter.clear();
                        adapter.add("Hasil Tes Ke " + tesTerbaru);

                        //Set Database Sesuai Spinner
                        dbUser.child("HasilTes").child("Tes" + tesTerbaru).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    //Set Keterangan Grafik
                                    keterangan.setVisibility(View.VISIBLE);
                                    keterangan.setText("Grafik Hasil Tes Ke " + tesTerbaru);

                                    //Set Tanggal Tes
                                    String tanggalTes = snapshot.child("TanggalTes").getValue(String.class);
                                    if (tanggalTes.equals("null")) {
                                        tanggal.setVisibility(View.GONE);
                                    } else {
                                        tanggal.setVisibility(View.VISIBLE);
                                        tanggal.setText("Tanggal Tes : " + tanggalTes);
                                    }

                                    float dominanceValue = snapshot.child("Dominance").getValue(Float.class);
                                    float influenceValue = snapshot.child("Influence").getValue(Float.class);
                                    float complianceValue = snapshot.child("Compliance").getValue(Float.class);
                                    float steadinessValue = snapshot.child("Steadiness").getValue(Float.class);

                                    pieChartGrafik(dominanceValue, influenceValue, complianceValue, steadinessValue);

                                    simpanHasilTes.setVisibility(View.VISIBLE);
                                    simpanHasilTes.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            getDataUser(tesTerbaru);
                                            loadingDialog.show();
                                        }
                                    });

                                } else {
                                    Toast.makeText(Hasil_Test.this, "Database Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(Hasil_Test.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    pilihHasil.setAdapter(adapter);

                    pilihHasil.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            if (position != 0) {

                                //Set Database Sesuai Spinner
                                dbUser.child("HasilTes").child("Tes" + position).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            //Set Keterangan Grafik
                                            keterangan.setVisibility(View.VISIBLE);
                                            keterangan.setText("Grafik Hasil Tes Ke " + position);

                                            //Set Tanggal Tes
                                            String tanggalTes = snapshot.child("TanggalTes").getValue(String.class);
                                            if (tanggalTes.equals("null")) {
                                                tanggal.setVisibility(View.GONE);
                                            } else {
                                                tanggal.setVisibility(View.VISIBLE);
                                                tanggal.setText("Tanggal Tes : " + tanggalTes);
                                            }

                                            float dominanceValue = snapshot.child("Dominance").getValue(Float.class);
                                            float influenceValue = snapshot.child("Influence").getValue(Float.class);
                                            float complianceValue = snapshot.child("Compliance").getValue(Float.class);
                                            float steadinessValue = snapshot.child("Steadiness").getValue(Float.class);

                                            pieChartGrafik(dominanceValue, influenceValue, complianceValue, steadinessValue);

                                            simpanHasilTes.setVisibility(View.VISIBLE);
                                            simpanHasilTes.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    getDataUser(position);
                                                    loadingDialog.show();
                                                }
                                            });

                                        } else {
                                            Toast.makeText(Hasil_Test.this, "Database Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(Hasil_Test.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Hasil_Test.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pieChartGrafik(float dominanceValue, float influenceValue, float complianceValue, float steadinessValue) {

        //Value Grafik
        pieChart = findViewById(R.id.pieChart);
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(dominanceValue, "Dominance"));
        entries.add(new PieEntry(influenceValue, "Influence"));
        entries.add(new PieEntry(complianceValue, "Compliance"));
        entries.add(new PieEntry(steadinessValue, "Steadiness"));

        //Hitung Nilai Max/Tertinggi
        float maxValue = Math.max(dominanceValue, Math.max(influenceValue, Math.max(complianceValue, steadinessValue)));

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
        pieData.setValueFormatter(new Hasil_Test.PercentFormatter());
        pieChart.setData(pieData);
        pieChart.invalidate();

        //Hilangkan Teks Deskripsi
        pieChart.getDescription().setEnabled(false);

        ///Set Radius Tengah PieChart
        float transparentCircleRadius = 40f;
        float holeRadius = 35f;
        pieChart.setHoleRadius(holeRadius);
        pieChart.setTransparentCircleRadius(transparentCircleRadius);

        //Disable Legend
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        //Hitung Jumlah Max Value
        if (dominanceValue == maxValue) {
            maxLabel = "Dominance";
            maxValueCount++;
        }
        if (influenceValue == maxValue) {
            maxLabel = "Influence";
            maxValueCount++;
        }
        if (complianceValue == maxValue) {
            maxLabel = "Compliance";
            maxValueCount++;
        }
        if (steadinessValue == maxValue) {
            maxLabel = "Steadiness";
            maxValueCount++;
        }

        if (maxValueCount > 1) {
            //Tampilan Layout Informasi
            informasi.setVisibility(View.GONE);
        } else {
            //Highlight Max Value;
            highlightMaxValue(entries, maxValue);
        }

        //Tampilan Keterangan Kepribadian
        jenisKepribadian.setVisibility(View.VISIBLE);

        //Tampilkan Informasi Karakteristik
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                //Tampilan Layout Informasi
                informasi.setVisibility(View.VISIBLE);
                if (e instanceof PieEntry) {
                    PieEntry pieEntry = (PieEntry) e;
                    String selectedLabel = pieEntry.getLabel();

                    //Tampilkan Informasi Kepribadian
                    if (selectedLabel.equals("Dominance")) {
                        if (dominanceValue > 50) {
                            jenisKepribadian.setText("Kepribadian Dominance");
                            karakter.setText(R.string.kDominanceTinggi);
                            pekerjaan.setText(R.string.pDominanceTinggi);
                            sifatUmum.setText(R.string.sDominanceTinggi);
                        } else {
                            jenisKepribadian.setText("Kepribadian Dominance");
                            karakter.setText(R.string.kDominanceRendah);
                            pekerjaan.setText(R.string.pDominanceRendah);
                            sifatUmum.setText(R.string.sDominanceRendah);
                        }
                    } else if (selectedLabel.equals("Influence")) {
                        if (influenceValue > 50) {
                            jenisKepribadian.setText("Kepribadian Influence");
                            karakter.setText(R.string.kInfluenceTinggi);
                            pekerjaan.setText(R.string.pInfluenceTinggi);
                            sifatUmum.setText(R.string.sInfluenceTinggi);
                        } else {
                            jenisKepribadian.setText("Kepribadian Influence");
                            karakter.setText(R.string.kInfluenceRendah);
                            pekerjaan.setText(R.string.pInfluenceRendah);
                            sifatUmum.setText(R.string.sInfluenceRendah);
                        }
                    } else if (selectedLabel.equals("Steadiness")) {
                        if (steadinessValue > 50) {
                            jenisKepribadian.setText("Kepribadian Steadiness");
                            karakter.setText(R.string.kSteadinessTinggi);
                            pekerjaan.setText(R.string.pSteadinessTinggi);
                            sifatUmum.setText(R.string.sSteadinessTinggi);
                        } else {
                            jenisKepribadian.setText("Kepribadian Steadiness");
                            karakter.setText(R.string.kSteadinessRendah);
                            pekerjaan.setText(R.string.pSteadinessRendah);
                            sifatUmum.setText(R.string.sSteadinessRendah);
                        }
                    } else if (selectedLabel.equals("Compliance")) {
                        if (complianceValue > 50) {
                            jenisKepribadian.setText("Kepribadian Compliance");
                            karakter.setText(R.string.kComplianceTinggi);
                            pekerjaan.setText(R.string.pComplianceTinggi);
                            sifatUmum.setText(R.string.sComplianceTinggi);
                        } else {
                            jenisKepribadian.setText("Kepribadian Compliance");
                            karakter.setText(R.string.kComplianceRendah);
                            pekerjaan.setText(R.string.pComplianceRendah);
                            sifatUmum.setText(R.string.sComplianceRendah);
                        }
                    }
                } else {
                    //Tampilan Layout Informasi
                    informasi.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected() {
                for (int i = 0; i < entries.size(); i++) {
                    PieEntry entry = entries.get(i);
                    if (entry.getValue() == maxValue && maxValueCount < 2) {
                        //Animasi Highlight
                        pieChart.highlightValue(new Highlight(i, 0, 0));

                        //Tampilkan Informasi Kepribadian
                        if (maxLabel.equals("Dominance")) {
                            if (dominanceValue > 50) {
                                jenisKepribadian.setText("Kepribadian Dominance");
                                karakter.setText(R.string.kDominanceTinggi);
                                pekerjaan.setText(R.string.pDominanceTinggi);
                                sifatUmum.setText(R.string.sDominanceTinggi);
                            } else {
                                jenisKepribadian.setText("Kepribadian Dominance");
                                karakter.setText(R.string.kDominanceRendah);
                                pekerjaan.setText(R.string.pDominanceRendah);
                                sifatUmum.setText(R.string.sDominanceRendah);
                            }
                        } else if (maxLabel.equals("Influence")) {
                            if (influenceValue > 50) {
                                jenisKepribadian.setText("Kepribadian Influence");
                                karakter.setText(R.string.kInfluenceTinggi);
                                pekerjaan.setText(R.string.pInfluenceTinggi);
                                sifatUmum.setText(R.string.sInfluenceTinggi);
                            } else {
                                jenisKepribadian.setText("Kepribadian Influence");
                                karakter.setText(R.string.kInfluenceRendah);
                                pekerjaan.setText(R.string.pInfluenceRendah);
                                sifatUmum.setText(R.string.sInfluenceRendah);
                            }
                        } else if (maxLabel.equals("Steadiness")) {
                            if (steadinessValue > 50) {
                                jenisKepribadian.setText("Kepribadian Steadiness");
                                karakter.setText(R.string.kSteadinessTinggi);
                                pekerjaan.setText(R.string.pSteadinessTinggi);
                                sifatUmum.setText(R.string.sSteadinessTinggi);
                            } else {
                                jenisKepribadian.setText("Kepribadian Steadiness");
                                karakter.setText(R.string.kSteadinessRendah);
                                pekerjaan.setText(R.string.pSteadinessRendah);
                                sifatUmum.setText(R.string.sSteadinessRendah);
                            }
                        } else if (maxLabel.equals("Compliance")) {
                            if (complianceValue > 50) {
                                jenisKepribadian.setText("Kepribadian Compliance");
                                karakter.setText(R.string.kComplianceTinggi);
                                pekerjaan.setText(R.string.pComplianceTinggi);
                                sifatUmum.setText(R.string.sComplianceTinggi);
                            } else {
                                jenisKepribadian.setText("Kepribadian Compliance");
                                karakter.setText(R.string.kComplianceRendah);
                                pekerjaan.setText(R.string.pComplianceRendah);
                                sifatUmum.setText(R.string.sComplianceRendah);
                            }
                        }
                    }
                }
            }
        });
    }

    private void highlightMaxValue(List<PieEntry> entries, float maxValue) {
        for (int i = 0; i < entries.size(); i++) {
            PieEntry entry = entries.get(i);
            if (entry.getValue() == maxValue && maxValueCount < 2) {
                //Tampilan Layout Informasi
                informasi.setVisibility(View.VISIBLE);

                //Animasi Highlight
                pieChart.highlightValue(new Highlight(i, 0, 0));

                float dominanceValue = entry.getValue();
                float influenceValue = entry.getValue();
                float complianceValue = entry.getValue();
                float steadinessValue = entry.getValue();

                //Tampilkan Informasi Kepribadian
                if (maxLabel.equals("Dominance")) {
                    if (dominanceValue > 50) {
                        jenisKepribadian.setText("Kepribadian Dominance");
                        karakter.setText(R.string.kDominanceTinggi);
                        pekerjaan.setText(R.string.pDominanceTinggi);
                        sifatUmum.setText(R.string.sDominanceTinggi);
                    } else {
                        jenisKepribadian.setText("Kepribadian Dominance");
                        karakter.setText(R.string.kDominanceRendah);
                        pekerjaan.setText(R.string.pDominanceRendah);
                        sifatUmum.setText(R.string.sDominanceRendah);
                    }
                } else if (maxLabel.equals("Influence")) {
                    if (influenceValue > 50) {
                        jenisKepribadian.setText("Kepribadian Influence");
                        karakter.setText(R.string.kInfluenceTinggi);
                        pekerjaan.setText(R.string.pInfluenceTinggi);
                        sifatUmum.setText(R.string.sInfluenceTinggi);
                    } else {
                        jenisKepribadian.setText("Kepribadian Influence");
                        karakter.setText(R.string.kInfluenceRendah);
                        pekerjaan.setText(R.string.pInfluenceRendah);
                        sifatUmum.setText(R.string.sInfluenceRendah);
                    }
                } else if (maxLabel.equals("Steadiness")) {
                    if (steadinessValue > 50) {
                        jenisKepribadian.setText("Kepribadian Steadiness");
                        karakter.setText(R.string.kSteadinessTinggi);
                        pekerjaan.setText(R.string.pSteadinessTinggi);
                        sifatUmum.setText(R.string.sSteadinessTinggi);
                    } else {
                        jenisKepribadian.setText("Kepribadian Steadiness");
                        karakter.setText(R.string.kSteadinessRendah);
                        pekerjaan.setText(R.string.pSteadinessRendah);
                        sifatUmum.setText(R.string.sSteadinessRendah);
                    }
                } else if (maxLabel.equals("Compliance")) {
                    if (complianceValue > 50) {
                        jenisKepribadian.setText("Kepribadian Compliance");
                        karakter.setText(R.string.kComplianceTinggi);
                        pekerjaan.setText(R.string.pComplianceTinggi);
                        sifatUmum.setText(R.string.sComplianceTinggi);
                    } else {
                        jenisKepribadian.setText("Kepribadian Compliance");
                        karakter.setText(R.string.kComplianceRendah);
                        pekerjaan.setText(R.string.pComplianceRendah);
                        sifatUmum.setText(R.string.sComplianceRendah);
                    }
                }

                //Animasi Tampil Pie Chart
                pieChart.animateXY(500, 500);
                break;
            }
            else {
                //Tampilan Layout Informasi
                informasi.setVisibility(View.GONE);
            }
        }
    }

    private class PercentFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            //Format Koma Belakang + Symbol %
            return String.valueOf(value).replaceAll("\\.?0*$", "") + "%";
        }
    }

    private void getDataUser(Integer urutanTes) {
        //Get UID User From Email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();

        //Nama User Berdasarkan Email
        FirebaseUser loginUsername = FirebaseAuth.getInstance().getCurrentUser();
        String namaEmail = loginUsername.getEmail();

        //Realtime Database
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);

        //Get Nama User
        dbUser.child("Nama").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String namaSnapshot = snapshot.getValue(String.class);

                    //Get Tanggal Lahir User
                    dbUser.child("TanggalLahir").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String tanggalSnapshot = snapshot.getValue(String.class);

                                //Get Jenis Kelamin
                                dbUser.child("Gender").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            String genderSnapshot = snapshot.getValue(String.class);

                                            createPdf(urutanTes, namaSnapshot, tanggalSnapshot, genderSnapshot, namaEmail);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(Hasil_Test.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Hasil_Test.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Hasil_Test.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createPdf(Integer urutanTes, String namaSnapshot, String  tanggalSnapshot, String genderSnapshot, String namaEmail) {

        //Lengkapi Data Dialog
        lengkapiDataDialog = new Dialog(this);
        lengkapiDataDialog.setContentView(R.layout.data_tidak_lengkap_dialogbox);
        lengkapiDataDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        lengkapiDataDialog.setCancelable(false);
        lengkapiDataDialog.setCanceledOnTouchOutside(false);

        //Set Image Dialog Keterangan
        imgKeterangan = lengkapiDataDialog.findViewById(R.id.imgKeterangan);
        imgKeterangan.setAnimation(R.raw.nodata);

        //Set Button Lengkapi Data
        lengkapiData = lengkapiDataDialog.findViewById(R.id.lengkapiData);
        lengkapiData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Hasil_Test.this, Profile.class));
                finish();
                lengkapiDataDialog.dismiss();
            }
        });

        if (namaSnapshot.equals("null") || tanggalSnapshot.equals("null") || genderSnapshot.equals("null")) {
            loadingDialog.dismiss();
            lengkapiDataDialog.show();
        } else {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 790, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            //Definis Header
            int headerHeight = 195;
            int pageWidth = pageInfo.getPageWidth();
            int pageHeight = pageInfo.getPageHeight();
            int footerHeight = 65;

            //Set Header Color
            paint.setColor(0xFF1DB7AE);
            canvas.drawRect(0, 0, pageWidth, headerHeight, paint);

            //Isi Header
            paint.setColor(0xFFFEDD06);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setTextSize(30);
            canvas.drawText("Informasi Tentang Anda", 20, 40, paint);

            paint.setColor(0xFFFFFFFF);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setTextSize(23);
            int x = 20;
            int y = 80;
            canvas.drawText("Nama  : " + namaSnapshot, x, y, paint);
            y += paint.descent() - paint.ascent() + 5;
            canvas.drawText("Tanggal Lahir  : " + tanggalSnapshot, x, y, paint);
            y += paint.descent() - paint.ascent() + 5;
            canvas.drawText("Jenis Kelamin  : " + genderSnapshot, x, y, paint);
            y += paint.descent() - paint.ascent() + 5;
            canvas.drawText("Alamat Email  : " + namaEmail, x, y, paint);

            //Capture Grafik
            View layoutView = findViewById(R.id.layoutHasilTes); // Replace with your layout ID
            Bitmap layoutBitmap = captureView(layoutView);

            int scaledWidth = pageWidth;
            int scaledHeight = (int) ((float) layoutBitmap.getHeight() * scaledWidth / layoutBitmap.getWidth());

            Bitmap scaledBitmap = scaleBitmap(layoutBitmap, scaledWidth, scaledHeight);

            int imageX = 0;
            int imageY = headerHeight + 20;
            canvas.drawBitmap(scaledBitmap, imageX, imageY, paint);

            //Footer
            paint.setColor(0xFFEBFCFB);
            canvas.drawRect(0, pageHeight - footerHeight, pageWidth, pageHeight, paint);

            paint.setColor(0xFF000000);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setTextSize(15);

            String footerTextLine1 = "Created on " + new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
            String footerTextLine2 = "Copyright By Dity©";

            float textHeight = paint.descent() - paint.ascent();
            int textY1 = pageHeight - footerHeight / 2 + (int) (textHeight / 2) - 15;
            int textY2 = textY1 + (int) textHeight + 5;

            float textWidth1 = paint.measureText(footerTextLine1);
            canvas.drawText(footerTextLine1, (pageWidth - textWidth1) / 2, textY1, paint);

            float textWidth2 = paint.measureText(footerTextLine2);
            canvas.drawText(footerTextLine2, (pageWidth - textWidth2) / 2, textY2, paint);

            pdfDocument.finishPage(page);

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Tes DISC Ke "+ urutanTes + ".pdf");
            try {
                pdfDocument.writeTo(new FileOutputStream(file));
                Toast.makeText(Hasil_Test.this, "Download Hasil Tes Behasil", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(Hasil_Test.this, "Download Hasil Tes Gagal", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
            pdfDocument.close();
        }
    }

    private Bitmap captureView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
}