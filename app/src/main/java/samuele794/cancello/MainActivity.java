package samuele794.cancello;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.net.ssl.HttpsURLConnection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.jar.Manifest;

import javax.net.ssl.HttpsURLConnection;

import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_READ_PHONE_STATE = 0;
    private URL paginaURL;
    private InputStream risposta;
    private TextView textView;
    private String text;
    private WifiManager wifiMan;
    //private static String a =android.telephony.TelephonyManager.getDeviceId();

    private static final String DEBUG_TAG = "NetworkStatusExample";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bottone = (Button) findViewById(R.id.bottoneCancello);
        bottone.setOnClickListener(this);
        textView = (TextView) findViewById(R.id.DebugText);
        if (savedInstanceState != null) {
            textView.setText(savedInstanceState.getString("textView"));
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
        }




    }

    @Override

    public void onClick(View v) {

        /**
         * GESTISCE ACCENSIONE WIFI IN CODICE
         */

        /*wifiMan = (WifiManager) getApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMan.isWifiEnabled() == false) {
            //Toast.makeText(getApplicationContext(), "wifi no good", Toast.LENGTH_SHORT).show();
            System.out.print(wifiMan.getWifiState());

            //wifiMan.setWifiEnabled(true);
        }*/



        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();




        if(isWifiConn ==true || isMobileConn == true){
            if(isOnline()){
                new Gt().execute();
            }else{
                Toast.makeText(getApplicationContext(), "Eja ci sono problemi di rete", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Eja ci sono problemi di rete", Toast.LENGTH_SHORT).show();
        }



    }


    public class Gt extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {

            DataOutputStream stream = null;
            HttpURLConnection connection = null;
            StringBuilder urlparam = new StringBuilder();
            String imei = new String();
            /**
             * DA AGGIUNGERE QUESTA FUNZIONALITÀ:
             *
             * _registrazione cancello tramite app.
             *  usare shared preference per limitare la registrazione a solo una volta tramite db
             *
             */


                try {
                    paginaURL = new URL("http://www.gate794.heliohost.org/access.php"); //URL

                    connection = (HttpURLConnection) paginaURL.openConnection(); //ISTAURAZIONE CONNESSIONE
                    urlparam.append("stato=1&IMEI="); //DATI PER POST

                    TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                    String imei_start =telephonyManager.getDeviceId();
                    if(imei_start.length() == 15){
                        imei = "0" + imei_start;
                    }else{
                        imei = imei_start;
                    }
                    urlparam.append(imei);
                    //CONFIGURAZIONE STREAM POST
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                    connection.setRequestProperty("ACCEPT-LANUGAGE", "en-US,en;0.5");
                    connection.setDoOutput(true);
                    stream = new DataOutputStream(connection.getOutputStream()); //CREAZIONE STREAM
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();
                }


            try {
                stream.writeBytes(String.valueOf(urlparam)); //INVIO DATI IN POST
                stream.flush();
                stream.close(); //CHIUSURA STREAM

                //int response = connection.getResponseCode();
                final StringBuilder out = new StringBuilder(); //"Request URL" + paginaURL
                //out.append(System.getProperty("line.separator")+ "Request Paramenters " + urlparam);
                //out.append(System.getProperty("line.separator")  + "Response Code " + response);

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responeout = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    responeout.append(line);
                }
                br.close();
                //System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") +
                out.append(responeout.toString());

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(out);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("textView", textView.getText().toString());

        super.onSaveInstanceState(outState);
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true; //mettiamo true per indicare ad android di fermare la ricerca nelle classi sottostanti alla ricerca dei menu
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch(id) {
            case R.id.Config: {
                Toast.makeText(getApplicationContext(), "funge", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }





}




