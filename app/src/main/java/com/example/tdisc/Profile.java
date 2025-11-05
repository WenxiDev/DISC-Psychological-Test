package com.example.tdisc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    Button back, ubahFoto, btnNama, btnTanggalLahir, btnJenisKelamin, btnHasilTes, about, logout;
    TextView nama, tanggalLahir, jenisKelamin, email, jumlahTes;
    GoogleSignInClient googleAuth;
    Dialog logoutDialog, inputNamaDialog, genderDialog, loadingDialog;
    CircleImageView fotoProfile;
    Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

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

        //User SigIn
        googleAuth = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        //Keterangan
        nama = findViewById(R.id.nama);
        tanggalLahir = findViewById(R.id.tanggalLahir);
        jenisKelamin = findViewById(R.id.jenisKelamin);
        email = findViewById(R.id.email);
        jumlahTes = findViewById(R.id.jumlahTes);

        //Get UID User From Email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();

        //Nama User Berdasarkan Email
        FirebaseUser loginUsername = FirebaseAuth.getInstance().getCurrentUser();
        String namaEmail = loginUsername.getEmail();

        //Realtime Database
        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);

        //Button Kembali
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Button Ubah Foto
        ubahFoto = findViewById(R.id.ubahFoto);
        ubahFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

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
                    Toast.makeText(Profile.this, "Database Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });

        //Dialog Input Nama
        Button namaConfirm, namaCancel;
        EditText edNama;
        TextView jumlahNama;
        inputNamaDialog = new Dialog(this);
        inputNamaDialog.setContentView(R.layout.input_nama_dialogbox);
        inputNamaDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        inputNamaDialog.setCanceledOnTouchOutside(false);

        //Textview Jumlah Nama
        jumlahNama = inputNamaDialog.findViewById(R.id.jumlahNama);

        //Edit Text Nama
        edNama = inputNamaDialog.findViewById(R.id.inputNama);

        //Hapus @gmail.com
        String userName = namaEmail.substring(0, namaEmail.indexOf("@"));

        //Read Database Nama
        dbUser.child("Nama").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String namaSnapshot = snapshot.getValue(String.class);
                    if (namaSnapshot.equals("null")) {
                        edNama.setText(userName);
                        nama.setText(userName);
                    } else {
                        edNama.setText(namaSnapshot);
                        nama.setText(namaSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });

        //Hitung Jumlah Karakter
        edNama.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                hitungKarakter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

            private void hitungKarakter(String nama) {
                int jumlahKarakter = nama.length();
                jumlahNama.setText(String.valueOf(jumlahKarakter + "/20"));
                if (jumlahKarakter > 20) {
                    edNama.setError("Nama Maksimal 20 Karakter");
                } else if (jumlahKarakter < 1) {
                    edNama.setError("Wajib Isi Nama");
                } else {
                    edNama.setError(null);
                }
            }
        });

        //Dialog Button Confirm
        namaConfirm = inputNamaDialog.findViewById(R.id.confirm);
        namaConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String NamaUser = edNama.getText().toString();
                int jumlahKarakter = NamaUser.length();
                if (jumlahKarakter > 20) {
                    edNama.setError("Nama Maksimal 20 Karakter");
                } else if (NamaUser.isEmpty()) {
                    edNama.setError("Wajib Isi Nama");
                } else {
                    dbUser.child("Nama").setValue(NamaUser);
                    inputNamaDialog.dismiss();
                }
            }
        });

        //Dialog Button Cancel
        namaCancel = inputNamaDialog.findViewById(R.id.cancel);
        namaCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNamaDialog.dismiss();
            }
        });

        //Button Ubah Nama
        btnNama = findViewById(R.id.btnNama);
        btnNama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputNamaDialog.show();
            }
        });

        //Read Database Tanggal Lahir
        dbUser.child("TanggalLahir").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String tanggalSnapshot = snapshot.getValue(String.class);
                if (snapshot.exists()) {
                    if (tanggalSnapshot.equals("null")) {
                        tanggalLahir.setText("Tanggal Lahir");
                    } else {
                        tanggalLahir.setText(tanggalSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });

        //Button Tanggal Lahir
        btnTanggalLahir = findViewById(R.id.btnTanggalLahir);
        btnTanggalLahir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get Current Date
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                //Date Dialog Picker
                DatePickerDialog datePickerDialog = new DatePickerDialog(Profile.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                                Calendar selectedDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);

                                String formattedDate = dateFormat.format(selectedDate.getTime());

                                dbUser.child("TanggalLahir").setValue(formattedDate);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.setCanceledOnTouchOutside(false);
                datePickerDialog.show();
            }
        });

        //Dialog Jenis Kelamin
        RadioButton lakilaki, perempuan;
        Button genderCancel;
        genderDialog = new Dialog(this);
        genderDialog.setContentView(R.layout.gender_dialogbox);
        genderDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        genderDialog.setCanceledOnTouchOutside(false);

        //Radio Button Perempuan
        perempuan = genderDialog.findViewById(R.id.rdPerempuan);

        //Radio Button Laki-Laki
        lakilaki = genderDialog.findViewById(R.id.rdLakilaki);

        //Dialog Cancel
        genderCancel = genderDialog.findViewById(R.id.cancel);
        genderCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                genderDialog.dismiss();
            }
        });

        //Read Database Gender
        dbUser.child("Gender").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String genderSnapshot = snapshot.getValue(String.class);
                    if (genderSnapshot.equals("null")) {
                        jenisKelamin.setText("Jenis Kelamin");
                    } else if (genderSnapshot.equals("Laki-Laki")) {
                        lakilaki.setChecked(true);
                        jenisKelamin.setText(genderSnapshot);
                    } else if (genderSnapshot.equals("Perempuan")) {
                        perempuan.setChecked(true);
                        jenisKelamin.setText(genderSnapshot);
                    } else {
                        jenisKelamin.setText(genderSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });

        //Laki-Laki
        lakilaki.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dbUser.child("Gender").setValue("Laki-Laki");
                    genderDialog.dismiss();
                }
            }
        });

        //Perempuan
        perempuan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    dbUser.child("Gender").setValue("Perempuan");
                    genderDialog.dismiss();
                }
            }
        });

        //Button Jenis Kelamin
        btnJenisKelamin = findViewById(R.id.btnJenisKelamin);
        btnJenisKelamin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                genderDialog.show();
            }
        });

        //Email
        email.setText(namaEmail);

        //Jumlah Tes
        dbUser.child("JumlahTes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer jumlahSnapshot = snapshot.getValue(Integer.class);
                    jumlahTes.setText("Jumlah Tes: "+jumlahSnapshot.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Akses Database Gagal", Toast.LENGTH_SHORT).show();
            }
        });

        //Button Hasil Tes
        btnHasilTes = findViewById(R.id.btnHasil);
        btnHasilTes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this, Hasil_Test.class));
            }
        });

        //Button About
        about = findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Profile.this, About_App.class));
            }
        });

        //Dialog Logout
        Button ya, tidak;
        logoutDialog = new Dialog(this);
        logoutDialog.setContentView(R.layout.logout_dialogbox);
        logoutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        logoutDialog.setCanceledOnTouchOutside(false);

        //Dialog Button Ya
        ya = logoutDialog.findViewById(R.id.btnYa);
        ya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                googleAuth.signOut();
                logoutDialog.dismiss();
                startActivity(new Intent(Profile.this, MainActivity.class));
                finish();
            }
        });

        //Dialog Button Tidak
        tidak = logoutDialog.findViewById(R.id.btnTidak);
        tidak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutDialog.dismiss();
            }
        });

        //Button Logout
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutDialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            //Compress Foto
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] compressImage = baos.toByteArray();

                //Upload Foto
                if (imageUri != null && compressImage != null) {
                    //Get UID User From Email
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String UID = currentUser.getUid();

                    loadingDialog.show();
                    //Upload Image ke Firebase Storage
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference dbFoto = storage.getReference("DatabaseFotoUser").child(UID).child("FotoProfile");
                    dbFoto.putBytes(compressImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dbFoto.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();

                                        //Realtime Database
                                        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference("DatabaseUser").child(UID);
                                        dbUser.child("UrlFoto").setValue(downloadUri.toString());
                                        loadingDialog.dismiss();
                                    } else {
                                        Toast.makeText(Profile.this, "URL Foto Tidak Ditemukan", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Profile.this, "Upload Foto Gagal", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(Profile.this, "Compress Image Gagal", Toast.LENGTH_SHORT).show();
            }
        }
    }
}