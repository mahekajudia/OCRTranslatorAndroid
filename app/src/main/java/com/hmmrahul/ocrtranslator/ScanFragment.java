package com.hmmrahul.ocrtranslator;

import static android.app.Activity.RESULT_OK;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.method.ScrollingMovementMethod;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.hmmrahul.ocrtranslator.databinding.FragmentScanBinding;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.util.Locale;


public class ScanFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    FragmentScanBinding fragmentScanBinding;
    Bitmap bitmap;
    int languageCode = FirebaseTranslateLanguage.HI;
    String inputText = null;
    TextToSpeech textToSpeech;
    String scannedText;
    Locale languageT2S = Locale.ENGLISH;

    public ScanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStop() {
        super.onStop();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentScanBinding = FragmentScanBinding.inflate(inflater, container, false);

        setUpSpinner();

        //used for TextView Scrolling effect
        fragmentScanBinding.scannedText.setMovementMethod(new ScrollingMovementMethod());
        fragmentScanBinding.translatedText.setMovementMethod(new ScrollingMovementMethod());


        fragmentScanBinding.scanNowFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .start(getContext(), ScanFragment.this);
            }
        });

        fragmentScanBinding.copyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String copyscannedText = fragmentScanBinding.translatedText.getText().toString();
                copyToClipBoard(copyscannedText);
            }
        });

        fragmentScanBinding.txtToSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textToSpeech != null) {
                    textToSpeech.stop();
                    textToSpeech.shutdown();
                }
                textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i == TextToSpeech.SUCCESS) {
                            textToSpeech.setLanguage(languageT2S);
                            textToSpeech.setSpeechRate(0.9f);
                            textToSpeech.speak(scannedText, TextToSpeech.QUEUE_FLUSH, null, null);
                            Toast.makeText(getContext(), "Playing Translated Text", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }

                });

            }
        });

        fragmentScanBinding.spinner.setOnItemSelectedListener(this);

        return fragmentScanBinding.getRoot();
    }

    private void setUpSpinner() {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(), R.array.languages, R.layout.spinner_dropdown_text);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_text);
        fragmentScanBinding.spinner.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), resultUri);
                    getTextFromImage(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void getTextFromImage(Bitmap bitmap) {
        TextRecognizer recognizer = new TextRecognizer.Builder(getContext()).build();
        if (!recognizer.isOperational()) {
            Toast.makeText(getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < textBlockSparseArray.size(); i++) {
                TextBlock textBlock = textBlockSparseArray.valueAt(i);
                stringBuilder.append(textBlock.getValue());
                stringBuilder.append("\n");
            }
            inputText = stringBuilder.toString();
            translateText(inputText);

        }
    }

    private void translateText(String input) {
        fragmentScanBinding.translatingTextLable.setText("Downloading Model...");
        fragmentScanBinding.translatingTextLable.setVisibility(View.VISIBLE);
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(languageCode)
                .build();


        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        fragmentScanBinding.animationView.setVisibility(View.GONE);
        fragmentScanBinding.scanNowFrame.setVisibility(View.GONE);
        fragmentScanBinding.tipsText.setVisibility(View.GONE);
        fragmentScanBinding.translatedTextLable.setVisibility(View.GONE);
        fragmentScanBinding.translatedText.setVisibility(View.GONE);
        fragmentScanBinding.scannedTextLable.setVisibility(View.GONE);
        fragmentScanBinding.scannedText.setVisibility(View.GONE);
        fragmentScanBinding.bottomLinearLayout.setVisibility(View.GONE);
        fragmentScanBinding.scannedTextProgressBar.setVisibility(View.VISIBLE);
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                fragmentScanBinding.translatingTextLable.setText("Translating...");
                translator.translate(input)
                        .addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                fragmentScanBinding.scannedTextProgressBar.setVisibility(View.GONE);
                                fragmentScanBinding.tipsText.setVisibility(View.GONE);
                                fragmentScanBinding.translatingTextLable.setVisibility(View.GONE);
                                fragmentScanBinding.translatedText.setText(s);
                                scannedText = s;
                                fragmentScanBinding.translatedTextLable.setVisibility(View.VISIBLE);
                                fragmentScanBinding.translatedText.setVisibility(View.VISIBLE);
                                fragmentScanBinding.scannedText.setText(input);
                                fragmentScanBinding.bottomLinearLayout.setVisibility(View.VISIBLE);
                                fragmentScanBinding.scanNowFrame.setVisibility(View.VISIBLE);
                                fragmentScanBinding.scanNowText.setText("Retake");
                                fragmentScanBinding.txtToSpeech.setVisibility(View.VISIBLE);
                                fragmentScanBinding.copyTextFrame.setVisibility(View.VISIBLE);
                                fragmentScanBinding.scannedTextLable.setVisibility(View.VISIBLE);
                                fragmentScanBinding.scannedText.setVisibility(View.VISIBLE);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        fragmentScanBinding.scannedTextProgressBar.setVisibility(View.GONE);
                        fragmentScanBinding.animationView.setVisibility(View.VISIBLE);
                        fragmentScanBinding.tipsText.setVisibility(View.VISIBLE);
                        fragmentScanBinding.translatingTextLable.setVisibility(View.GONE);
                        fragmentScanBinding.bottomLinearLayout.setVisibility(View.VISIBLE);
                        fragmentScanBinding.scanNowFrame.setVisibility(View.VISIBLE);
                        fragmentScanBinding.scanNowText.setText("Scan Now");
                        fragmentScanBinding.scannedText.setVisibility(View.GONE);
                        fragmentScanBinding.copyTextFrame.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Fail to Translate" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                fragmentScanBinding.scannedTextProgressBar.setVisibility(View.GONE);
                fragmentScanBinding.animationView.setVisibility(View.VISIBLE);
                fragmentScanBinding.tipsText.setVisibility(View.VISIBLE);
                fragmentScanBinding.translatingTextLable.setVisibility(View.GONE);
                fragmentScanBinding.bottomLinearLayout.setVisibility(View.VISIBLE);
                fragmentScanBinding.scanNowFrame.setVisibility(View.VISIBLE);
                fragmentScanBinding.scanNowText.setText("Scan Now");
                fragmentScanBinding.txtToSpeech.setVisibility(View.GONE);
                fragmentScanBinding.copyTextFrame.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Fail to Download Language Model" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public int getLanguageCode(String language) {
        int languageCode = 0;
        switch (language) {
            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                languageT2S = Locale.forLanguageTag("af");
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
                languageT2S = new Locale("ja_JP");
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
                languageT2S = new Locale("ur_IN");
                break;
            default:
                languageCode = 0;
        }
        return languageCode;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        String text = adapterView.getItemAtPosition(pos).toString();
        languageCode = getLanguageCode(text);
        if (inputText != null) {
            translateText(inputText);
            if (textToSpeech != null) {
                textToSpeech.stop();
                textToSpeech.shutdown();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void copyToClipBoard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied data", text);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(getActivity().getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
    }
}