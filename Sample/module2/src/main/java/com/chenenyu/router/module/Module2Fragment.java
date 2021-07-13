package com.chenenyu.router.module;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.Route;
import com.chenenyu.router.module2.databinding.FragmentModule2Binding;

import org.jetbrains.annotations.NotNull;

/**
 * A simple {@link Fragment} subclass.
 */
@Route("fragment2")
public class Module2Fragment extends Fragment {
    private FragmentModule2Binding binding;

    public Module2Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentModule2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnGo.setOnClickListener(v -> {
            Router.build("module2").go(Module2Fragment.this);
            requireActivity().finish();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
