package com.aqsatech.phonenumberauthentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.aqsatech.phonenumberauthentication.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //View Binding
    ActivityMainBinding binding;

    //If code send failed, will use to resend code
    private PhoneAuthProvider.ForceResendingToken mResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId; //Will hold OTP/Verification Code
    private static final String TAG = "PhoneAuthActivity";

    private FirebaseAuth mAuth;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth=FirebaseAuth.getInstance();
        dialog=new ProgressDialog(this);
        dialog.setTitle("Please Wait...");
        dialog.setCanceledOnTouchOutside(false);

        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                dialog.dismiss();
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                mVerificationId=s;
                mResendingToken=forceResendingToken;
                dialog.dismiss();

                Toast.makeText(MainActivity.this, "verification code sent.", Toast.LENGTH_SHORT).show();

                binding.lbl2.setText("Please Enter the verification code we sent \n "+binding.phoneET.getText().toString().trim());

            }
        };

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone=binding.phoneET.getText().toString().trim();
                if(TextUtils.isEmpty(phone))
                {
                    Toast.makeText(MainActivity.this, "Please Enter Phone Number", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    startPhoneNumberVerification(phone);
                }
            }
        });

        binding.resendOtpTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone=binding.phoneET.getText().toString().trim();
                if(TextUtils.isEmpty(phone))
                {
                    Toast.makeText(MainActivity.this, "Please Enter Phone Number", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    resendVerificationCode(phone);
                }
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code=binding.codeET.getText().toString().trim();
                if(TextUtils.isEmpty(code))
                {
                    Toast.makeText(MainActivity.this, "Please enter verifiacation code...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    verifyPhoneNumberWithCode(mVerificationId,code);
                }

            }
        });

    }

    private void startPhoneNumberVerification(String phone) {
        dialog.setMessage("Verifying Phone Number");
        dialog.show();

        PhoneAuthOptions options=  PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode(String phone) {
        dialog.setMessage("Resending Code");
        dialog.show();
        PhoneAuthOptions options=PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L,TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .setForceResendingToken(mResendingToken)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneNumberWithCode(String mVerificationId, String code) {
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(mVerificationId,code);

        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        dialog.setMessage("Logging In");
        dialog.show();

        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
           if(task.isSuccessful())
           {
               dialog.dismiss();
               String phone=mAuth.getCurrentUser().getPhoneNumber();
               Toast.makeText(MainActivity.this, "Logging In as"+phone, Toast.LENGTH_SHORT).show();
           }
           else
           {
               dialog.dismiss();
               Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
           }
            }
        });
    }


}