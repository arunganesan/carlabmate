package edu.umich.carlabui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.blox.graphview.BaseGraphAdapter;
import de.blox.graphview.Graph;
import de.blox.graphview.GraphView;
import de.blox.graphview.Node;
import de.blox.graphview.energy.FruchtermanReingoldAlgorithm;
import de.blox.graphview.tree.BuchheimWalkerAlgorithm;
import de.blox.graphview.tree.BuchheimWalkerConfiguration;
import edu.umich.carlab.io.AppLoader;
import edu.umich.carlab.loadable.App;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DepMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DepMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DepMapFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private int nodeCount = 1;


    private OnFragmentInteractionListener mListener;

    public DepMapFragment() {
        // Required empty public constructor
    }

    private class ViewHolder {
        TextView mTextView;
        ViewHolder(View view) {
            mTextView = view.findViewById(R.id.textView);
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DepMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DepMapFragment newInstance(String param1, String param2) {
        DepMapFragment fragment = new DepMapFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(
                R.layout.fragment_dep_map,
                container,
                false);


        GraphView graphView = v.findViewById(R.id.graph);

        // example tree
        final Graph graph = new Graph();

        List<App> listOfApps = AppLoader.getInstance().instantiateApps(null, null);
        listOfApps.get(0).getClass().getCanonicalName();
        Map<String, Node> nodeMiddlewareMap = new HashMap<>();
        for (App app : listOfApps)
            nodeMiddlewareMap.put(
                    app.getMiddlewareName(),
                    new Node(app.getMiddlewareName()));


        Node src, dst;
        for (App app : listOfApps) {
            src = nodeMiddlewareMap.get(app.getMiddlewareName());
            for (Pair<String, String> sensor : app.sensors) {
                String dev = sensor.first;
                nodeMiddlewareMap.putIfAbsent(dev, new Node(dev));
                dst = nodeMiddlewareMap.get(dev);
                graph.addEdge(dst, src);
            }
        }


        // you can set the graph via the constructor or use the adapter.setGraph(Graph) method
        final BaseGraphAdapter<ViewHolder> adapter = new BaseGraphAdapter<ViewHolder>(getContext(), R.layout.node, graph) {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(View view) {
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(ViewHolder viewHolder, Object data, int position) {
                viewHolder.mTextView.setText(data.toString());
            }
        };
        graphView.setAdapter(adapter);
        graphView.setUseMaxSize(true);
        adapter.setAlgorithm(new FruchtermanReingoldAlgorithm());
        return v;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

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
