package edu.umich.carlabui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.umich.carlab.CLService;
import edu.umich.carlab.loadable.App;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AppViewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AppViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AppViewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;
    String classname;

    boolean mBound = false;
    CLService carlabService;
    View layout;
    TextView invisibilityTextView;
    TextView middlewareTitle;
    FrameLayout middlewareContent;
    boolean drawnLayout = false;

    private OnFragmentInteractionListener mListener;

    public AppViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AppViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AppViewFragment newInstance(String classname) {
        AppViewFragment fragment = new AppViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, classname);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            classname = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_app_view, container, false);
        invisibilityTextView = layout.findViewById(R.id.not_running_text);
        middlewareTitle = layout.findViewById(R.id.middleware_title);
        middlewareContent = layout.findViewById(R.id.middleware_content);
        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        getContext().bindService(
                new Intent(
                        getContext(),
                        CLService.class),
                mConnection,
                Context.BIND_AUTO_CREATE);

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

        // FIXME For now we always set this to false and redraw the layout.
        drawnLayout = false;
        drawViewIfNeeded();
    }

    void drawViewIfNeeded() {
        if (drawnLayout) return;

        if (carlabService == null) {
            invisibilityTextView.setVisibility(VISIBLE);
            return;
        }

        App app = carlabService.getRunningApp(classname);
        if (app == null) {
            invisibilityTextView.setVisibility(VISIBLE);
            return;
        }


        middlewareTitle.setText(app.getName());
        View appView = app.initializeVisualization(getActivity());
        if (appView == null)
            invisibilityTextView.setVisibility(VISIBLE);
        else {
            invisibilityTextView.setVisibility(INVISIBLE);
            middlewareContent.addView(appView);
            drawnLayout = true;
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        if (mBound) {
            getContext().unbindService(mConnection);
            drawnLayout = false;
            mBound = false;
        }
    }

    /************************* CarLab service binding ************************/
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CLService.LocalBinder binder = (CLService.LocalBinder) service;
            carlabService = binder.getService();
            drawViewIfNeeded();
            mBound = true;
            // TODO Initialize the middleware view if that is currently loaded
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            carlabService = null;
        }
    };

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
