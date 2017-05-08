package com.chenenyu.router.module;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.chenenyu.router.RouteCallback;
import com.chenenyu.router.RouteResult;
import com.chenenyu.router.Router;
import com.chenenyu.router.annotation.Route;
import com.chenenyu.router.module1.R;

/**
 * A simple {@link Fragment} subclass.
 */
@Route("fragment1")
public class Module1Fragment extends Fragment {

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
                Router.build("module1").go(getContext(), new RouteCallback() {
                    @Override
                    public void callback(RouteResult state, Uri uri, String message) {
                        Toast.makeText(getContext(), state.name() + ", " + uri, Toast.LENGTH_SHORT).show();
                    }
                });
                getActivity().finish();
            }
        });
    }
}
