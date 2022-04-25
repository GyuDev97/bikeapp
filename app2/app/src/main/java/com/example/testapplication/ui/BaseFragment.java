package com.example.testapplication.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.testapplication.MainViewModel;
import com.example.testapplication.R;
import com.example.testapplication.net.Server;
import com.example.testapplication.net.ServerApi;

public class BaseFragment extends Fragment {

    protected MainViewModel model;
    protected NavController controller;
    protected ServerApi serverApi;
    SharedPreferences auto;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        controller = Navigation.findNavController(view);
        serverApi = Server.getInstance().getApi();
    }

    protected void showServerFailure(){
        Toast.makeText(requireContext(), "Server와 연결이 되지 않습니다", Toast.LENGTH_SHORT).show();
    }
}
