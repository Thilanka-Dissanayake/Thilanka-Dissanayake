package com.example.myrestaurant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this matches your XML filename (activity_registration or registration)
        setContentView(R.layout.registration);

        Spinner spinner = findViewById(R.id.roleSpinner);
        String[] roles = {"Worker", "Owner"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles));

        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String email = ((EditText)findViewById(R.id.etRegEmail)).getText().toString().trim();
            String pass = ((EditText)findViewById(R.id.etRegPassword)).getText().toString().trim();
            String role = spinner.getSelectedItem().toString();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Added Failure Listener to catch errors
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(authResult -> {
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("role", role);

                        db.collection("Users").document(authResult.getUser().getUid())
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // This will tell you if the email is invalid or password is too short
                        Toast.makeText(this, "Auth Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        findViewById(R.id.tvGoToLogin).setOnClickListener(v -> finish());
    }
}