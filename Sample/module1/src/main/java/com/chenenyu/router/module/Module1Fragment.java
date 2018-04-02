package com.chenenyu.router.module;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.InjectParam;
import com.chenenyu.router.annotation.Route;
import com.chenenyu.router.module1.R;

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

    public Module1Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_module1, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getView().findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.build("module1").go(Module1Fragment.this);
                getActivity().finish();
            }
        });

        Router.injectParams(Module1Fragment.this);

        Log.d(Module1Fragment.class.getSimpleName(), "test1=" + test1);
    }
}
