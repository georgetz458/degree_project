package com.example.weatheranalysis;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.android.volley.Request;

import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.weatheranalysis.classes.util.GeneralUtils;
import com.example.weatheranalysis.classes.LoadingDialog;
import com.example.weatheranalysis.classes.util.UploadUtils;
import com.example.weatheranalysis.classes.singleton.MySingletonVolley;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//login/register activity
public class LoginActivity extends AppCompatActivity {
    private final String loginUrl = UploadUtils.HostURL +"/login";
    private final String registerUrl = UploadUtils.HostURL +"/register";
    private EditText emailText, passText, confirmPassText;
    public static String token;
    private TextView switchActionText;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private boolean isRegular = true;
    private LoadingDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailText = findViewById(R.id.editTextTextEmailAddress);
        passText = findViewById(R.id.editTextTextPassword);
        confirmPassText = findViewById(R.id.editTextTextConfirmPassword);
        confirmPassText.setVisibility(View.GONE);
        loginButton = findViewById(R.id.button);
        switchActionText = findViewById(R.id.switchAction);
        switchActionText
                .setPaintFlags(switchActionText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        //έαν το checkbox επιλέχθηκε τότε γίνεται register το όνομα στο κουμπί
        switchAction();
        //TODO comment handleSSLHandshake when in production and uncomment in dev
        //UploadUtils.handleSSLHandshake();
        mAuth  =FirebaseAuth.getInstance();
        if(UploadUtils.isSingedIn(mAuth)){
            loginSuccess(mAuth.getCurrentUser().getEmail());
        }
        loadingDialog = new LoadingDialog(this);
    }
    //εναλλαγή login/register
    private void switchAction(){
        switchActionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRegular){
                    loginButton.setText(R.string.register);
                    confirmPassText.setVisibility(View.VISIBLE);
                    switchActionText.setText(R.string.logInPrompt);
                    emailText.getText().clear();
                    passText.getText().clear();
                    isRegular = false;
                }
                else {
                    loginButton.setText(R.string.login);
                    confirmPassText.setVisibility(View.GONE);
                    switchActionText.setText(R.string.signUpPrompt);
                    emailText.getText().clear();
                    passText.getText().clear();
                    confirmPassText.getText().clear();
                    isRegular = true;
                }
            }
        });
    }
    //αποστολή request για σύνδεση (Login)
    //στέλνει JWT για την σύνδεση
    private void requestLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            addRoleClaim(false);
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            loadingDialog.removeDialog();
                            //εάν το email δεν έχει τη σωστή μορφή εμφάνισε το κατάλληλο μήνυμα
                            GeneralUtils.showMessage(LoginActivity.this, getString(R.string.error), task.getException().getMessage());
                        } else {
                            loadingDialog.removeDialog();
                            // αλλιώς εμφάνισε μήνυμα λανθασμένων στοιχείων
                            GeneralUtils.showMessage(LoginActivity.this, getString(R.string.error), Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }

                });
    }
    //αποστολή request για εγγραφή και σύνδεση (register)
    //συνδέση γίνεται αυτόματα στο backend
    //στέλνει JWT για την σύνδεση
    private void requestRegister(String email, String password) throws JSONException {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            addRoleClaim(true);


                        }
                        else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            loadingDialog.removeDialog();
                            // εάν υπάρχει ήδη το email στη firebase εμφάνισε το κατάλληλο μήνυμα
                            GeneralUtils.showMessage(LoginActivity.this, getString(R.string.error), getString(R.string.userExists));
                        } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                            loadingDialog.removeDialog();
                            // εάν ο κωδικός δεν είναι τουλάχιστον 6 χαρακτήρων εμφάνισε το κατάλληλο μήνυμα
                            GeneralUtils.showMessage(LoginActivity.this, getString(R.string.error), getString(R.string.wrongPassword));
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            loadingDialog.removeDialog();
                            //εάν το email δεν έχει τη σωστή μορφή εμφάνισε το κατάλληλο μήνυμα
                            GeneralUtils.showMessage(LoginActivity.this, getString(R.string.error),getString(R.string.wrong_format_email_message));
                        } else {
                            loadingDialog.removeDialog();
                            // αλλιώς εμφάνισε μήνυμα λανθασμένων στοιχείων
                            GeneralUtils.showMessage(LoginActivity.this, getString(R.string.error), getString(R.string.wrongCredentials));
                        }
                    }

                });
    }
    SignInClient oneTapClient;
    //αίτηση google login
    public void requestGoogleLogin(View view){

        oneTapClient = Identity.getSignInClient(this);
        BeginSignInRequest signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // server's client ID, όχι Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        //εμφάνιση accounts από εγγεγραμένους χρήστες.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();
        //εκκίνηση intent
        oneTapClient.beginSignIn(signInRequest)

                .addOnSuccessListener(new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult beginSignInResult) {
                        try {

                            startIntentSenderForResult(
                                    beginSignInResult.getPendingIntent().getIntentSender(), 0,
                                    null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            loadingDialog.removeDialog();
                            GeneralUtils.showMessage(getApplicationContext(), getString(R.string.error), "");

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.removeDialog();
                        GeneralUtils.showMessage(getApplicationContext(), getString(R.string.error), "");
                    }
                });
    }
    //είσοδος στη firebase με google credentials
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            try {
                loadingDialog.loadDialog();
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken != null) {

                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);

                    mAuth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        FirebaseUser user = mAuth.getCurrentUser();


                                        user.getIdToken(false).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                                            @Override
                                            public void onSuccess(GetTokenResult getTokenResult) {
                                                token = getTokenResult.getToken();

                                                boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                                                if (isNew) {
                                                    addRoleClaim(true);

                                                } else {
                                                    addRoleClaim(false);

                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                loadingDialog.removeDialog();
                                                GeneralUtils.showMessage(getApplicationContext(), getString(R.string.error), "");

                                            }
                                        });

                                    } else {
                                        loadingDialog.removeDialog();
                                        GeneralUtils.showMessage(getApplicationContext(), getString(R.string.error), getString(R.string.login_error));
;
                                    }
                                }
                            });
                }
            } catch (ApiException e) {
                loadingDialog.removeDialog();

            }
        }
    }


    //εάν τα πεδία  email και κωδικού είναι σωστά τότε ανάλογα με την επιλογή του χρήστη γίνεται
    //εγγραφή εάν είναι νέος χρήστης ή login εάν έχει ήδη λογαριασμό.
    public void login(View view) throws JSONException {
        loadingDialog.loadDialog();
        String email = emailText.getText().toString();
        String password = passText.getText().toString();
        boolean isEmailValid = isEmailValid(email);
        boolean isPasswordValid = isPasswordValid(password);
        if(isEmailValid && isPasswordValid){
            if (!isRegular){//εάν ο user θέλει register
                if(checkConfirmPAss(password)){
                    requestRegister(email, password);
                }else{
                    loadingDialog.removeDialog();
                    GeneralUtils.showMessage(this, "Wrong credentials", "Your password and confirmation of it were different!");

                }

            }
            else{//εάν ο user θέλει login

                requestLogin(emailText.getText().toString(), passText.getText().toString());
            }
        }else if(!isPasswordValid) {
            loadingDialog.removeDialog();
            GeneralUtils.showMessage(this, getString(R.string.wrong_credentials), getString(R.string.wrong_format_pass_message));
        }
        else {
            loadingDialog.removeDialog();
            GeneralUtils.showMessage(this, getString(R.string.wrong_credentials), getString(R.string.wrong_format_email_message));
        }

    }

    private void loginSuccess(String email) {
        loadingDialog.removeDialog();
        Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
        //flag για επιστροφή στο main activity
        intent.putExtra("introductory", true);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        sharedPreferences.edit()
                .putString("email", email)
                .apply();
        startActivity(intent);
        finish();
    }
    //εισαγωγή role claim μέσω backend σε επακόλουθα tokens
    private void addRoleClaim(boolean isNew)  {

        mAuth.getCurrentUser().getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if(task.isSuccessful()){
                    String token = task.getResult().getToken();
                    JSONObject body  = new JSONObject();
                    try {
                        body.put("token", token);
                        if(isNew){
                            body.put("role", "USER");
                        }
                    } catch (JSONException e) {
                        loadingDialog.removeDialog();

                    }
                    String url;
                    if(isNew){
                        url = registerUrl;
                    }
                    else url = loginUrl;
                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    loginSuccess(mAuth.getCurrentUser().getEmail());
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    loadingDialog.removeDialog();
                                    if(error.networkResponse == null){
                                        if(error.getClass().equals(TimeoutError.class)){
                                            loadingDialog.removeDialog();
                                            GeneralUtils.showMessage(LoginActivity.this, getString(R.string.error), getString(R.string.timeout_error));

                                        }
                                        else {
                                            loadingDialog.removeDialog();
                                            GeneralUtils.showMessage(LoginActivity.this, getString(R.string.error), "");

                                        }
                                    }
                                    else if(error.networkResponse.statusCode == 401){
                                        loadingDialog.removeDialog();
                                        GeneralUtils.showMessage(LoginActivity.this, getString(R.string.error), getString(R.string.wrong_credentials));

                                    }
                                    else {
                                        loadingDialog.removeDialog();
                                        GeneralUtils.showMessage(LoginActivity.this, getString(R.string.error), "");

                                    }
                                    mAuth.signOut();
                                }
                            });
                    MySingletonVolley.getInstance(getApplicationContext()).getRequestQueue().add(request);

                }else{
                    loadingDialog.removeDialog();
                    GeneralUtils.showMessage(LoginActivity.this, getString(R.string.error), "");
                    mAuth.signOut();
                }
            }
        });

    }
    private boolean checkConfirmPAss(String password){
        String confirmPass = confirmPassText.getText().toString();
        return confirmPass.equals(password);
    }

    //Έλεγχος Email format
    // Μέρος πριν το @ (local-part):
    // -Κεφαλαία (A-Z) και μικρά (a-z) γράμματα λατινικού αλφάβητου.
    //-ψηφία (0-9).
    // -χαρακτήρες ! # $ % & ' * + - / = ? ^ _ ` { | } ~
    //-ο χαρακτήρας . ( τελεία) αρκεί να μην βρίσκεται στην αρχή
    //ή στο τέλος του local-part και να μην είναι συνεχόμενα.
    //Μέρος μετά το @(domain name):
    // -Κεφαλαία (A-Z) και μικρά (a-z) γράμματα λατινικού αλφάβητου.
    //-ψηφία (0-9).
    // -ο χαρακτήρας _  αρκεί να μην βρίσκεται στην αρχή
    //ή στο τέλος του local-part και να μην είναι συνεχόμενα.
    //Στο τέλος του domain είναι η κατάληψη που έχει στην αρχή τελεία και στο τέλος
    //2 με 3 μικρά γράμματα
    //Δεν επιτρέπεται quotation διότι στη πράξη κανένας δεν έχει τέτοιο email
    private boolean isEmailValid(String email){
        String expression = "^[\\w!#$%&'*+-\\/=?^_`{|}~]+(\\.[\\w!#$%&'*+-\\/=?^_`{|}~]+)*@\\w+(_\\w+)*\\.[a-z]{2,3}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();

    }
    //Έλεγχος format κωδικού
    //ο κωδικός  πρέπει να έχει 1 με 32 γράμματα(μικρά ή κεφαλαία), νούμερα ή χαρακτήρες ! # $ % & ' * + - = ? ^ _ ` { | } ~
    private boolean isPasswordValid(String password){
        String expression = "^[\\w!#$%&'*+-=?^_`{|}~]{6,32}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(password);
        return  matcher.matches();
    }

}