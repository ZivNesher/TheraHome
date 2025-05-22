package com.ziv.therahome;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ziv.therahome.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class AuthManager {
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference usersRef;
    private Context context;
    private UserManagerCallback userManagerCallback;
    private MainActivity mainActivity;

    public AuthManager(Context context, UserManagerCallback userManagerCallback, MainActivity mainActivity) {
        this.context = context;
        this.userManagerCallback = userManagerCallback;
        this.mainActivity = mainActivity;
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return mGoogleSignInClient;
    }

    public void registerUser(String email, String password, String Id, String firstName, String surName,
                             String dateOfBirth, String weight, String height, OnRegisterSuccessListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((MainActivity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                UserManager userManager = new UserManager(userManagerCallback);
                                userManager.saveUserData(userId, email, password, Id, firstName, surName, dateOfBirth, weight, height);
                                sendVerificationEmail(user);
                                if (listener != null) {
                                    listener.onRegisterSuccess(user);
                                }
                            }
                        } else {
                            Toast.makeText(context, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Verification email sent. Please check your email.", Toast.LENGTH_SHORT).show();
                            userManagerCallback.goToLoginScreen();
                        } else {
                            Toast.makeText(context, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((MainActivity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.reload().addOnCompleteListener(reloadTask -> {
                                    if (user.isEmailVerified()) {
                                        UserManager userManager = new UserManager(userManagerCallback);
                                        userManager.checkUserData(user.getUid(), context);
                                    } else {
                                        Toast.makeText(context, "Please verify your email address.", Toast.LENGTH_LONG).show();
                                        mAuth.signOut();
                                    }
                                });
                            } else {
                                Toast.makeText(context, "Authentication succeeded, but user is null.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(context, "Invalid password.", Toast.LENGTH_LONG).show();
                            } catch (FirebaseAuthInvalidUserException e) {
                                Toast.makeText(context, "Invalid email.", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(context, context.getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((MainActivity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            String email = acct.getEmail(); // Extract email from GoogleSignInAccount
                            UserManager userManager = new UserManager(userManagerCallback);
                            userManager.checkUserData(user.getUid(), email, context);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(context, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void handleGoogleSignIn(Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Toast.makeText(context, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
