package samuele794.cancello;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_READ_PHONE_STATE = 0;
    private URL paginaURL;
    private TextView textView;
    private WifiManager wifiMan;
    private Button bottoneApertura;
    private Button bottoneChiusura;

    //private static String a =android.telephony.TelephonyManager.getDeviceId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ACQISIZIONE DATI VIEW
        bottoneApertura = (Button) findViewById(R.id.bottoneCancelloApertura);
        bottoneChiusura = (Button) findViewById(R.id.bottoneCancelloChiusura);
        textView = (TextView) findViewById(R.id.DebugText);

        //IMPOSTAZIONE CLICK LISTENER
        bottoneApertura.setOnClickListener(this);
        bottoneChiusura.setOnClickListener(this);

        //SALVATAGGIO ISTANZA PER ROTAZIONE SCHERMO
        if (savedInstanceState != null) {
            textView.setText(savedInstanceState.getString("textView"));
        }

        //RICHIESTA PERMESSO LETTURA IMEI
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        }





    }

    /**
     * Metodo chiamato dal clickListener, gestisce la disattivazione dei bottoni, controllo di connessione
     * e i metodi per apertura e chiusura del cancello
     * @param v
     */
    @Override
    public void onClick(View v) {

        //DISATTIVAZIONE BOTTONI
        bottoneApertura.setActivated(false);
        bottoneChiusura.setActivated(false);


        //CONTROLLO CHE LA CONNESSIONE SIA ATTIVA E ONLINE
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();

        if(isWifiConn || isMobileConn){
            if(isOnline()){
                switch(v.getId()){
                    case R.id.bottoneCancelloApertura:{
                        //Toast.makeText(getApplicationContext(), "Bottone Apertura", Toast.LENGTH_SHORT).show();
                       new openGate().execute();    //APERTURA
                    }
                    break;
                    case R.id.bottoneCancelloChiusura:{
                       // Toast.makeText(getApplicationContext(), "Bottone Chiusura", Toast.LENGTH_SHORT).show();
                        new closeGate().execute();  //CHIUSURA
                    }

                }
            }else{
                Toast.makeText(getApplicationContext(), "Eja ci sono problemi di rete", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "Eja ci sono problemi di rete", Toast.LENGTH_SHORT).show();
        }




        /*
         * GESTISCE ACCENSIONE WIFI IN CODICE
         */

        /*wifiMan = (WifiManager) getApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMan.isWifiEnabled() == false) {
            //Toast.makeText(getApplicationContext(), "wifi no good", Toast.LENGTH_SHORT).show();
            System.out.print(wifiMan.getWifiState());

            //wifiMan.setWifiEnabled(true);
        }*/

    }

    /**
     *  Questa Classe crea una task per eseguire le funzioni di apertura del cancello,
     *
     */
    private class openGate extends AsyncTask<Void, Void, Void> {


        /**
         * Metodo eseguito in background si occupa di creare la connessine, passare i dati in post per l' apertura
         * e scaricare la pagina generata dal codice php
         *
         * @param params
         * @return null
         */
        @Override
        protected Void doInBackground(Void... params) {

            DataOutputStream stream = null;
            HttpURLConnection connection = null;
            StringBuilder urlparam = new StringBuilder();
            String imei;

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
                String line;
                StringBuilder responeout = new StringBuilder();

                //DOWNLOAD PAGINA

                while ((line = br.readLine()) != null) {
                    responeout.append(line);
                }
                br.close();
                //System.getProperty("line.separator") + "Response " + System.getProperty("line.separator") + System.getProperty("line.separator") +
                out.append(responeout.toString());

                //TREAD PER LA MODIFICA DELL'INTERFACCIA
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(out);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Metodo eseguito in automatico dopo la la fine del doInBackaground, per
         * riabilitare i bottoni
         * @param aVoid
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            bottoneApertura.setActivated(true);
            bottoneChiusura.setActivated(true);

        }
    }

    /**
     * Questa classe crea una task che serve a chiudere il cancello
     */
    private class closeGate extends AsyncTask<Void, Void, Void>{

        /**
         * Metodo eseguito in background si occupa di creare la connessine, passare i dati in post per l' apertura
         * e scaricare la pagina generata dal codice php
         * @param params
         * @return null
         */
        @Override
        protected Void doInBackground(Void... params) {
            DataOutputStream stream = null;
            HttpURLConnection connection = null;
            StringBuilder urlparam = new StringBuilder();
            String imei;


            try {
                paginaURL = new URL("http://www.gate794.heliohost.org/access.php"); //URL

                connection = (HttpURLConnection) paginaURL.openConnection(); //ISTAURAZIONE CONNESSIONE
                urlparam.append("stato=2&IMEI="); //DATI PER POST

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
                String line;
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
            } catch (NullPointerException e){
                e.printStackTrace();
            }

            return null;

        }
    }


    /**
     * Questo metodo salva l'istanza attiva sull'app per non perdere dati durante la rotazione dell schermo
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString("textView", textView.getText().toString());

        super.onSaveInstanceState(outState);
    }

    private boolean isOnline() {
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
            case R.id.menu_config: {
                Toast.makeText(getApplicationContext(), "funge", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){}
                break;

            default:
                break;
        }
    }





}




