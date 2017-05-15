package samuele794.cancello;

import android.os.AsyncTask;
import android.os.PersistableBundle;
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

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private URL paginaURL;
    private InputStream risposta;
    private TextView textView;
    private String text;



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
       // OpenGate();
        new Gt().execute();
    }

    public class Gt extends AsyncTask<Void, Void, Void>{


        @Override
        protected Void doInBackground(Void... params) {

            try{
                paginaURL = new URL("http://www.gate794.heliohost.org/access.php"); //URL

                HttpURLConnection connection = (HttpURLConnection)paginaURL.openConnection(); //ISTAURAZIONE CONNESSIONE
                String urlparam= "a=25"; //DATI PER POST
                //CONFIGURAZIONE STREAM POST
                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANUGAGE", "en-US,en;0.5");
                connection.setDoOutput(true);

                DataOutputStream stream = new DataOutputStream(connection.getOutputStream()); //CREAZIONE STREAM
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


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
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
