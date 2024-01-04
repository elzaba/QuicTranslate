package com.elzaba.quictranslate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceEdt;
    private ImageView micIV;
    private MaterialButton translateBtn;
    private TextView translatedTV;
    private static final int REQUEST_PERMISSION_CODE = 1;

    // Map language codes to their full names
    private static final String[] languageCodes = {
            "af", "sq", "ar", "be", "bn", "bg", "ca", "zh",
            "hr", "cs", "da", "nl", "en", "eo", "et", "fi",
            "fr", "gl", "ka", "de", "el", "gu", "ht", "he",
            "hi", "hu", "is", "id", "ga", "it", "ja", "kn",
            "ko", "lv", "lt", "mk", "ms", "mt", "mr", "no",
            "fa", "pl", "pt", "ro", "ru", "sk", "sl", "es",
            "sw", "sv", "tl", "ta", "te", "th", "tr", "uk",
            "ur", "vi", "cy"
            // Add more language codes as needed
    };
    private static final String[] languageNames = {
            "Afrikaans", "Albanian", "Arabic", "Belarusian", "Bengali", "Bulgarian", "Catalan", "Chinese",
            "Croatian", "Czech", "Danish", "Dutch", "English", "Esperanto", "Estonian", "Finnish",
            "French", "Galician", "Georgian", "German", "Greek", "Gujarati", "Haitian Creole", "Hebrew",
            "Hindi", "Hungarian", "Icelandic", "Indonesian", "Irish", "Italian", "Japanese", "Kannada",
            "Korean", "Latvian", "Lithuanian", "Macedonian", "Malay", "Maltese", "Marathi", "Norwegian",
            "Persian", "Polish", "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian", "Spanish",
            "Swahili", "Swedish", "Tagalog", "Tamil", "Telugu", "Thai", "Turkish", "Ukrainian",
            "Urdu", "Vietnamese", "Welsh"
            // Add more language names in the same order as languageCodes
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceEdt = findViewById(R.id.idEdtSource);
        micIV = findViewById(R.id.idIVMic);
        translateBtn = findViewById(R.id.idBtnTranslate);
        translatedTV = findViewById(R.id.idTVTranslatedTV);

        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item,languageNames);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item,languageNames);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        // Set spinner selection listener to get language codes instead of names
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Set the corresponding language code as a tag
                fromSpinner.setTag(languageCodes[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Set the corresponding language code as a tag
                toSpinner.setTag(languageCodes[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedTV.setText("");
                String fromLanguage = Objects.requireNonNull(fromSpinner.getTag()).toString();
                String toLanguage = Objects.requireNonNull(toSpinner.getTag()).toString();
                if(sourceEdt.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your text to translate", Toast.LENGTH_SHORT).show();
                } else if(fromLanguage.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please select source language", Toast.LENGTH_SHORT).show();
                } else if(toLanguage.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please select target language", Toast.LENGTH_SHORT).show();
                } else {
                    String sourceText = Objects.requireNonNull(sourceEdt.getText()).toString();
                    DownloadConditions conditions = new DownloadConditions.Builder()
                            .requireWifi()
                            .build();
                    translatedTV.setText("Downloading Model");
                    TranslatorOptions options =
                            new TranslatorOptions.Builder()
                                    .setSourceLanguage(Objects.requireNonNull(TranslateLanguage.fromLanguageTag(fromLanguage)))
                                    .setTargetLanguage(Objects.requireNonNull(TranslateLanguage.fromLanguageTag(toLanguage)))
                                    .build();
                    Translator translator = Translation.getClient(options);
                    translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            translatedTV.setText("Translating...");
                            translator.translate(sourceText)
                                    .addOnSuccessListener(translatedText -> {
                                        translatedTV.setText(translatedText);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(MainActivity.this, "Failed to translate"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to translate"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Now");
                try{
                    startActivityForResult(i, REQUEST_PERMISSION_CODE);
                } catch(Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PERMISSION_CODE) {
            if(resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdt.setText(result.get(0));
            }
        }
    }
}