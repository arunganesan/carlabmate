package edu.umich.carlabui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.Registry;

public class AlgorithmSandboxActivity extends AppCompatActivity {
    final int SEL_FAKE = 2;
    final int SEL_FIXED = 1;
    final int SEL_SENSOR = 0;
    final int SEL_TRACE = 3;
    final String TAG = "AlgorithmSandboxActivity";
    boolean currentlyRunning = false;
    boolean dataDumpEnalbed = false;
    File dataDumpReplayFile = null;
    int dataDumpReplayerIndex = 0;
    List<DataMarshal.DataObject> dataDumpReplayerObjects;
    DataDumpWriter dataDumpWriter;
    View.OnClickListener dialogTraceMode = new View.OnClickListener() {
        @Override
        public void onClick (final View v) {
            final Registry.Information information = (Registry.Information) v.getTag();
            File dumpsDir = DataDumpWriter.GetDumpsDir(AlgorithmSandboxActivity.this);
            Comparator<File> sortByLastModified = new Comparator<File>() {
                @Override
                public int compare (File f1, File f2) {
                    if (f1.lastModified() == f2.lastModified()) return 0;
                    if (f1.lastModified() < f2.lastModified()) return -1;
                    return 1;
                }
            };

            List<File> allFiles = Arrays.asList(dumpsDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept (File dir, String name) {
                    return name.contains(information.name);
                }
            }));

            Collections.sort(allFiles, sortByLastModified);
            final File[] dumpFiles = allFiles.toArray(new File[]{});

            List<CharSequence> filenames = new ArrayList<>();
            for (File ifile : dumpFiles) {
                filenames.add(ifile.getName());
            }

