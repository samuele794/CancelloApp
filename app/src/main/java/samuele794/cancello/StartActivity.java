package samuele794.cancello;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StartActivity extends AppCompatActivity {


    private Handler cha =null;

    private final Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent= new Intent(StartActivity.this,MainActivity.class);
            startActivity(intent);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        cha = new Handler();
        cha.postDelayed(myRunnable, 2000);

    }
}
