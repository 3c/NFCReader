package com.junglewind.nfcreader;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by swp on 7/6/16.
 */
public class NfcReaderActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_reader);
        Intent intent = getIntent();
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Log.w("NfcReader", tagFromIntent.toString());
        Log.i("NfcReader","==============================");
        Log.e("NfcReader", "id: " + tagFromIntent.getId().toString());
        String[] techs = tagFromIntent.getTechList();
        for (String t :techs) {
            Log.e("NfcReader","tech: " + t);
        }

        TextView balanceTextView = (TextView)findViewById(R.id.balance);

        BeijingMunicipal beijingMunicipal = new BeijingMunicipal(tagFromIntent);
        if (beijingMunicipal.verifyAndRead()){
            Log.i("NfcReader","yes! found a beijing municipal card");
            balanceTextView.setText(String.format("Balance：%.2f CNY",beijingMunicipal.getBalance()/100.0f));
        }else{
            Log.e("NfcReader","not a beijing municipal card");
            balanceTextView.setText("Read NFC Failed!");
        }
    }
}
