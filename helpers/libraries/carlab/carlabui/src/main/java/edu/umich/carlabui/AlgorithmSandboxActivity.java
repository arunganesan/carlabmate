package edu.umich.carlabui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.AlgorithmSpecs;

public class AlgorithmSandboxActivity extends AppCompatActivity {
    final int FAKE = 0;
    final int FIXED = 1;
    final int SENSOR = 3;
    final String TAG = "AlgorithmSandboxActivity";
    final int TRACE = 2;
    boolean currentlyRunning = false;
    Map<String, List<RangeInfo>> fakeValuesRange = new HashMap<>();
    TextView.OnEditorActionListener doneChangeRange = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction (TextView target, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                event != null && event.getAction() == KeyEvent.ACTION_DOWN &&
                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (event == null || !event.isShiftPressed()) {
                    FakeRangeSpecific fakeRangeDetails = (FakeRangeSpecific) target.getTag();
                    String s = target.getText().toString();

                    if (fakeRangeDetails.minOrMax == RangeEndpoint.MIN)
                        fakeValuesRange.get(fakeRangeDetails.information)
                                       .get(fakeRangeDetails.index).first = Float.parseFloat(s);
                    else fakeValuesRange.get(fakeRangeDetails.information)
                                        .get(fakeRangeDetails.index).second = Float.parseFloat(s);

                    return true; // consume.
                }
            }
            return false;
        }
    };
    View.OnClickListener fakeModeDialog = new View.OnClickListener() {
        @Override
        public void onClick (final View v) {
            String information = (String) v.getTag();
            final float[] obj = (float[]) AlgorithmSpecs.InformationDatatypes.get(information);

            LinearLayout ll = new LinearLayout(AlgorithmSandboxActivity.this);
            ll.setOrientation(LinearLayout.VERTICAL);

            LinearLayout _ll;
            TextView _fieldIdTv;
            EditText _minVal, _maxVal;

            for (int i = 0; i < obj.length; i++) {
                _ll = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.fake_input_ranges, ll, false);
                _fieldIdTv = _ll.findViewById(R.id.fieldId);
                _fieldIdTv.setText("" + i);

                _minVal = _ll.findViewById(R.id.minFakeValue);
                _minVal.setText("" + fakeValuesRange.get(information).get(i).first);
                _minVal.setTag(new FakeRangeSpecific(information, i, RangeEndpoint.MIN));
                _minVal.setOnEditorActionListener(doneChangeRange);


                _maxVal = _ll.findViewById(R.id.maxFakeValue);
                _maxVal.setText("" + fakeValuesRange.get(information).get(i).second);
                _maxVal.setTag(new FakeRangeSpecific(information, i, RangeEndpoint.MAX));
                _maxVal.setOnEditorActionListener(doneChangeRange);

                ll.addView(_ll);
            }

            AlertDialog.Builder dialogBuilder =
                    new AlertDialog.Builder(AlgorithmSandboxActivity.this);
            dialogBuilder.setTitle("Set distribution").setView(ll)
                         .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                             public void onClick (DialogInterface dialog, int id) {
                                 Log.v(TAG, String.format(
                                         "Set info for information: %s. Num elements %d",
                                         v.getTag(), obj.length));
                             }
                         });

            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }
    };
    Map<String, Serializable[]> fixedValues = new HashMap<>();
    TextView.OnEditorActionListener doneChangeFixedValue = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction (TextView target, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                event != null && event.getAction() == KeyEvent.ACTION_DOWN &&
                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (event == null || !event.isShiftPressed()) {
                    FakeRangeSpecific fakeRangeDetails = (FakeRangeSpecific) target.getTag();
                    String s = target.getText().toString();

                    fixedValues.get(fakeRangeDetails.information)[fakeRangeDetails.index] =
                            Float.parseFloat(s);

                    return true;
                }
            }
            return false;
        }
    };
    View.OnClickListener fixedModeDialog = new View.OnClickListener() {
        @Override
        public void onClick (final View v) {
            String information = (String) v.getTag();
            final float[] obj = (float[]) AlgorithmSpecs.InformationDatatypes.get(information);
            LinearLayout ll = new LinearLayout(AlgorithmSandboxActivity.this);
            ll.setOrientation(LinearLayout.VERTICAL);

            LinearLayout _ll;
            TextView _fieldIdTv;
            EditText _minVal, _maxVal;

            for (int i = 0; i < obj.length; i++) {
                _ll = (LinearLayout) getLayoutInflater().inflate(R.layout.fixed_input, ll, false);
                _fieldIdTv = _ll.findViewById(R.id.fieldId);
                _fieldIdTv.setText("" + i);

                _minVal = _ll.findViewById(R.id.fixedValue);
                _minVal.setText("" + fixedValues.get(information)[i]);
                _minVal.setTag(new FakeRangeSpecific(information, i, RangeEndpoint.MIN));
                _minVal.setOnEditorActionListener(doneChangeFixedValue);

                ll.addView(_ll);
            }

            AlertDialog.Builder dialogBuilder =
                    new AlertDialog.Builder(AlgorithmSandboxActivity.this);
            dialogBuilder.setTitle("Set fixed value").setView(ll)
                         .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                             public void onClick (DialogInterface dialog, int id) {
                                 Log.v(TAG, String.format(
                                         "Set info for information: %s. Num elements %d",
                                         v.getTag(), obj.length));
                             }
                         });

            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }
    };
    LinearLayout inputCardList, outputCardList;
    Map<String, TextView> outputValueMap = new HashMap<>();
    long runPeriod = 100;
    Handler scheduledHandler;
    Shadow shadow = null;
    Runnable callAlgorithm = new Runnable() {
        @Override
        public void run () {
            int numArgs = StaticObjects.selectedAppFunction.inputInformation.size();

            for (int i = 0; i < numArgs; i++) {
                String inputInfo = StaticObjects.selectedAppFunction.inputInformation.get(i);
                Serializable[] values = fixedValues.get(inputInfo);
                DataMarshal.DataObject inputData = new DataMarshal.DataObject(inputInfo, values);
                StaticObjects.selectedAlgorithm.newData(inputData);
                if (shadow != null) shadow.addData(inputData);
            }

            String outputInfoName = StaticObjects.selectedAppFunction.outputInformation;
            Map<String, DataMarshal.DataObject> receivedData =
                    StaticObjects.dataReceiver.latestData;
            if (receivedData.containsKey(outputInfoName)) {
                DataMarshal.DataObject outputData = receivedData.get(outputInfoName);
                if (shadow != null) shadow.addData(outputData);
                Float[] outputVal = (Float[]) outputData.value;
                String[] outputValStrings = new String[outputVal.length];
                for (int i = 0; i < outputVal.length; i++)
                    outputValStrings[i] = outputVal[i].toString();

                outputValueMap.get(outputInfoName).setText(String.join(", ", outputValStrings));
            }

            if (currentlyRunning) scheduledHandler.postDelayed(callAlgorithm, runPeriod);
        }
    };
    FrameLayout shadowContentFrameLayout;
    Button startToggleButton;
    View.OnClickListener toggleDataFlow = new View.OnClickListener() {
        @Override
        public void onClick (View v) {
            currentlyRunning = !currentlyRunning;

            if (currentlyRunning) {
                startToggleButton.setText("Stop test");
                shadow.initializeVisualization(AlgorithmSandboxActivity.this,
                                               shadowContentFrameLayout);
                scheduledHandler.postDelayed(callAlgorithm, runPeriod);
            } else {
                startToggleButton.setText("Start test");
                scheduledHandler.removeCallbacks(callAlgorithm);
            }
        }
    };

    void createUI () {
        inputCardList = findViewById(R.id.algorithmInputList);
        outputCardList = findViewById(R.id.algorithmOutputList);

        LayoutInflater inflater = getLayoutInflater();

        for (String inputInfo : StaticObjects.selectedAppFunction.inputInformation) {
            final LinearLayout inputLinear = (LinearLayout) inflater
                    .inflate(R.layout.sandbox_input_configuration, inputCardList, false);

            inputLinear.setTag(inputInfo);
            TextView tv = inputLinear.findViewById(R.id.inputName);
            tv.setText(inputInfo);
            inputCardList.addView(inputLinear);

            // Initialize the dropdown-specific controls
            Spinner choiceSpinner = inputLinear.findViewById(R.id.inputChoice);
            choiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected (AdapterView<?> parent, View view, int position,
                                            long id) {
                    initializeComponent(inputLinear, position);
                }

                @Override
                public void onNothingSelected (AdapterView<?> parent) {

                }
            });

            // call the initialization one time
            initializeComponent(inputLinear, choiceSpinner.getSelectedItemPosition());
            shadow = new LineShadow();
        }

        LinearLayout outputLinear =
                (LinearLayout) inflater.inflate(R.layout.sandbox_output_row, outputCardList, false);
        TextView outputNameTv = outputLinear.findViewById(R.id.outputName);
        TextView outputValueTv = outputLinear.findViewById(R.id.outputValue);
        shadowContentFrameLayout = outputLinear.findViewById(R.id.shadowContentWrapper);

        String outputInfo = StaticObjects.selectedAppFunction.outputInformation;
        outputNameTv.setText(outputInfo);
        outputValueMap.put(outputInfo, outputValueTv);
        outputCardList.addView(outputLinear);

        startToggleButton = findViewById(R.id.toggleTest);
        startToggleButton.setOnClickListener(toggleDataFlow);
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
                dialogCallback = fixedModeDialog;
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

    void initializeValueRanges () {
        for (String information : StaticObjects.selectedAppFunction.inputInformation) {
            final float[] obj = (float[]) AlgorithmSpecs.InformationDatatypes.get(information);
            fakeValuesRange.put(information, new ArrayList<RangeInfo>());
            fixedValues.put(information, new Float[obj.length]);
            for (int i = 0; i < obj.length; i++) {
                fakeValuesRange.get(information).add(new RangeInfo(0, 1));
                fixedValues.get(information)[i] = 1.0F;
            }
        }
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sandbox);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        TextView text = findViewById(R.id.sandboxAppTitle);
        text.setText(StaticObjects.selectedAlgorithm.getName());


        findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                finish();
            }
        });

        scheduledHandler = new Handler();
        initializeValueRanges();
        createUI();
    }

    enum RangeEndpoint {
        MIN, MAX
    }

    public class FakeRangeSpecific {
        public int index;
        public String information;
        public RangeEndpoint minOrMax;

        public FakeRangeSpecific (String information, int index, RangeEndpoint minOrMax) {
            this.information = information;
            this.index = index;
            this.minOrMax = minOrMax;
        }
    }

    public class RangeInfo {
        public float first;
        public float second;

        public RangeInfo (float first, float second) {
            this.first = first;
            this.second = second;
        }


        public String toString () {
            return String.format("(%f, %f)", first, second);
        }
    }

}
