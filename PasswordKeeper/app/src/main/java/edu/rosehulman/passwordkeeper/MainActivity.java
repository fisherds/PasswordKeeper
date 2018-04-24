package edu.rosehulman.passwordkeeper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginListener,
    PasswordFragment.OnLogoutListener, GoogleApiClient.OnConnectionFailedListener {

  private static final int RC_GOOGLE_LOGIN = 42;
  private FirebaseAuth mAuth;
  private FirebaseAuth.AuthStateListener mAuthStateListener;
  private OnCompleteListener mOnCompleteListener;
  
  private GoogleApiClient mGoogleApiClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
//    switchToPasswordFragment("users");

    mAuth = FirebaseAuth.getInstance();
    initializeListeners();
    setupGoogleSignin();
  }

  private void setupGoogleSignin() {
    GoogleSignInOptions gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build();
    
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .enableAutoManage(this, this)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gOptions)
        .build();
  }

  private void initializeListeners() {
    mAuthStateListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
          Log.d(Constants.TAG, "Signed in as UID = " + user.getUid());
          switchToPasswordFragment("users/" + user.getUid());
        } else {
          Log.d(Constants.TAG, "Not signed in");
          switchToLoginFragment();
        }
      }
    };

    mOnCompleteListener = new OnCompleteListener() {
      @Override
      public void onComplete(@NonNull Task task) {
        if (!task.isSuccessful()) {
          showLoginError("Log in failed!");
        }
      }
    };
  }

  @Override
  protected void onStart() {
    super.onStart();
    mAuth.addAuthStateListener(mAuthStateListener);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mAuthStateListener != null) {
      mAuth.removeAuthStateListener(mAuthStateListener);
    }
  }

  @Override
  public void onLogin(String email, String password) {
    //DONE: Log user in with username & password
    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mOnCompleteListener);
  }

  @Override
  public void onGoogleLogin() {
    Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
    startActivityForResult(intent, RC_GOOGLE_LOGIN);
  }

  @Override
  public void onRosefireLogin() {
    //TODO: Log user in with RoseFire account
  }

  @Override
  public void onLogout() {
    //DONE: Log the user out.
    mAuth.signOut();
  }

  // MARK: Provided Helper Methods
  private void switchToLoginFragment() {
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ft.replace(R.id.fragment, new LoginFragment(), "Login");
    ft.commit();
  }

  private void switchToPasswordFragment(String path) {
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    Fragment passwordFragment = new PasswordFragment();
    Bundle args = new Bundle();
    args.putString(Constants.FIREBASE_PATH, path);
    passwordFragment.setArguments(args);
    ft.replace(R.id.fragment, passwordFragment, "Passwords");
    ft.commit();
  }

  private void showLoginError(String message) {
    LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentByTag("Login");
    loginFragment.onLoginError(message);
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    Log.e(Constants.TAG, "Google sign in failed!");
    AlertDialog alert = new AlertDialog.Builder(this).setTitle("Error!").create();
    alert.show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == RC_GOOGLE_LOGIN && resultCode == Activity.RESULT_OK) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      if (result.isSuccess()) {
        GoogleSignInAccount account = result.getSignInAccount();

        Log.d(Constants.TAG, "You are now signed in with Google " + account.getEmail());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(mOnCompleteListener);
      } else {
        showLoginError("Google authentication failed");
      }

    }


    super.onActivityResult(requestCode, resultCode, data);

  }
}
