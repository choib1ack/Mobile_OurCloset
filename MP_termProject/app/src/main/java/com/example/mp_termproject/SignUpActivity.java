package com.example.mp_termproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.signUpButton).setOnClickListener(onClickListener);
        findViewById(R.id.goToLoginButton).setOnClickListener(onClickListener);

    }
    // 뒤로 가기 버튼을 누르면 main activity 로 넘어가는 것을 막아줌
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.signUpButton:
                    signUp();
                    break;
                case R.id.goToLoginButton:
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };


    private void signUp() {
        String email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
        String password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
        String passwordCheck = ((EditText) findViewById(R.id.passwordCheckEditText)).getText().toString();

        // 이메일이나 비밀번호, 비밀번호확인을 입력하지 않았을때
        if (email.length() > 0 && password.length() > 0 && passwordCheck.length() > 0) {
            // 비밀번호와 비밀번호 확인이 일치하는지
            if (password.equals(passwordCheck)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // 회원가입 성공
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    startToast("회원가입에 성공하였습니다.");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                }
                                // 비밀번호 null 일때
                                else {
                                    if (task.getException().toString() != null) {
                                        startToast(task.getException().toString());
                                    }
                                    // If sign in fails, display a message to the user.
                                }
                            }
                        });
            } else {
                startToast("비밀번호가 일치하지 않습니다.");

            }
        } else {
            startToast("이메일 또는 비밀번호를 입력하시오");
        }
    }

    // 회원가입 성공 혹은 불일치 Toast
    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

//    // getApplicationContext 과 달리 this를 사용하면 listener에서 사용하지 못하기때문에 함수를 만들어서 사용
//    private void startLoginActivity(){
//        Intent intent = new Intent(this,LoginActivity.class);
//        startActivity(intent);
//    }
}