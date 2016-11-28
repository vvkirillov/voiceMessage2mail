package nr.com.voice2mail.gui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import nr.com.voice2mail.R;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Application main activity
 *
 * Created by kirillov
 *
 */
@ContentView(R.layout.activity_main)
public class MainActivity extends RoboActivity {
    private static final String DEBUG_TAG = MainActivity.class.getSimpleName();
    private static final int SPEECH_INTENT_ID = 1;
    private static final String SEND_BUTTON_STATE = "SEND_BUTTON_STATE";

    @InjectView(R.id.txtText)
    private EditText txtText;

    @InjectView(R.id.btnTalk)
    private Button btnTalk;

    @InjectView(R.id.btnSend)
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSend.setEnabled(false);

        btnTalk.setOnClickListener(l -> promptToSpeech());
        txtText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                btnSend.setEnabled(txtText.getText().length() != 0);
            }
        });
    }

    private void promptToSpeech(){
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.talk));

        try {
            startActivityForResult(intent, SPEECH_INTENT_ID);
        }catch (ActivityNotFoundException e){
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_is_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SPEECH_INTENT_ID){
            if(resultCode == RESULT_OK && data != null){
                final List<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if(result != null && !result.isEmpty()){
                    final String textResult = result.get(0);
                    if(!textResult.isEmpty()) {
                        txtText.setText(textResult);
                        Log.d(DEBUG_TAG, String.format("Text recognized: \"%s\" - success", textResult));
                        btnSend.setEnabled(true);
                    }
                }
            }else if(resultCode == RESULT_CANCELED){
                Log.d(DEBUG_TAG, "Text recognition was cancelled by the user");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SEND_BUTTON_STATE, btnSend.isEnabled());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        btnSend.setEnabled(savedInstanceState.getBoolean(SEND_BUTTON_STATE, false));
    }
}
