package edu.umich.carlabui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.umich.carlab.loadable.AlgorithmSpecs;

public class AlgorithmSandboxActivity extends AppCompatActivity {
    LinearLayout inputCardList, outputCardList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sandbox);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        TextView text = findViewById(R.id.sandboxAppTitle);
        text.setText(StaticObjects.selectedAlgorithm.getName());


        findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO need to shut down CarLab before we go back
                finish();
            }
        });


        /*
        AppLoader instance = AppLoader.getInstance();
        // TODO This should be loaded during run time
        instance.loadApps(new Class<?>[]{
                edu.umich.aligned_imu.AlignedIMU.class,
        });*/


        createUI();
    }


    void createUI() {
        inputCardList = findViewById(R.id.algorithmInputList);
        outputCardList = findViewById(R.id.algorithmOutputList);

        for (String inputInfo : StaticObjects.selectedAppFunction.inputInformation) {
            LayoutInflater inflater = getLayoutInflater();
            LinearLayout inputLinear = (LinearLayout)inflater.inflate(
                    R.layout.sandbox_input_configuration,
                    inputCardList,
                    false);

            TextView tv = inputLinear.findViewById(R.id.inputName);
            tv.setText(String.format("%s => %s",
                    inputInfo,
                    AlgorithmSpecs.InformationDatatypes.get(inputInfo).getClass().getSimpleName()));
            inputCardList.addView(inputLinear);
        }


        TextView tv = new TextView(this);
        tv.setText(StaticObjects.selectedAppFunction.outputInformation);
        outputCardList.addView(tv);
    }
}
