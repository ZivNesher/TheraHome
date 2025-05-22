package com.ziv.therahome;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class VisitNotesActivity extends AppCompatActivity {

    private ListView notesListView;
    private Button backButton;
    private DatabaseReference userRef;
    private ArrayList<String> notesList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_notes);

        notesListView = findViewById(R.id.notes_list);
        backButton = findViewById(R.id.back_button);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notesList);
        notesListView.setAdapter(adapter);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("visits");

        loadNotes();

        backButton.setOnClickListener(v -> finish());
    }

    private void loadNotes() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                notesList.clear();
                for (DataSnapshot noteSnap : snapshot.getChildren()) {
                    Map<String, String> note = (Map<String, String>) noteSnap.getValue();
                    String date = note.get("date");
                    String therapist = note.get("therapist");
                    String comment = note.get("comment");
                    notesList.add(date + "\n" + therapist + ": " + comment);
                }
                Collections.reverse(notesList); // newest first
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(VisitNotesActivity.this, "Failed to load notes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
