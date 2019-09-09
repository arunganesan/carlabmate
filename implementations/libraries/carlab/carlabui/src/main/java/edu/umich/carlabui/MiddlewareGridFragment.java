package edu.umich.carlabui;

import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import edu.umich.carlab.CLService;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.io.AppLoader;
import edu.umich.carlab.loadable.App;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.umich.carlabui.AppsAdapter.AppState.ACTIVE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MiddlewareGridFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MiddlewareGridFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MiddlewareGridFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    boolean mBound = false;
    CLService carlabService;

    Map<String, AppViewFragment> appViewFragments;
    List<AppsAdapter.AppModel> appModels;
    Map<String, Integer> appModelIndexMap;
    AppsAdapter appsAdapter;

    private OnFragmentInteractionListener mListener;
    final String TAG = "middleware_grid";
    public MiddlewareGridFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MiddlewareGridFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MiddlewareGridFragment newInstance(String param1, String param2) {
        MiddlewareGridFragment fragment = new MiddlewareGridFragment();
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

        appViewFragments = new HashMap<>();
    }

    Fragment getFragmentFor(String classname) {
        if (!appViewFragments.containsKey(classname))
            appViewFragments.put(classname, AppViewFragment.newInstance(classname));
        return appViewFragments.get(classname);
    }


    /**
     * Once we receive any app state updates, we will change the app state.
     */
    IntentFilter appStateIntentFilter = new IntentFilter(edu.umich.carlab.Constants.INTENT_APP_STATE_UPDATE);
    private AdapterView.OnItemClickListener openAppDetails = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            transaction.replace(R.id.main_wrapper, getFragmentFor(appModels.get(position).className));
            transaction.addToBackStack(appModels.get(position).className);
            transaction.commit();
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout layout = (FrameLayout) inflater.inflate(
                R.layout.fragment_middleware_grid,
                container,
                false);
        appModels = new ArrayList<>();
        appModelIndexMap = new HashMap<>();

        GridView middlewareGrid = layout.findViewById(R.id.middleware_grid);

        // If CarLab is running, we can create live middleware links
        // Otherwise, we will just use the static app loader
        List<App> allApps = AppLoader.getInstance().instantiateApps(null, null);

        for (App app : allApps) {
            if (!app.foregroundApp)
                continue;

            AppsAdapter.AppState appState = ACTIVE;
            appModels.add(new AppsAdapter.AppModel(
                    app.getName(),
                    app.getClass().getCanonicalName(),
                    appState));
        }

        // This index is useful later
        for (int i = 0; i < appModels.size(); i++) {
            String classname = appModels.get(i).className;
            appModelIndexMap.put(classname, i);
        }

        appsAdapter = new AppsAdapter(getActivity(), appModels);
        middlewareGrid.setAdapter(appsAdapter);
        middlewareGrid.setOnItemClickListener(openAppDetails);
        return layout;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().bindService(
                new Intent(
                        getContext(),
                        CLService.class),
                mConnection,
                Context.BIND_AUTO_CREATE);
        getContext().registerReceiver(appStateReceiver, appStateIntentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBound) {
            getContext().unbindService(mConnection);
            mBound = false;
        }

        getContext().unregisterReceiver(appStateReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    private BroadcastReceiver appStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //activeApps = prefs.getStringSet(Constants.ACTIVE_APPS_KEY, new HashSet<String>());

            String appClassName = intent.getStringExtra("appClassName");
            DataMarshal.MessageType appState = (DataMarshal.MessageType) intent.getSerializableExtra("appState");

            // This means we haven't set up this app yet
            if (appModelIndexMap == null) return;
            if (!appModelIndexMap.containsKey(appClassName))
                return;

            int appIndex = appModelIndexMap.get(appClassName);

            switch (appState) {
                case ERROR:
                    appModels.get(appIndex).state = AppsAdapter.AppState.ERROR;
                    break;
                case DATA:
                    appModels.get(appIndex).state = AppsAdapter.AppState.DATA;
                    break;
                case STATUS:
                    appModels.get(appIndex).state = AppsAdapter.AppState.PROCESSING;
            }

            appsAdapter.notifyDataSetChanged();
            Log.v(TAG, "Updating state color");
        }
    };

    /************************* CarLab service binding ************************/
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CLService.LocalBinder binder = (CLService.LocalBinder) service;
            carlabService = binder.getService();
            mBound = true;
            // TODO Initialize the middleware view if that is currently loaded
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            carlabService = null;
        }
    };

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
