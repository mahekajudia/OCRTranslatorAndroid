package com.hmmrahul.ocrtranslator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.hmmrahul.ocrtranslator.databinding.FragmentManualTranslatorBinding;

import java.util.Locale;


public class ManualTranslatorFragment extends Fragment {

    FragmentManualTranslatorBinding fragmentManualTranslatorBinding;
    int fromlanguageCode = -1;
    int tolanguageCode = -1;
    String inputText;
    TextToSpeech textToSpeech;
    String translatedText;
    Locale languageT2S = Locale.ENGLISH;

    public ManualTranslatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentManualTranslatorBinding = FragmentManualTranslatorBinding.inflate(inflater, container, false);

        setUpSpinner();


        fragmentManualTranslatorBinding.spinner1.setOnItemSelectedListener(new fromSpinnerClass());
        fragmentManualTranslatorBinding.spinner2.setOnItemSelectedListener(new toSpinnerClass());


        fragmentManualTranslatorBinding.scanNowFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputText = fragmentManualTranslatorBinding.typedText.getText().toString();
                translateText(inputText);
            }
        });
        fragmentManualTranslatorBinding.copyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String copyscannedText = fragmentManualTranslatorBinding.translatedText.getText().toString();
                copyToClipBoard(copyscannedText);
            }
        });
        fragmentManualTranslatorBinding.txtToSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i == TextToSpeech.SUCCESS) {
                            int res = textToSpeech.setLanguage(languageT2S);
                            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Toast.makeText(getContext(), "Language Not Supported", Toast.LENGTH_SHORT).show();
                            } else {
                                textToSpeech.setSpeechRate(0.9f);
                                textToSpeech.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, null);
                                Toast.makeText(getContext(), "Playing Translated Text", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        return fragmentManualTranslatorBinding.getRoot();
    }

    private void setUpSpinner() {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(), R.array.languages, R.layout.spinner_dropdown_text);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(getContext(), R.array.languages, R.layout.spinner_dropdown_text);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_text);
        adapter1.setDropDownViewResource(R.layout.spinner_dropdown_text);
        fragmentManualTranslatorBinding.spinner1.setAdapter(adapter);
        fragmentManualTranslatorBinding.spinner2.setAdapter(adapter1);
    }

    class fromSpinnerClass implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            String text = parent.getItemAtPosition(position).toString();
            fromlanguageCode = getLanguageCode(text);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class toSpinnerClass implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            String text = parent.getItemAtPosition(position).toString();
            tolanguageCode = getLanguageCode(text);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public int getLanguageCode(String language) {
        int languageCode = 0;
        switch (language) {
            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                languageT2S = new Locale("af_");
                break;
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                languageT2S = new Locale("ar_AE");
                break;
            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                languageT2S = new Locale("bn_IN");
                break;
            case "German":
                languageCode = FirebaseTranslateLanguage.DE;
                languageT2S = new Locale("de_");
                break;
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                languageT2S = new Locale("en_");
                break;
            case "Spanish":
                languageCode = FirebaseTranslateLanguage.ES;
                languageT2S = new Locale("es_");
                break;
            case "French":
                languageCode = FirebaseTranslateLanguage.FR;
                languageT2S = new Locale("fr_");
                break;
            case "Gujarati":
                languageCode = FirebaseTranslateLanguage.GU;
                languageT2S = new Locale("gu_IN");
                break;
            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                languageT2S = new Locale("hi_IN");
                break;
            case "Italian":
                languageCode = FirebaseTranslateLanguage.IT;
                languageT2S = new Locale("it_");
                break;
            case "Japanese":
                languageCode = FirebaseTranslateLanguage.JA;
                languageT2S = new Locale("ja_");
                break;
            case "Kannada":
                languageCode = FirebaseTranslateLanguage.KN;
                languageT2S = new Locale("kn_");
                break;
            case "Korean":
                languageCode = FirebaseTranslateLanguage.KO;
                languageT2S = new Locale("ko_");
                break;
            case "Marathi":
                languageCode = FirebaseTranslateLanguage.MR;
                languageT2S = new Locale("mr_");
                break;
            case "Malay":
                languageCode = FirebaseTranslateLanguage.MS;
                languageT2S = new Locale("ms_");
                break;
            case "Russian":
                languageCode = FirebaseTranslateLanguage.RU;
                languageT2S = new Locale("ru_");
                break;
            case "Tamil":
                languageCode = FirebaseTranslateLanguage.TA;
                languageT2S = new Locale("ta_");
                break;
            case "Telugu":
                languageCode = FirebaseTranslateLanguage.TE;
                languageT2S = new Locale("te_");
                break;
            case "Urdu":
                languageCode = FirebaseTranslateLanguage.UR;
                languageT2S = new Locale("ur_");
                break;
            default:
                languageCode = 0;
        }
        return languageCode;
    }

    private void translateText(String input) {
        fragmentManualTranslatorBinding.translatingTextLable.setText("Downloading Model...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromlanguageCode)
                .setTargetLanguage(tolanguageCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        fragmentManualTranslatorBinding.scanNowFrame.setVisibility(View.GONE);
        fragmentManualTranslatorBinding.translatedTextLable.setVisibility(View.GONE);
        fragmentManualTranslatorBinding.translatedText.setVisibility(View.GONE);
        fragmentManualTranslatorBinding.scannedTextLable.setVisibility(View.GONE);
        fragmentManualTranslatorBinding.typedText.setVisibility(View.GONE);
        fragmentManualTranslatorBinding.bottomLinearLayout.setVisibility(View.GONE);
        fragmentManualTranslatorBinding.scannedTextProgressBar.setVisibility(View.VISIBLE);
        fragmentManualTranslatorBinding.translatingTextLable.setVisibility(View.VISIBLE);
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translator.translate(input)
                        .addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                fragmentManualTranslatorBinding.translatingTextLable.setText("Translating...");
                                fragmentManualTranslatorBinding.scannedTextProgressBar.setVisibility(View.GONE);
                                fragmentManualTranslatorBinding.translatingTextLable.setVisibility(View.GONE);
                                fragmentManualTranslatorBinding.translatedText.setText(s);
                                translatedText = s;
                                fragmentManualTranslatorBinding.translatedTextLable.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.translatedText.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.typedText.setText(input);
                                fragmentManualTranslatorBinding.bottomLinearLayout.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.scanNowFrame.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.scanNowText.setText("Retranslate");
                                fragmentManualTranslatorBinding.txtToSpeech.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.copyTextFrame.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.scannedTextLable.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.typedText.setVisibility(View.VISIBLE);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        fragmentManualTranslatorBinding.scannedTextProgressBar.setVisibility(View.GONE);
                        fragmentManualTranslatorBinding.translatingTextLable.setVisibility(View.GONE);
                        fragmentManualTranslatorBinding.bottomLinearLayout.setVisibility(View.VISIBLE);
                        fragmentManualTranslatorBinding.scanNowFrame.setVisibility(View.VISIBLE);
                        fragmentManualTranslatorBinding.scanNowText.setText("Scan Now");
                        fragmentManualTranslatorBinding.txtToSpeech.setVisibility(View.GONE);
                        fragmentManualTranslatorBinding.copyTextFrame.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Fail to Translate" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                fragmentManualTranslatorBinding.scannedTextProgressBar.setVisibility(View.GONE);
                fragmentManualTranslatorBinding.translatingTextLable.setVisibility(View.GONE);
                fragmentManualTranslatorBinding.bottomLinearLayout.setVisibility(View.VISIBLE);
                fragmentManualTranslatorBinding.scanNowFrame.setVisibility(View.VISIBLE);
                fragmentManualTranslatorBinding.scanNowText.setText("Scan Now");
                fragmentManualTranslatorBinding.txtToSpeech.setVisibility(View.GONE);
                fragmentManualTranslatorBinding.copyTextFrame.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Fail to Download Language Model" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void copyToClipBoard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied data", text);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(getActivity().getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
    }

}