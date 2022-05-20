package com.example.codi_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText change_ID_ET, change_password_ET, change_confirm_ET;
    private AppCompatButton change_password_Btn;
    private FirebaseFirestore db;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);
        db= FirebaseFirestore.getInstance();
        change_ID_ET=findViewById(R.id.change_ID_EditText);
        change_password_ET=findViewById(R.id.change_password_EditText);
        change_confirm_ET=findViewById(R.id.change_confirm_EditText);
        change_password_Btn=findViewById(R.id.change_password_Btn);

        change_password_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String ID=change_ID_ET.getText().toString();
                String password=change_password_ET.getText().toString();
                String conf_password=change_confirm_ET.getText().toString();
                if(password.equals(conf_password)){     //비밀번호 확인이 비밀번호와 같을 시
                    UpdateData(ID, password);
                }else{
                    Toast.makeText(ChangePasswordActivity.this, "Password not match", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void UpdateData(String ID, String password) {         //비밀번호 변경
        db.collection("person")
                .document("customer")
                .collection("id")
                .document(ID).update("password", password).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ChangePasswordActivity.this, "Update Success", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChangePasswordActivity.this, "Update Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
