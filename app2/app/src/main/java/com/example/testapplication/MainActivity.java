package com.example.testapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.testapplication.dto.Server;
import com.example.testapplication.ui.member.LoginActivity;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private TextView lay;
    private TextView userMeta;
    private View header;
    private ImageView userImage;
    private SharedPreferences auto;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_records, R.id.nav_course_view, R.id.nav_course_search, R.id.nav_course_scrap,
                R.id.nav_competition_view, R.id.nav_mission_view, R.id.nav_danger_view, R.id.setting)
                .setDrawerLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        toolbar.setTitle("Routain");
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> toolbar.setTitle("Routain"));

        MainViewModel model = new ViewModelProvider(this).get(MainViewModel.class);
        auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        model.r_Id.setValue(auto.getString("LoginId", ""));
        model.r_Image.setValue(auto.getString("r_image", ""));
        model.r_Nickname.setValue(auto.getString("r_nickname", ""));
        model.r_ClubName.setValue(auto.getString("r_clubname", ""));

        header = navigationView.getHeaderView(0);
        lay = header.findViewById(R.id.infouserid);
        userMeta = header.findViewById(R.id.infousermeta);
        userImage = header.findViewById(R.id.user_image);
        bindDrawerHeader(model);

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            menuItem.setChecked(true);
            drawer.closeDrawers();

            int id = menuItem.getItemId();
            switch (id) {
                case R.id.nav_home:
                    navigateTo(navController, R.id.nav_home);
                    break;
                case R.id.nav_records:
                    navigateTo(navController, R.id.nav_records);
                    break;
                case R.id.nav_course_view:
                    navigateTo(navController, R.id.nav_course_view);
                    break;
                case R.id.nav_course_search:
                    navigateTo(navController, R.id.nav_course_search);
                    break;
                case R.id.nav_course_scrap:
                    navigateTo(navController, R.id.nav_course_scrap);
                    break;
                case R.id.nav_competition_view:
                    navigateTo(navController, R.id.nav_competition_view);
                    break;
                case R.id.nav_mission_view:
                    navigateTo(navController, R.id.nav_mission_view);
                    break;
                case R.id.nav_danger_view:
                    navigateTo(navController, R.id.nav_danger_view);
                    break;
                case R.id.nav_sett:
                    navigateTo(navController, R.id.setting);
                    break;
                case R.id.friend_invite:
                    shareInvite(model);
                    break;
                case R.id.nav_help:
                    Toast.makeText(this, "\uB3C4\uC6C0\uB9D0\uC740 \uC900\uBE44 \uC911\uC785\uB2C8\uB2E4.", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_logout:
                    logout();
                    break;
            }
            return true;
        });

        logKeyHash();
    }

    private void bindDrawerHeader(MainViewModel model) {
        String riderId = valueOrDefault(model.r_Id.getValue(), "admin");
        String nickname = valueOrDefault(model.r_Nickname.getValue(), riderId);
        String clubName = valueOrDefault(model.r_ClubName.getValue(), "Routain");
        lay.setText("\uC548\uB155\uD558\uC138\uC694, " + riderId + "\uB2D8");
        userMeta.setText("(" + nickname + ") " + clubName + " \uB77C\uC774\uB354");

        String imageName = model.r_Image.getValue();
        if (imageName != null && imageName.length() > 0) {
            Server server = new Server();
            Picasso.get().load(server.getIP() + "/app/getGPX/rider/" + imageName).into(userImage);
        } else {
            userImage.setImageResource(R.drawable.m_logo);
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    private void navigateTo(NavController navController, int destinationId) {
        if (navController.getCurrentDestination() == null || navController.getCurrentDestination().getId() != destinationId) {
            navController.navigate(destinationId);
        }
    }

    private void shareInvite(MainViewModel model) {
        String nickname = valueOrDefault(model.r_Nickname.getValue(), "Routain");
        Intent friend = new Intent();
        friend.setAction(Intent.ACTION_SEND);
        friend.setType("text/plain");
        friend.putExtra(Intent.EXTRA_SUBJECT, nickname + "\uB2D8\uC774 Routain\uC5D0 \uCD08\uB300\uD569\uB2C8\uB2E4");
        friend.putExtra(Intent.EXTRA_TEXT, "Routain\uC5D0\uC11C \uD568\uAED8 \uB77C\uC774\uB529\uD574\uC694.");
        Intent chooser = Intent.createChooser(friend, "\uCE5C\uAD6C\uCD08\uB300\uD558\uAE30");
        startActivity(chooser);
    }

    private void logout() {
        auto.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
