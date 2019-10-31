package edu.umich.carlabui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.loadable.AlgorithmSpecs;

public class LocalAlgorithmsActivity extends AppCompatActivity {
    LinearLayout moduleLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_algorithms);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        moduleLayout = findViewById(R.id.localAlgorithmsListView);
    }


    protected void createModuleButtons(Algorithm... loadedAlgorithms) {
        for (final Algorithm algorithm : loadedAlgorithms) {
            for (final AlgorithmSpecs.AppFunction func : algorithm.algorithmFunctions) {
                LayoutInflater inflater = getLayoutInflater();
                Button button = (Button)inflater.inflate(R.layout.sandbox_algorithm, moduleLayout, false);
                button.setText(String.format("%s\nInput: %s\nOutput: %s",
                        algorithm.getName(),
                        func.outputInformation,
                        String.join(", ", func.inputInformation)));


                button.setHeight(300);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StaticObjects.selectedAlgorithm = algorithm;
                        StaticObjects.selectedAppFunction = func;
                        startActivity(new Intent(
                                LocalAlgorithmsActivity.this,
                                AlgorithmSandboxActivity.class));
                    }
                });
                moduleLayout.addView(button);
            }
        }
    }
}