package edu.umich.carlabui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class AlgorithmSandboxActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sandbox);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        TextView text = findViewById(R.id.sandboxAppTitle);
        text.setText(String.format("%s: %s",
                StaticObjects.selectedApp.getClass().getSimpleName(),
                StaticObjects.selectedMethod.getName()));


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
    }
}
