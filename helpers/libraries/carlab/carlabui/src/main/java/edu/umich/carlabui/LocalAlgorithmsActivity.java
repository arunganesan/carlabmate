package edu.umich.carlabui;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.lang.reflect.Method;

import edu.umich.carlab.loadable.App;

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


    protected void createModuleButtons (final App app, Method[] methods) {
        for (final Method method : methods) {
            Button button = new Button(this);
            button.setText(String.format("%s: %s".format(
                    app.getClass().getCanonicalName(),
                    method.getName())));

            button.setHeight(300);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StaticObjects.selectedApp = app;
                    StaticObjects.selectedMethod = method;
                    startActivity(new Intent(
                            LocalAlgorithmsActivity.this, AlgorithmSandboxActivity.class));
                }
            });
            moduleLayout.addView(button);
        }


    }
}
