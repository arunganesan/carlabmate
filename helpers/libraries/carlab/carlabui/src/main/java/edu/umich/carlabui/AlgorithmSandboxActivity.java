package edu.umich.carlabui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import edu.umich.carlab.loadable.AlgorithmSpecs;

/*
    <string-array name="sandbox_input_array">
        <item>Fake</item>
        <item>Fixed</item>
        <item>Trace</item>
        <item>Sensor</item>
    </string-array>
 */

public class AlgorithmSandboxActivity extends AppCompatActivity {
    LinearLayout inputCardList, outputCardList;

    final int FAKE = 0;
    final int FIXED = 1;
    final int TRACE = 2;
    final int SENSOR = 3;

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
            final LinearLayout inputLinear = (LinearLayout)inflater.inflate(
                    R.layout.sandbox_input_configuration,
                    inputCardList,
                    false);

            TextView tv = inputLinear.findViewById(R.id.inputName);
            tv.setText(String.format("%s => %s",
                    inputInfo,
                    AlgorithmSpecs.InformationDatatypes.get(inputInfo).getClass().getSimpleName()));
            inputCardList.addView(inputLinear);

            // Initialize the dropdown-specific controls
            Spinner choiceSpinner = inputLinear.findViewById(R.id.inputChoice);
            choiceSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    initializeComponent(inputLinear, position);
                }
            });

            // call the initialization one time
            initializeComponent(inputLinear, choiceSpinner.getSelectedItemPosition());
        }

        TextView tv = new TextView(this);
        tv.setText(StaticObjects.selectedAppFunction.outputInformation);
        outputCardList.addView(tv);
    }


    void initializeComponent (LinearLayout inputLinear, int selectionId) {
        int resource = R.layout.sandbox_input_fake;
        switch (selectionId) {
            case FIXED:
                resource = R.layout.sandbox_input_fixed;
                break;
            case FAKE:
                resource = R.layout.sandbox_input_fake;
                break;
            case SENSOR:
                resource = R.layout.sandbox_input_sensor;
                break;
            case TRACE:
                resource = R.layout.sandbox_input_trace;
                break;
        }

        FrameLayout customization = inputLinear.findViewById(R.id.inputSpecificControl);
        LayoutInflater inflater = getLayoutInflater();
        customization.removeAllViews();
        customization.addView(inflater.inflate(
                resource,
                customization,
                false));
    }
}
