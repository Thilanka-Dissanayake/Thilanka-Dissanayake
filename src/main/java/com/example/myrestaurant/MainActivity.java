package com.example.myrestaurant;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Stay Logged In Logic
        if (mAuth.getCurrentUser() != null) {
            checkUserRoleAndRedirect(mAuth.getCurrentUser().getUid());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etEmail = findViewById(R.id.etLoginEmail);
        EditText etPassword = findViewById(R.id.etLoginPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvGoToRegister);

        // Switch to Register Interface
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String pass = etPassword.getText().toString();

            if (!email.isEmpty() && !pass.isEmpty()) {
                mAuth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(authResult -> {
                    checkUserRoleAndRedirect(authResult.getUser().getUid());
                }).addOnFailureListener(e -> Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void checkUserRoleAndRedirect(String uid) {
        db.collection("Users").document(uid).get().addOnSuccessListener(doc -> {
            String role = doc.getString("role");
            if ("Owner".equals(role)) {
                startActivity(new Intent(this, OwnerDashboardActivity.class));
            } else {
                startActivity(new Intent(this, WorkerChoiceActivity.class));
            }
            finish();
        });
    }
}