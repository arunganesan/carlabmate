package edu.umich.carlabui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.Registry;

public class LocalAlgorithmsActivity extends AppCompatActivity {
    protected DataReceiver dataReceiver = new DataReceiver();
    LinearLayout moduleLayout;

    // TODO this needs to change to "addAppFunction" call instead. That will load JUST the hardcoded list of functions.
    // protected void createModuleButtons (Algorithm... loadedAlgorithms) {
    //     for (final Algorithm algorithm : loadedAlgorithms) {
    //         for (final Registry.AppFunction func : algorithm.algorithmFunctions) {
    //             LayoutInflater inflater = getLayoutInflater();
    //             Button button =
    //                     (Button) inflater.inflate(R.layout.sandbox_algorithm, moduleLayout, false);
    //
    //
    //             List<String> inputInfoNames = new ArrayList<>();
    //             for (Registry.Information info : func.inputInformation) {
    //                 inputInfoNames.add(info.name);
    //             }
    //
    //
    //             button.setText(String.format("%s\nInput: %s\nOutput: %s", algorithm.getName(),
    //                                          func.outputInformation.name,
    //                                          String.join(", ", inputInfoNames)));
    //
    //             button.setOnClickListener(new View.OnClickListener() {
    //                 @Override
    //                 public void onClick (View v) {
    //                     StaticObjects.selectedAlgorithm = algorithm;
    //                     StaticObjects.selectedAppFunction = func;
    //                     startActivity(new Intent(LocalAlgorithmsActivity.this,
    //                                              AlgorithmSandboxActivity.class));
    //                 }
    //             });
    //             moduleLayout.addView(button);
    //         }
    //     }
    // }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_algorithms);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        StaticObjects.dataReceiver = dataReceiver;
        moduleLayout = findViewById(R.id.localAlgorithmsListView);
    }

    public class DataReceiver implements CLDataProvider {
        Map<String, DataMarshal.DataObject> latestData = new HashMap<>();

        @Override
        public void newData (DataMarshal.DataObject dataObject) {
            latestData.put(dataObject.information, dataObject);
        }
    }
}
