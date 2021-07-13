package com.chenenyu.router.module;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.InjectParam;
import com.chenenyu.router.annotation.Route;
import com.chenenyu.router.module1.databinding.FragmentModule1Binding;


/**
 * A simple {@link Fragment} subclass.
 */
@Route("fragment1")
public class Module1Fragment extends Fragment {
    // Test param inject, not been used.
    @InjectParam
    int test1 = 123; // test default value
    @InjectParam(key = "test22")
    char[] test2;

    private FragmentModule1Binding binding;

    public Module1Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentModule1Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnGo.setOnClickListener(v -> {
            Router.build("module1").go(Module1Fragment.this);
            requireActivity().finish();
        });

        Router.injectParams(Module1Fragment.this);

        Log.d(Module1Fragment.class.getSimpleName(), "test1=" + test1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
