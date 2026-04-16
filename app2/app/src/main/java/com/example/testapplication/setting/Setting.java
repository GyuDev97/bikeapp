package com.example.testapplication.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.testapplication.R;
import com.example.testapplication.ui.BaseFragment;
import com.example.testapplication.ui.member.LoginActivity;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Setting#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Setting extends BaseFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Setting() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment setting.
     */
    // TODO: Rename and change types and number of parameters
    public static Setting newInstance(String param1, String param2) {
        Setting fragment = new Setting();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view != null) {
            bindProfileMenu(view);
            return;
        }

        Button saveBt;
        saveBt = (Button) view.findViewById(R.id.save_bt);
        EditText editText;
        editText = (EditText) view.findViewById(R.id.phone_num);
        saveBt.setOnClickListener(v ->{
            //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.
            String text = editText.getText().toString(); // 사용자가 입력한 저장할 데이터


            SharedPreferences auto = getActivity().getSharedPreferences("auto", Activity.MODE_PRIVATE);
            SharedPreferences.Editor autoLogin = auto.edit();
            autoLogin.putString("sosphone", text);
            autoLogin.commit();
            getActivity().finish();
        });
    }



    private void bindProfileMenu(@NonNull View view) {
        SharedPreferences auto = requireActivity().getSharedPreferences("auto", Activity.MODE_PRIVATE);
        String loginId = auto.getString("LoginId", "");
        String nickname = firstNotEmpty(auto.getString("r_nickname", ""), loginId, "김철수");
        String imageName = auto.getString("r_image", "");
        String phone = auto.getString("sosphone", "");
        String email = loginId.contains("@") ? loginId : "이메일 미등록";

        TextView profileName = view.findViewById(R.id.profile_name);
        TextView profileEmail = view.findViewById(R.id.profile_email);
        TextView rowNameValue = view.findViewById(R.id.row_name_value);
        TextView rowEmailValue = view.findViewById(R.id.row_email_value);
        TextView rowPhoneValue = view.findViewById(R.id.row_phone_value);
        ImageView profileImage = view.findViewById(R.id.profile_image);

        profileName.setText(nickname);
        profileEmail.setText(email);
        rowNameValue.setText(nickname);
        rowEmailValue.setText(email);
        rowPhoneValue.setText(firstNotEmpty(phone, "미등록"));

        if (!imageName.isEmpty()) {
            Picasso.get()
                    .load("http://gyudrive.ipdisk.co.kr:8080/bike/app/getGPX/rider/" + imageName)
                    .placeholder(R.drawable.m_logo)
                    .error(R.drawable.m_logo)
                    .into(profileImage);
        }

        view.findViewById(R.id.row_phone).setOnClickListener(v -> showPhoneDialog(auto, rowPhoneValue));
        view.findViewById(R.id.row_logout).setOnClickListener(v -> logout(auto));
    }

    private void showPhoneDialog(SharedPreferences auto, TextView phoneValue) {
        EditText input = new EditText(requireContext());
        input.setSingleLine(true);
        input.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_profile_setting, container, false);
    }
}
