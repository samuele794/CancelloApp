package samuele794.cancello;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.jar.Manifest;

import javax.net.ssl.HttpsURLConnection;

import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private URL paginaURL;
    private InputStream risposta;
    private TextView textView;
    private String text;
    private static final int MY_PERMISSION_DATE = 0 ;
    private WifiManager wifiMan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Button bottone = (Button) findViewById(R.id.bottoneCancello);
        bottone.setOnClickListener(this);
        textView = (TextView) findViewById(R.id.DebugText);
        if( savedInstanceState != null){
            textView.setText(savedInstanceState.getString("textView"));
        }


    }

    @Override

    public void onClick(View v){
        wifiMan = (WifiManager) getApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiMan.isWifiEnabled() == false) {
            //Toast.makeText(getApplicationContext(), "wifi no good", Toast.LENGTH_SHORT).show();
            System.out.print(wifiMan.getWifiState());

                //wifiMan.setWifiEnabled(true);
        }
        new Gt().execute();
    }



    public class Gt extends AsyncTask<Void, Void, Void>{


        @Override
        protected Void doInBackground(Void... params) {

            DataOutputStream stream = null;
            HttpURLConnection connection = null;
            String urlparam = "";
            boolean a = true;
            do{     try{
                paginaURL = new URL("http://www.gate794.heliohost.org/access.php"); //URL

                connection = (HttpURLConnection)paginaURL.openConnection(); //ISTAURAZIONE CONNESSIONE
                urlparam= "a=25"; //DATI PER POST
                //CONFIGURAZIONE STREAM POST
                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANUGAGE", "en-US,en;0.5");
                connection.setDoOutput(true);
                stream = new DataOutputStream(connection.getOutputStream()); //CREAZIONE STREAM
                a = false;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                a = true;
                e.printStackTrace();
            }
            }while (a);
            /*
                try{

                }catch (IOException e){

                }*/



             try {
                 stream.writeBytes(urlparam); //INVIO DATI IN POST
                 stream.flush();
                 stream.close(); //CHIUSURA STREAM

                 //int response = connection.getResponseCode();
                 final StringBuilder out = new StringBuilder(); //"Request URL" + paginaURL
                 //out.append(System.getProperty("line.separator")+ "Request Paramenters " + urlparam);
                 //out.append(System.getProperty("line.separator")  + "Response Code " + response);

                 BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                 String line = "";
                 StringBuilder responeout = new StringBuilder();

                 while((line = br.readLine())!= null ){
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
             }catch (IOException e) {

                 e.printStackTrace();
             }




            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            //textView.setText(text);
            super.onPostExecute(aVoid);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState ) {

        outState.putString("textView", textView.getText().toString());

        super.onSaveInstanceState(outState);
    }
}