            AlertDialog.Builder dialogBuilder =
                    new AlertDialog.Builder(AlgorithmSandboxActivity.this);
            dialogBuilder.setTitle("Select trace file")
                         .setItems(filenames.toArray(new CharSequence[]{}),
                                   new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick (DialogInterface dialogInterface,
                                                            int i) {
                                           loadDumpFile(dumpFiles[i]);
                                       }
                                   });
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }
    };
    Map<Registry.Information, List<RangeInfo>> fakeValuesRange = new HashMap<>();
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
    View.OnClickListener dialogFakeMode = new View.OnClickListener() {
        @Override
        public void onClick (final View v) {
            Registry.Information information = (Registry.Information) v.getTag();

            // Initialize this constructor.
            // But more generally we literally don't care about this Wtf is this anyway.
            final float[] obj = (information.dataType.getClass().con) information.dataType;

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
    Map<Registry.Information, Serializable[]> fixedValues = new HashMap<>();

    // Show the dialog box to select trace file

    // File dumpsDir = DataDumpWriter.GetDumpsDir(ExperimentBaseActivity.this);
    // Comparator<File> sortByLastModified = new Comparator<File>() {
    //     @Override
    //     public int compare(File f1, File f2) {
    //         if (f1.lastModified() == f2.lastModified()) return 0;
    //         if (f1.lastModified() < f2.lastModified()) return -1;
    //         return 1;
    //     }
    // };
    //
    // List<File> allFiles = Arrays.asList(dumpsDir.listFiles());
    //         Collections.sort(allFiles, sortByLastModified);
    // final File[] dumpFiles = allFiles.toArray(new File[]{});
    //
    // List<CharSequence> filenames = getFilenames(dumpFiles);
    //         filenames.add("None");
    //
    // // If they choose any of the dump files, set that value.
    // // Otherwise set it to null.
    // AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ExperimentBaseActivity.this);
    //         dialogBuilder
    //                 .setTitle("Select Trace File")
    //                 .setItems(
    //         filenames.toArray(new CharSequence[]{}),
    //         new DialogInterface.OnClickListener() {
    //     @Override
    //     public void onClick(DialogInterface dialogInterface, int i) {
    //         if (i == dumpFiles.length) {
    //             // This is the undefined one
    //             prefs.edit().putString(Load_From_Trace_Key, null).commit();
    //         } else {
    //             Toast.makeText(
    //                     getApplicationContext(),
    //                     String.format("Showing this file: %s", dumpFiles[i]),
    //                     Toast.LENGTH_SHORT).show();
    //             prefs.edit().putString(Load_From_Trace_Key, dumpFiles[i].toString()).commit();
    //         }
    //
    //         updateButtons();
    //     }
    // });
    // AlertDialog dialog = dialogBuilder.create();
    //         dialog.show();
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
    View.OnClickListener dialogFixedMode = new View.OnClickListener() {
        @Override
        public void onClick (final View v) {
            Registry.Information information = (Registry.Information) v.getTag();
            final float[] obj = (float[]) information.dataType;
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
    Map<Registry.Information, DataFeedMode> inputDataModes = new HashMap<>();
    Shadow inputShadow, outputShadow;
    FrameLayout inputShadowContentFrameLayout, outputShadowContentFrameLayout;
    Map<Registry.Information, TextView> outputValueMap = new HashMap<>();
    long runPeriod = 100;
    ToggleButton saveToggleButton;
    Handler scheduledHandler;
    Runnable callAlgorithm = new Runnable() {
        @Override
        public void run () {
            int numArgs = StaticObjects.selectedAppFunction.inputInformation.size();

            for (int i = 0; i < numArgs; i++) {
                Registry.Information inputInfo =
                        StaticObjects.selectedAppFunction.inputInformation.get(i);
                DataMarshal.DataObject inputData = null;
                if (inputDataModes.get(inputInfo) == DataFeedMode.FAKE ||
                    inputDataModes.get(inputInfo) == DataFeedMode.FIXED) {
                    Serializable[] values = fixedValues.get(inputInfo).clone();
                    inputData = new DataMarshal.DataObject(inputInfo.name, values);
                } else if (inputDataModes.get(inputInfo) == DataFeedMode.TRACE) {
                    if (dataDumpReplayFile != null) {
                        inputData = dataDumpReplayerObjects.get(dataDumpReplayerIndex);
                        dataDumpReplayerIndex =
                                (dataDumpReplayerIndex + 1) % dataDumpReplayerObjects.size();
                    }
                }

                if (inputData != null) {
                    if (inputShadow != null) inputShadow.addData(inputData);
                    StaticObjects.selectedAlgorithm.newData(inputData);
                }
            }

            Registry.Information outputInfoName =
                    StaticObjects.selectedAppFunction.outputInformation;
            Map<String, DataMarshal.DataObject> receivedData =
                    StaticObjects.dataReceiver.latestData;
            if (receivedData.containsKey(outputInfoName)) {
                DataMarshal.DataObject outputData = receivedData.get(outputInfoName);

                if (outputShadow != null) outputShadow.addData(outputData);
                if (dataDumpEnalbed) dataDumpWriter.addData(outputData);

                Float[] outputVal = (Float[]) outputData.value;
                String[] outputValStrings = new String[outputVal.length];
                for (int i = 0; i < outputVal.length; i++)
                    outputValStrings[i] = outputVal[i].toString();

                outputValueMap.get(outputInfoName).setText(String.join(", ", outputValStrings));
            }

            if (currentlyRunning) scheduledHandler.postDelayed(callAlgorithm, runPeriod);
        }
    };
    Map<Integer, String> sensorTypeToInformation = new HashMap<>();
    SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged (Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged (SensorEvent event) {
            Float[] values = new Float[event.values.length];
            for (int i = 0; i < event.values.length; i++) {
                values[i] = event.values[i];
            }
            DataMarshal.DataObject d =
                    new DataMarshal.DataObject(sensorTypeToInformation.get(event.sensor.getType()),
                                               values);

            StaticObjects.selectedAlgorithm.newData(d);
            inputShadow.addData(d);
        }
    };
    Button startToggleButton;
    View.OnClickListener toggleDataFlow = new View.OnClickListener() {
        @Override
        public void onClick (View v) {
            currentlyRunning = !currentlyRunning;

            if (currentlyRunning) {
                startToggleButton.setText("Stop test");
                inputShadow.initializeVisualization(AlgorithmSandboxActivity.this,
                                                    inputShadowContentFrameLayout);
                outputShadow.initializeVisualization(AlgorithmSandboxActivity.this,
                                                     outputShadowContentFrameLayout);
                scheduledHandler.postDelayed(callAlgorithm, runPeriod);

                for (Map.Entry<Registry.Information, DataFeedMode> entry : inputDataModes.entrySet()) {
                    if (entry.getValue() == DataFeedMode.SENSOR) {
                        int sensorType = entry.getKey().lowLevelSensor;
                        if (sensorType != -1) {
                            SensorManager sensorManager = (SensorManager) getApplicationContext()
                                    .getSystemService(Context.SENSOR_SERVICE);
                            sensorManager.registerListener(sensorListener, sensorManager
                                    .getDefaultSensor(sensorType), 1000, 1000);
                        }
                    }
                }
            } else {
                startToggleButton.setText("Start test");
                inputShadow.destroyVisualization();
                scheduledHandler.removeCallbacks(callAlgorithm);

                for (Map.Entry<Registry.Information, DataFeedMode> entry : inputDataModes.entrySet()) {
                    if (entry.getValue() == DataFeedMode.SENSOR) {
                        int sensorType = entry.getKey().lowLevelSensor;
                        if (sensorType != -1) {
                            SensorManager sensorManager = (SensorManager) getApplicationContext()
                                    .getSystemService(Context.SENSOR_SERVICE);
                            sensorManager.unregisterListener(sensorListener);
                        }
                    }
                }
            }
        }
    };
    CompoundButton.OnCheckedChangeListener toggleSavingData =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        Registry.Information outputInfo = StaticObjects.selectedAppFunction.outputInformation;
                        dataDumpWriter.startNewFile(outputInfo.name);
                        dataDumpEnalbed = true;
                    } else {
                        dataDumpEnalbed = false;
                        String filename = dataDumpWriter.saveFile();
                        Toast.makeText(AlgorithmSandboxActivity.this, filename, Toast.LENGTH_SHORT)
                             .show();
                    }
                }
            };

    void createUI () {
        inputCardList = findViewById(R.id.algorithmInputList);
        outputCardList = findViewById(R.id.algorithmOutputList);

        LayoutInflater inflater = getLayoutInflater();

        for (Registry.Information inputInfo : StaticObjects.selectedAppFunction.inputInformation) {
            final LinearLayout inputLinear = (LinearLayout) inflater
                    .inflate(R.layout.sandbox_input_configuration, inputCardList, false);

            inputLinear.setTag(inputInfo);
            TextView tv = inputLinear.findViewById(R.id.inputName);
            tv.setText(inputInfo.name);
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
            inputShadow = new LineShadow();
            outputShadow = new LineShadow();
        }

        LinearLayout outputLinear =
                (LinearLayout) inflater.inflate(R.layout.sandbox_output_row, outputCardList, false);
        TextView outputNameTv = outputLinear.findViewById(R.id.outputName);
        TextView outputValueTv = outputLinear.findViewById(R.id.outputValue);
        inputShadowContentFrameLayout = findViewById(R.id.inputShadowWrapper);
        outputShadowContentFrameLayout = findViewById(R.id.outputShadowWrapper);

        Registry.Information outputInfo = StaticObjects.selectedAppFunction.outputInformation;
        outputNameTv.setText(outputInfo.name);
        outputValueMap.put(outputInfo, outputValueTv);
        outputCardList.addView(outputLinear);

        startToggleButton = findViewById(R.id.toggleTest);
        startToggleButton.setOnClickListener(toggleDataFlow);

        saveToggleButton = findViewById(R.id.saveToggleButton);
        saveToggleButton.setOnCheckedChangeListener(toggleSavingData);
        dataDumpWriter = new DataDumpWriter(this);
    }

    void initializeComponent (LinearLayout inputLinear, int selectionId) {
        String buttonText = "";
        DataFeedMode dataFeedMode = DataFeedMode.FAKE;
        View.OnClickListener dialogCallback = null;
        Registry.Information info = (Registry.Information) inputLinear.getTag();

        switch (selectionId) {
            case SEL_FAKE:
                buttonText = "Adjust distribution";
                dialogCallback = dialogFakeMode;
                dataFeedMode = DataFeedMode.FAKE;
                break;
            case SEL_FIXED:
                buttonText = "Set fixed value";
                dialogCallback = dialogFixedMode;
                dataFeedMode = DataFeedMode.FIXED;
                break;
            case SEL_SENSOR:
                buttonText = "Choose sensor";
                dialogCallback = null;
                dataFeedMode = DataFeedMode.SENSOR;
                break;
            case SEL_TRACE:
                buttonText = "Choose trace";
                dialogCallback = dialogTraceMode;
                dataFeedMode = DataFeedMode.TRACE;
                break;
        }

        Button button = inputLinear.findViewById(R.id.inputButtonConfiguration);

        if (selectionId == SEL_SENSOR) {
            int lowLevelSensor = info.lowLevelSensor;
            button.setEnabled(false);
            if (lowLevelSensor == -1) buttonText = "No usable sensor";
            else {
                buttonText = info.lowLevelSensorName;
                sensorTypeToInformation.put(lowLevelSensor, info.name);
            }
        } else {
            button.setEnabled(true);
        }

        inputDataModes.put(info, dataFeedMode);
        button.setText(buttonText);
        button.setTag(info);

        if (dialogCallback != null) button.setOnClickListener(dialogCallback);
    }

    void initializeValueRanges () {
        for (Registry.Information information : StaticObjects.selectedAppFunction.inputInformation) {
            final float[] obj = (float[]) information.dataType;
            fakeValuesRange.put(information, new ArrayList<RangeInfo>());
            fixedValues.put(information, new Float[obj.length]);
            for (int i = 0; i < obj.length; i++) {
                fakeValuesRange.get(information).add(new RangeInfo(0, 1));
                fixedValues.get(information)[i] = 1.0F;
            }
        }
    }

    void loadDumpFile (File file) {
        dataDumpReplayFile = file;
        dataDumpReplayerObjects = DataDumpWriter.ReadData(file);
        dataDumpReplayerIndex = 0;
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

    enum DataFeedMode {
        FAKE, FIXED, SENSOR, TRACE
    }

    enum RangeEndpoint {
        MIN, MAX
    }

    public class FakeRangeSpecific {
        public int index;
        public Registry.Information information;
        public RangeEndpoint minOrMax;

        public FakeRangeSpecific (Registry.Information information, int index,
                                  RangeEndpoint minOrMax) {
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
