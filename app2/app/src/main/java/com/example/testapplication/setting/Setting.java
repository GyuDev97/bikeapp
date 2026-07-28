package com.example.testapplication.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.testapplication.R;
import com.example.testapplication.ui.BaseFragment;
import com.example.testapplication.ui.member.LoginActivity;
import com.example.testapplication.ui.riding.StartActivity;
import com.squareup.picasso.Picasso;

public class Setting extends BaseFragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public Setting() {
    }

    public static Setting newInstance(String param1, String param2) {
        Setting fragment = new Setting();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindProfileMenu(view);
    }

    private void bindProfileMenu(@NonNull View view) {
        SharedPreferences auto = requireActivity().getSharedPreferences("auto", Activity.MODE_PRIVATE);
        String loginId = auto.getString("LoginId", "");
        String nickname = firstNotEmpty(auto.getString("r_nickname", ""), loginId, "admin");
        String imageName = auto.getString("r_image", "");
        String phone = auto.getString("sosphone", "");
        String email = loginId.contains("@") ? loginId : "이메일 미등록";

        TextView profileName = view.findViewById(R.id.profile_name);
        TextView rowNameValue = view.findViewById(R.id.row_name_value);
        TextView rowEmailValue = view.findViewById(R.id.row_email_value);
        TextView rowPhoneValue = view.findViewById(R.id.row_phone_value);
        TextView rideCount = view.findViewById(R.id.profile_ride_count);
        TextView rideDistance = view.findViewById(R.id.profile_ride_distance);
        ImageView profileImage = view.findViewById(R.id.profile_image);

        profileName.setText(nickname);
        rowNameValue.setText(nickname);
        rowEmailValue.setText(email);
        rowPhoneValue.setText(firstNotEmpty(phone, "미등록"));
        rideCount.setText(firstNotEmpty(auto.getString("ride_count", ""), "12 회"));
        rideDistance.setText(firstNotEmpty(auto.getString("ride_distance", ""), "128 km"));

        if (!imageName.isEmpty()) {
            Picasso.get()
                    .load("http://gyudrive.ipdisk.co.kr:8080/bike/app/getGPX/rider/" + imageName)
                    .placeholder(R.drawable.m_logo)
                    .error(R.drawable.m_logo)
                    .into(profileImage);
        }

        view.findViewById(R.id.row_phone).setOnClickListener(v -> showPhoneDialog(auto, rowPhoneValue));
        view.findViewById(R.id.row_logout).setOnClickListener(v -> logout(auto));
        view.findViewById(R.id.bottom_nav_home).setOnClickListener(v -> navigateTo(R.id.nav_home));
        view.findViewById(R.id.bottom_nav_records).setOnClickListener(v -> navigateTo(R.id.nav_records));
        view.findViewById(R.id.bottom_nav_course).setOnClickListener(v -> navigateTo(R.id.nav_course_view));
        view.findViewById(R.id.bottom_nav_ride).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), StartActivity.class))
        );
        view.findViewById(R.id.profile_menu_courses).setOnClickListener(v -> navigateTo(R.id.nav_course_view));
        view.findViewById(R.id.profile_menu_favorite).setOnClickListener(v -> navigateTo(R.id.nav_course_scrap));
        TextView profile = view.findViewById(R.id.bottom_nav_profile);
        profile.setTextColor(android.graphics.Color.parseColor("#1F7A2E"));
        profile.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void navigateTo(int destinationId) {
        if (controller.getCurrentDestination() == null
                || controller.getCurrentDestination().getId() != destinationId) {
            controller.navigate(destinationId);
        }
    }

    private void showPhoneDialog(SharedPreferences auto, TextView phoneValue) {
        EditText input = new EditText(requireContext());
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setText(auto.getString("sosphone", ""));
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(requireContext())
                .setTitle("전화번호 설정")
                .setView(input)
                .setNegativeButton("취소", null)
                .setPositiveButton("저장", (dialog, which) -> {
                    String phone = input.getText().toString().trim();
                    auto.edit().putString("sosphone", phone).apply();
                    phoneValue.setText(firstNotEmpty(phone, "미등록"));
                })
                .show();
    }

    private void logout(SharedPreferences auto) {
        auto.edit().clear().apply();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private String firstNotEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return "";
    }

    @Override
    public void onResume() {
        super.onResume();
        View appBar = requireActivity().findViewById(R.id.app_bar_layout);
        if (appBar != null) {
            appBar.setVisibility(View.GONE);
        }
    }
}
