package com.finalyear.networkservicediscovery.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.finalyear.networkservicediscovery.R;
import com.finalyear.networkservicediscovery.activities.SendFiileActivity;

/**
 * Created by KayO on 09/03/2017.
 */

public class UserDiscoveryFragment extends Fragment {
    private ListView lvDiscoveryList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_user_discovery, container, false);

        init(view);

        lvDiscoveryList.setAdapter(((SendFiileActivity)getActivity()).getDiscoveryListAdapter());
        return view;
    }

    private void init(View view) {
        lvDiscoveryList = (ListView) view.findViewById(R.id.lvDiscoveryList);
    }
}
