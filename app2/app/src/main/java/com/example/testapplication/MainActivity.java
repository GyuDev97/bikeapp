package com.example.testapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.NavigationMenuItemView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ImageView img;
    private TextView lay;
    private View header;
    private View view;
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
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_records, R.id.nav_course_view, R.id.nav_course_search, R.id.nav_course_scrap,
                R.id.nav_competition_view, R.id.nav_mission_view, R.id.nav_danger_view)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        MainViewModel model = new ViewModelProvider(this).get(MainViewModel.class);

        navigationView.findViewById(R.id.infouserid);
        header = navigationView.getHeaderView(0);
        lay = (TextView) header.findViewById(R.id.infouserid);
        userImage = (ImageView) header.findViewById(R.id.user_image);

        auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        model.r_Id.setValue(auto.getString("LoginId", ""));
        model.r_Image.setValue(auto.getString("r_image", ""));
        model.r_Nickname.setValue(auto.getString("r_nickname", ""));
        model.r_ClubName.setValue(auto.getString("r_clubname", ""));
        lay.setText(model.r_Nickname.getValue() + "님 환영합니다");

        Picasso.get().load("http://13.209.229.237:8080/app/getGPX/rider/" + model.r_Image.getValue())
                .into(userImage);

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            menuItem.setChecked(true);
            drawer.closeDrawers();

            int id = menuItem.getItemId();
            switch (id) {
                case R.id.nav_records:
                    navController.navigate(R.id.action_nav_home_to_nav_records);
                    break;
                case R.id.nav_course_view:
                    navController.navigate(R.id.action_nav_home_to_nav_course_view);
                    break;
                case R.id.nav_course_search:
                    navController.navigate(R.id.action_nav_home_to_nav_course_search);
                    break;
                case R.id.nav_course_scrap:
                    navController.navigate(R.id.action_nav_home_to_nav_course_scrap);
                    break;
                case R.id.nav_competition_view:
                    navController.navigate(R.id.action_nav_home_to_nav_competition_view);
                    break;
                case R.id.nav_mission_view:
                    navController.navigate(R.id.action_nav_home_to_nav_mission_view);
                    break;
                case R.id.friend_invite:
                    Intent friend = new Intent();
                    friend.setAction(Intent.ACTION_SEND);
                    friend.setType("text/plain");
                    friend.putExtra(Intent.EXTRA_SUBJECT, model.r_Nickname.getValue() + "님이 귀하를 초대합니다. 앱 설치하기");
                    friend.putExtra(Intent.EXTRA_TEXT, "tmarket://details?id=com.capston.mtbcraft");

                    Intent chooser = Intent.createChooser(friend, "초대하기");
                    startActivity(chooser);
                    break;
                case R.id.nav_danger_view:
                    navController.navigate(R.id.action_nav_home_to_nav_danger_view);
                    break;
                case R.id.nav_sett:
                    navController.navigate(R.id.action_nav_home_to_setting);
                    break;
            }
            return true;
        });

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("키해시는 :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
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