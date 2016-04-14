package org.mamkschools.mhs.fbla_mobileapp_2016;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.mamkschools.mhs.fbla_mobileapp_2016.lib.*;

import java.util.Scanner;

public class LegalInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_info);
        TextView LegalText = (TextView) findViewById(R.id.legaltext);
        Scanner s = new Scanner(this.getResources().openRawResource(R.raw.legal));
        String text = "";
        try{
            do{
                text += s.nextLine();
            } while(s.hasNextLine());
        }catch(Exception e){
            Debug.log(e.getMessage());
        }
        LegalText.setText(text);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
