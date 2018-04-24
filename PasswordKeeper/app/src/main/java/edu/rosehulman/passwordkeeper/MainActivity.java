package edu.rosehulman.passwordkeeper;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnLoginListener, PasswordFragment.OnLogoutListener {

  private FirebaseAuth mAuth;
  private FirebaseAuth.AuthStateListener mAuthStateListener;
  private OnCompleteListener mOnCompleteListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
//    switchToPasswordFragment("users");

    mAuth = FirebaseAuth.getInstance();
    initializeListeners();
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
    //TODO: Log user in with Google account
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

}
