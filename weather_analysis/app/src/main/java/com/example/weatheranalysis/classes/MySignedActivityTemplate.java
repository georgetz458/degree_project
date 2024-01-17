package com.example.weatheranalysis.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.weatheranalysis.InfoActivity;
import com.example.weatheranalysis.LanguageActivity;
import com.example.weatheranalysis.LoginActivity;
import com.example.weatheranalysis.MainActivity;
import com.example.weatheranalysis.R;
import com.example.weatheranalysis.classes.util.GeneralUtils;
import com.example.weatheranalysis.classes.util.UploadUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
//template για activities που ο χρήστης είναι συνδεδεμένος
//ορίζεται το μενού τέτοιων activities
public abstract class MySignedActivityTemplate extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private boolean doubleBackToExitPressedOnce;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout_button) {//Logout
            logOut();
            return true;
        }
        if(item.getItemId() == android.R.id.home){
            if(drawerLayout.isDrawerVisible(findViewById(R.id.navigation_id))){
                drawerLayout.closeDrawers();
            }
            else{
                drawerLayout.openDrawer(GravityCompat.START);
            }

        }
        return super.onOptionsItemSelected(item);
    }
    protected void setMenu(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);
        setNavigationDrawer();

    }

    private void setNavigationDrawer(){


        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_id);
        //add title to navigation drawer
        View headerView = navigationView.getHeaderView(0);
        TextView menuTitle = headerView.findViewById(R.id.menu_title);
        String email = getSharedPreferences(getString(R.string.preferences_name), Context.MODE_PRIVATE).getString("email", "noEmail");
        if (!email.equals("noEmail")){
            menuTitle.setText(email);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home_button:
                        return homeButtonAction();

                    case R.id.info_button:
                        return infoButtonAction();
                    case R.id.language_button:
                        return languageButtonAction();
                    case R.id.logout_button:
                        logOut();
                        return true;
                }
                return true;
            }
        });
    }
    //αντικαθίσταται απο την εκάστοτε τάξη
    protected abstract boolean homeButtonAction();
    protected abstract boolean infoButtonAction();
    protected abstract boolean languageButtonAction();

    protected void closeDrawer(){
        drawerLayout.closeDrawers();
    }
    protected void startMainActivity(){
        GeneralUtils.goToActivity(this, MainActivity.class);
    }
    protected void startInfoActivity(){
        GeneralUtils.goToActivity(this, InfoActivity.class);

    }
    protected void startLanguageActivity(){
        GeneralUtils.goToActivity(this, LanguageActivity.class);


    }

    protected void logOut() {
        String preferences_name = getString(R.string.preferences_name);
        SharedPreferences preferences = getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
        UploadUtils.logOut(FirebaseAuth.getInstance());
        GeneralUtils.goToActivity(this, LoginActivity.class);

    }
    //με το διπλό πάτημα του πίσω κουμπιού τερματίζει η εφαρμογή
    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {

            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_back_button_again, Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }


}
