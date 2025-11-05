package com.example.tdisc;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuestionBank {

    private List<Question> questions;

    public QuestionBank() {
        questions = new ArrayList<>();
    }

    public void getNextQuestion(final OnQuestionLoadedListener listener) {
        //Get UID User From Email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String UID = currentUser.getUid();

        DatabaseReference dbUser = FirebaseDatabase.getInstance().getReference().child("DatabaseUser").child(UID).child("HistoriTes").child("NomorUrutSoal");
        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer nomorUrutSnapshot = snapshot.getValue(Integer.class);
                Integer nomorUrut = nomorUrutSnapshot + 1;

                //Get Soal Dari Database
                DatabaseReference paketSoal = FirebaseDatabase.getInstance().getReference().child("DatabaseUser").child(UID).child("PaketSoal");
                paketSoal.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DataSnapshot questionSnapshot = snapshot.child(String.valueOf(nomorUrut));
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
                        questions.add(question);
                        listener.onQuestionLoaded(question);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        return;
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                return;
            }
        });
    }

    public interface OnQuestionLoadedListener {
        void onQuestionLoaded(Question question);
    }
}
