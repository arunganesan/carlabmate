package edu.umich.carlabui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import edu.umich.carlab.loadable.AlgorithmSpecs;

import static edu.umich.carlab.Constants.Load_From_Trace_Key;

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
    final String TAG = "AlgorithmSandboxActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sandbox);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        TextView text = findViewById(R.id.sandboxAppTitle);
        text.setText(StaticObjects.selectedAlgorithm.getName());


        findViewById(R.id.closeButton).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            }
        );

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

            inputLinear.setTag(inputInfo);
            TextView tv = inputLinear.findViewById(R.id.inputName);
            tv.setText(inputInfo);
            inputCardList.addView(inputLinear);

            // Initialize the dropdown-specific controls
            Spinner choiceSpinner = inputLinear.findViewById(R.id.inputChoice);
            choiceSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view, int position,
                            long id) {
                        initializeComponent(inputLinear, position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
            );

            // call the initialization one time
            initializeComponent(inputLinear, choiceSpinner.getSelectedItemPosition());
        }

        TextView tv = new TextView(this);
        tv.setText(StaticObjects.selectedAppFunction.outputInformation);
        outputCardList.addView(tv);
    }


    void initializeComponent (LinearLayout inputLinear, int selectionId) {
        String buttonText = "";
        View.OnClickListener dialogCallback = null;
        switch (selectionId) {
            case FAKE:
                buttonText = "Adjust distribution";
                dialogCallback = fakeModeDialog;
                break;
            case FIXED:
                buttonText = "Set fixed value";
                dialogCallback = fakeModeDialog;
                break;
            case SENSOR:
                buttonText = "Choose sensor";
                dialogCallback = fakeModeDialog;
                break;
            case TRACE:
                buttonText = "Choose trace";
                dialogCallback = fakeModeDialog;
                break;
        }

        Button button = inputLinear.findViewById(R.id.inputButtonConfiguration);
        button.setText(buttonText);
        button.setTag(inputLinear.getTag());
        button.setOnClickListener(dialogCallback);
    }

    View.OnClickListener fakeModeDialog = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            LinearLayout fakeInputDialog = (LinearLayout)getLayoutInflater().inflate(
                    R.layout.fake_input_dialog,
                    null);
            EditText minValue = fakeInputDialog.findViewById(R.id.minFakeValue);
            EditText maxValue = fakeInputDialog.findViewById(R.id.maxFakeValue);
            minValue.setText("123");

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
                    AlgorithmSandboxActivity.this);
            dialogBuilder
                    .setTitle("Set distribution")
                    .setView(fakeInputDialog)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.v(TAG, "Set info for information: " + v.getTag());
                        }
                    });

            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }
    };

}
