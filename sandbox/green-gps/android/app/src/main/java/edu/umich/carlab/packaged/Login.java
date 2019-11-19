package edu.umich.carlab.packaged;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import edu.umich.carlab.Constants;
import edu.umich.carlab.utils.Utilities;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static edu.umich.carlab.Constants.LOGINURL;

public class Login extends AppCompatActivity {
    Button loginButton;
    Response.ErrorListener loginRequestError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse (VolleyError error) {
            Toast.makeText(Login.this, "Login failed", Toast.LENGTH_SHORT).show();
        }
    };
    SharedPreferences prefs;
    Response.Listener<String> gotLoginSession = new Response.Listener<String>() {
        @Override
        public void onResponse (String responseLogin) {
            try {
                JSONObject loginInfo = new JSONObject(responseLogin);
                prefs.edit().putString(Constants.SESSION, loginInfo.getString("session")).apply();
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    EditText usernameText, passwordText;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginButton);
        usernameText = findViewById(R.id.username);
        passwordText = findViewById(R.id.password);
        prefs = getDefaultSharedPreferences(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                String username = usernameText.getText().toString();
                String password = passwordText.getText().toString();

                // Make server call
                if (!Utilities.isConnectedAndWifi(Login.this)) {
                    Toast.makeText(Login.this, "Please connect to Internet", Toast.LENGTH_SHORT)
                         .show();
                } else {
                    RequestQueue queue = Volley.newRequestQueue(Login.this);
                    StringRequest myReq = new StringRequest(Request.Method.GET,
                                                            String.format(LOGINURL, username,
                                                                          password),
                                                            gotLoginSession, loginRequestError);
                    queue.add(myReq);
                }
            }
        });
    }
}

