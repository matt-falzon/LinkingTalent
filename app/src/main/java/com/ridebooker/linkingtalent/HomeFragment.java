package com.ridebooker.linkingtalent;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ridebooker.linkingtalent.datatypes.Job;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment
{

    TextView tvHomeTitle, tvfacebookimg;

    public HomeFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvHomeTitle = (TextView) view.findViewById(R.id.tvHomeTitle);
        tvfacebookimg = (TextView) view.findViewById(R.id.facebook_img_uri);

        tvHomeTitle.setText("Welcome back " + MainActivity.user.getName() + "!");
        // Inflate the layout for this fragment
        tvfacebookimg.setText(getArguments().getString("facebook"));
        Log.d("sss", tvfacebookimg.getText().toString());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }


}
