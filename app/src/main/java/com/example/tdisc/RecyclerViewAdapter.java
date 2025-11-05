package com.example.tdisc;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private List<Question> questionList;
    Kelola_Soal kelola_soal;
    Dialog inputSoalDialog, deleteDialog;
    EditText soal, pgA, pgB, pgC, pgD;
    TextView jumlahSoal, jumlahA, jumlahB, jumlahC, jumlahD;
    Button simpan, cancel, ya, tidak;
    Spinner dominance, influence, steadiness, compliance;
    public RecyclerViewAdapter(List<Question> questions, Kelola_Soal kelola_soal) {
        this.questionList = questions;
        this.kelola_soal = kelola_soal;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View viewQuestion = layoutInflater.inflate(R.layout. list_soal, parent, false);
        return new ViewHolder(viewQuestion);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        final Question question = questionList.get(position);
        //Tampil Nomor Soal
        holder.nomorSoal.setText("Soal Nomor " +(position + 1));

        //Tampil Soal Tes
        holder.soalTes.setText(question.getQuestion());

        //Button Edit
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Inisiasi Dialog Input Soal
                inputSoalDialog = new Dialog(kelola_soal);
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

                //Tampilkan Data Soal Saat Edit
                soal.setText("" +question.getQuestion());
                pgA.setText("A. " +question.getOptionA());
                pgB.setText("B. " +question.getOptionB());
                pgC.setText("C. " +question.getOptionC());
                pgD.setText("D. " +question.getOptionD());

                //Data Spinner
                String[] spinnerOptions = {"A", "B", "C", "D"};
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(kelola_soal, android.R.layout.simple_spinner_item, spinnerOptions);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                dominance.setAdapter(spinnerAdapter);
                influence.setAdapter(spinnerAdapter);
                steadiness.setAdapter(spinnerAdapter);
                compliance.setAdapter(spinnerAdapter);

                //Spinner Dominance
                char dominanceValue = question.getDominance();
                String dominanceString = String.valueOf(dominanceValue);
                int positionD = spinnerAdapter.getPosition(dominanceString);
                dominance.setSelection(positionD);

                //Spinner Influence
                char influenceValue = question.getInfluence();
                String influenceString = String.valueOf(influenceValue);
                int positionI = spinnerAdapter.getPosition(influenceString);
                influence.setSelection(positionI);

                //Spinner Steadiness
                char steadinessValue = question.getSteadiness();
                String steadinessString = String.valueOf(steadinessValue);
                int positionS = spinnerAdapter.getPosition(steadinessString);
                steadiness.setSelection(positionS);

                //Spinner Compliance
                char complianceValue = question.getCompliance();
                String complianceString = String.valueOf(complianceValue);
                int positionC = spinnerAdapter.getPosition(complianceString);
                compliance.setSelection(positionC);

                //Button Simpan
                simpan.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Get Semua Data Input Ke String
                        String editedQuestion = soal.getText().toString();
                        String editedOptionA = pgA.getText().toString();
                        String editedOptionB = pgB.getText().toString();
                        String editedOptionC = pgC.getText().toString();
                        String editedOptionD = pgD.getText().toString();
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
                        } else {
                            editedOptionA = editedOptionA.replace("A. ", "");
                            editedOptionB = editedOptionB.replace("B. ", "");
                            editedOptionC = editedOptionC.replace("C. ", "");
                            editedOptionD = editedOptionD.replace("D. ", "");

                            DatabaseReference dbSoal = FirebaseDatabase.getInstance().getReference().child("BankSoal").child(question.getKey());
                            //Get Semua Data Soal
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("Soal", editedQuestion);
                            updateData.put("Option A", editedOptionA);
                            updateData.put("Option B", editedOptionB);
                            updateData.put("Option C", editedOptionC);
                            updateData.put("Option D", editedOptionD);
                            updateData.put("KunciJawaban/Dominance", selectedDominance);
                            updateData.put("KunciJawaban/Influence", selectedInfluence);
                            updateData.put("KunciJawaban/Steadiness", selectedSteadiness);
                            updateData.put("KunciJawaban/Compliance", selectedCompliance);

                            dbSoal.updateChildren(updateData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(kelola_soal, "Update Soal Berhasil", Toast.LENGTH_SHORT).show();
                                    inputSoalDialog.dismiss();
                                    kelola_soal.recreate();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(kelola_soal, "Update Soal Gagal", Toast.LENGTH_SHORT).show();
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
            }
        });

        //Button Delete
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Inisiasi Dialog Konfirmasi Delete
                deleteDialog = new Dialog(kelola_soal);
                deleteDialog.setContentView(R.layout.delete_dialogbox);
                deleteDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                deleteDialog.setCanceledOnTouchOutside(false);
                deleteDialog.show();

                //Inisiasi Item Dialog Delete Soal
                ya = deleteDialog.findViewById(R.id.btnYa);
                tidak = deleteDialog.findViewById(R.id.btnTidak);

                //Button Ya
                ya.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference dbSoal = FirebaseDatabase.getInstance().getReference().child("BankSoal").child(question.getKey());
                        dbSoal.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(kelola_soal, "Hapus Soal Berhasil", Toast.LENGTH_SHORT).show();
                                deleteDialog.dismiss();
                                questionList.remove(position);
                                notifyItemRangeChanged(position, questionList.size());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(kelola_soal, "Hapus Soal Gagal", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                //Button Tidak
                tidak.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteDialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nomorSoal, soalTes;
        Button edit, delete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nomorSoal = itemView.findViewById(R.id.nomorSoal);
            soalTes = itemView.findViewById(R.id.soalTes);
            edit = itemView.findViewById(R.id.btn_Edit);
            delete = itemView.findViewById(R.id.btn_Delete);
        }
    }
}
