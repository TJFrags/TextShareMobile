package com.example.textsharemobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    // declaring required variables
    private Socket client;
    private PrintWriter printwriter;
    private BufferedReader bufferedReader;
    private EditText messageField;
    private Button sendButton, dispButton;
    public String message, srcLang, destLang, ip, port, translate, berry;


    // handler to update UI from background thread
    private Handler handler = new Handler();

    public static final  String pref_srcLang = "srclang";
    public static final  String pref_translate = "translate";
    public static final  String pref_destLang = "destlang";
    public static final  String pref_ip = "ip";
    public static final  String pref_port = "port";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.add_note) {

            // Going from MainActivity to NotesEditorActivity
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //getSupportActionBar().setCustomView(R.layout.toolbar_main);

        // reference to UI elements
        messageField = findViewById(R.id.edit_text);
        //ipField = findViewById(R.id.edit_ip);
        //portField = findViewById(R.id.edit_port);
        //responseField = findViewById(R.id.text_view);
        sendButton = findViewById(R.id.send_button);
        dispButton = findViewById(R.id.disp_button);
        //Translate = findViewById(R.id.Translate);
        //Lang = findViewById(R.id.language);
        message = "TextRequest:";
        dispButton.setEnabled(false);




        // Button press event listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read_prefs();
                // get input values from UI elements
                if (translate.equals("true")){
                    message += "tr:" + destLang;
                }

                //ip = "192.168.1.147";
                //int _port = 4444;

                // disable the send button

                // start the Thread to connect to server
                new Thread(new ClientThread(message, ip, Integer.parseInt(port))).start();
                message = "TextRequest:";

            }
        });

        dispButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Thread2()).start();

            }
        });


    }

    // the ClientThread class performs
    // the networking operations
    class ClientThread implements Runnable {
        private final String message;
        private final String ip;
        private final int port;

        ClientThread(String message, String ip, int port) {
            this.message = message;
            this.ip = ip;
            this.port = port;
        }
        @Override
        public void run() {
            try {
                // Creates a stream socket and connects it to the specified port number on the named host.
                client = new Socket(ip, port); // connect to server
                printwriter = new PrintWriter(client.getOutputStream(),true);
                printwriter.write(message); // write the message to output stream
                printwriter.flush();

                // read response from server
                bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                final String response = bufferedReader.readLine();
                Log.d("D", "RESP: " + response);

                //Scanner scanner = new Scanner(client.getInputStream());
                //String response = scanner.nextLine();



                // close connections
                printwriter.close();
                bufferedReader.close();
                client.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CharSequence text = "Recieved";
                        int duration = Toast.LENGTH_SHORT;
                        messageField.setText(response);
                        Toast toast = Toast.makeText(MainActivity.this /* MyActivity */, text, duration);
                        toast.show();
                        Log.d("D", "toast done");
                        //berry = response;

                    }
                });



            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class Thread2 implements Runnable{
        @Override
        public void run() {
            // update UI with response from server
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageField.setText(berry);

                }
            });
        }
    }
    public void read_prefs(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);


        srcLang = prefs.getString(pref_srcLang, "def");
        translate = String.valueOf(prefs.getBoolean(pref_translate, false));
        destLang = prefs.getString(pref_destLang, "def");
        ip = prefs.getString(pref_ip, "def");
        port = prefs.getString(pref_port, "def");
        //messageField.setText(port);

    }
}
