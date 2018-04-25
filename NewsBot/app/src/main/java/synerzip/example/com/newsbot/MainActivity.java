package synerzip.example.com.newsbot;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;

import java.util.Locale;
import java.util.Map;
import com.amazonaws.mobileconnectors.lex.interactionkit.Response;
import com.amazonaws.mobileconnectors.lex.interactionkit.config.InteractionConfig;
import com.amazonaws.mobileconnectors.lex.interactionkit.ui.InteractiveVoiceView;

import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_component);
        Button txtToVoiceBtn=(Button)findViewById(R.id.textToVoice);
        txtToVoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,TextActivity.class);
                startActivity(intent);
            }
        });
        requestPermissionIfNeeded();


        AWSMobileClient.getInstance().initialize(this).execute();
        init();
    }

    private void requestPermissionIfNeeded() {
        String[] perms = {READ_CONTACTS, CALL_PHONE, CAMERA, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE,RECORD_AUDIO};
        if (!hasPermissions(this, perms)) {
            ActivityCompat.requestPermissions(this, perms, PERMISSION_REQUEST_CODE);
        }
    }

    protected boolean hasPermissions(Context context, String... permissions) {
        boolean hasPermission = false;
        if (context != null && permissions != null) {
            hasPermission = true;
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    hasPermission = false;
                    break;
                }
            }
        }
        return hasPermission;
    }

    public void init(){
        InteractiveVoiceView voiceView =
                (InteractiveVoiceView) findViewById(R.id.voiceInterface);
        final TextView txtBotResponse=(TextView)findViewById(R.id.txt_bot_response);

        voiceView.setInteractiveVoiceListener(
                new InteractiveVoiceView.InteractiveVoiceListener() {

                    @Override
                    public void dialogReadyForFulfillment(Map slots, String intent) {
                        Log.d(TAG, String.format(
                                Locale.US,
                                "Dialog ready for fulfillment:\n\tIntent: %s\n\tSlots: %s",
                                intent,
                                slots.toString()));
                    }

                    @Override
                    public void onResponse(Response response) {
                        Log.d(TAG, "Bot response: " + response.getTextResponse());
                        txtBotResponse.setText("");
                        txtBotResponse.setText(response.getTextResponse());
                    }

                    @Override
                    public void onError(String responseText, Exception e) {
                        Log.e(TAG, "Error: " + responseText, e);
                    }
                });

        voiceView.getViewAdapter().setCredentialProvider(AWSMobileClient.getInstance().getCredentialsProvider());

        //replace parameters with your botname, bot-alias
        voiceView.getViewAdapter()
                .setInteractionConfig(
                        new InteractionConfig("YOUR_BOT_NAME","$LATEST"));

        voiceView.getViewAdapter()
                .setAwsRegion(getApplicationContext()
                        .getString(R.string.aws_region));
    }
}
