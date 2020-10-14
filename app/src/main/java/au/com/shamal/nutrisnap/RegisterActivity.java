package au.com.shamal.nutrisnap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameTxt;
    private EditText emailAddressTxt;
    private EditText firstNameTxt;
    private EditText lastNameTxt;
    private EditText passwordTxt;
    private EditText confirmPasswordTxt;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    // Constants
    public static final String PREFS = "Prefs";
    public static final String DISPLAY_NAME_KEY = "username";
    private final static String APP = "NutriSnap";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        usernameTxt = (EditText) findViewById(R.id.username_txt);
        emailAddressTxt = (EditText) findViewById(R.id.email_txt);
        firstNameTxt = (EditText) findViewById(R.id.first_name_txt);
        lastNameTxt = (EditText) findViewById(R.id.last_name_txt);
        passwordTxt = (EditText) findViewById(R.id.password_txt);
        confirmPasswordTxt = (EditText) findViewById(R.id.confirm_password_txt);
        confirmPasswordTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.integer.register_form_finished || actionId == EditorInfo.IME_NULL) {
                    attemptRegistration();
                    return true;
                }
                return false;

            }

        });

        Button backToLoginBtn = (Button) findViewById(R.id.back_to_login_btn);
        backToLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
            }
        });

        Button registerBtn = (Button) findViewById(R.id.register_btn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegistration();
            }
        });
    }

    private void attemptRegistration() {

        // Reset errors displayed in the form.
        usernameTxt.setError(null);
        emailAddressTxt.setError(null);
        firstNameTxt.setError(null);
        lastNameTxt.setError(null);
        passwordTxt.setError(null);
        confirmPasswordTxt.setError(null);

        // Store values at the time of the login attempt.
        String email = emailAddressTxt.getText().toString();
        String firstName = firstNameTxt.getText().toString();
        String lastName = lastNameTxt.getText().toString();
        String password = passwordTxt.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            passwordTxt.setError(getString(R.string.error_invalid_password));
            focusView = passwordTxt;
            cancel = true;
        }

        // Check the first name field
        if (TextUtils.isEmpty(firstName)){
            firstNameTxt.setError(getString(R.string.error_invalid_first_name));
            focusView = firstNameTxt;
            cancel = true;
        }
        // Check the last name field
        if (TextUtils.isEmpty(lastName)){
            lastNameTxt.setError(getString(R.string.error_invalid_last_name));
            focusView = lastNameTxt;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailAddressTxt.setError(getString(R.string.error_field_required));
            focusView = emailAddressTxt;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailAddressTxt.setError(getString(R.string.error_invalid_email));
            focusView = emailAddressTxt;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            createFirebaseUser();
        }
    }

    private boolean isEmailValid(String email) {
        // You can add more checking logic here.
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {

        String confirmPassword = confirmPasswordTxt.getText().toString();
        return confirmPassword.equals(password) && password.length() > 4;
    }

    // Create a Firebase user
    private void createFirebaseUser() {
        String email = emailAddressTxt.getText().toString();
        String password = passwordTxt.getText().toString();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(APP, "createUser onComplete: " + task.isSuccessful());
                if (!task.isSuccessful()) {
                    Log.d(APP, "user creation failed");
                    showErrorDialog("Registration attempt failed");
                } else {
                    saveDisplayName();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });

    }

    // Save the display name to Shared Preferences
    private void saveDisplayName() {
        String username = usernameTxt.getText().toString();
        String email = emailAddressTxt.getText().toString();
        String firstName = firstNameTxt.getText().toString();
        String lastName = lastNameTxt.getText().toString();

        SharedPreferences.Editor editor = getSharedPreferences(PREFS, 0).edit();
        editor.putString(DISPLAY_NAME_KEY, username);
        editor.putString("email", email);
        editor.putString("first_name", firstName);
        editor.putString("last_name", lastName);
        editor.apply();
        AppUser user = new AppUser(username, email, firstName, lastName);
        databaseReference.child("user").push().setValue(user);
    }

    // Create an alert dialog to show in case registration failed
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}