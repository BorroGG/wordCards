package com.example.wordscards;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wordscards.db.CollectionsToLearn;
import com.example.wordscards.db.CollectionsWordsCount;
import com.example.wordscards.db.WordToLearn;
import com.example.wordscards.db.WordsDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/*TODO :
 *  1) Добавить коллекции для слов
 *  2) Добавить русский язык
 *  3) Попробовать добавить озвучку текста
 *  4) Свайпы для слов
 *  5) Поменять иконку приложения */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, ChooseWordDescriptionDialog.OnLinkItemSelectedListener {

    Button buttAddWord, buttWord, buttPrevious, buttNext, buttAdd, buttDeleteWord, buttCheckWord,
            buttToStart, buttToEnd, buttShuffleCards, buttList, buttGear, buttAddCollection,
            buttAllWords;
    EditText edWord, edDescription;
    TextView tvCountWords, tvFromSite;
    List<WordToLearn> wordsToLearn = new ArrayList<>();
    List<CollectionsToLearn> collectionsToLearn = new ArrayList<>();
    List<CollectionsWordsCount> collectionsWordsCounts = new ArrayList<>();
    RelativeLayout mainLayout, layoutCollectionsForData;
    int currentWord = 0;
    Boolean isWord = true;
    Boolean isCollectionDelete = false;
    Button[] collections;
    static volatile Boolean searchComplete = false;
    static String word;
    static String textFromSite;
    static List<String> descriptionTexts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mainLayout = findViewById(R.id.mainLayout);
        layoutCollectionsForData = findViewById(R.id.layoutCollectionsForData);

        buttAddWord = findViewById(R.id.buttAddWord);
        buttAdd = findViewById(R.id.buttAdd);
        buttWord = findViewById(R.id.buttWord);
        buttPrevious = findViewById(R.id.buttPrevious);
        buttNext = findViewById(R.id.buttNext);
        buttDeleteWord = findViewById(R.id.buttDeleteWord);
        buttCheckWord = findViewById(R.id.buttCheckWord);
        buttToStart = findViewById(R.id.buttToStart);
        buttToEnd = findViewById(R.id.buttToEnd);
        buttShuffleCards = findViewById(R.id.buttShuffleCards);
        buttList = findViewById(R.id.buttList);
        buttGear = findViewById(R.id.buttGear);
        buttAddCollection = findViewById(R.id.buttAddCollection);
        buttAllWords = findViewById(R.id.buttAllWords);

        edWord = findViewById(R.id.edWord);
        edDescription = findViewById(R.id.edDescription);

        tvCountWords = findViewById(R.id.tvCountWords);
        tvFromSite = findViewById(R.id.tvFromSite);

        buttAddWord.setOnClickListener(this);
        buttList.setOnClickListener(this);
        buttGear.setOnClickListener(this);
        buttShuffleCards.setOnClickListener(this);
        buttAdd.setOnClickListener(this);
        buttWord.setOnClickListener(this);
        buttPrevious.setOnClickListener(this);
        buttNext.setOnClickListener(this);
        buttDeleteWord.setOnClickListener(this);
        buttCheckWord.setOnClickListener(this);
        buttToStart.setOnClickListener(this);
        buttToEnd.setOnClickListener(this);
        buttAddCollection.setOnClickListener(this);
        buttAllWords.setOnClickListener(this);

        setData();
        String allWordsText = "All words " + wordsToLearn.size();
        buttAllWords.setText(allWordsText);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttAddWord:
                addNewWord();
                break;
            case R.id.buttWord:
                if (wordsToLearn.size() > 0) {
                    if (isWord) {
                        buttWord.setText(wordsToLearn.get(currentWord).description);
                        isWord = false;
                    } else {
                        buttWord.setText(wordsToLearn.get(currentWord).word);
                        isWord = true;
                    }
                }
                break;
            case R.id.buttPrevious:
                if (wordsToLearn.size() > 0) {
                    currentWord--;
                    tvCountWords.setText(getCountString());
                    if (wordsToLearn.size() > 1) {
                        buttNext.setEnabled(true);
                    }
                    if (currentWord == 0) {
                        buttPrevious.setEnabled(false);
                    }
                    if (currentWord == wordsToLearn.size() - 1) {
                        buttNext.setEnabled(false);
                    }
                    buttWord.setText(wordsToLearn.get(currentWord).word);
                    isWord = true;
                }
                break;
            case R.id.buttNext:
                if (wordsToLearn.size() > 0) {
                    currentWord++;
                    tvCountWords.setText(getCountString());
                    if (wordsToLearn.size() > 1) {
                        buttPrevious.setEnabled(true);
                    }
                    if (currentWord == wordsToLearn.size() - 1) {
                        buttNext.setEnabled(false);
                    }
                    buttWord.setText(wordsToLearn.get(currentWord).word);
                    isWord = true;
                }
                break;
            case R.id.buttAdd:
                addWordToDB();
                break;
            case R.id.buttDeleteWord:
                if (wordsToLearn.size() > 0) {
                    try {
                        deleteFromDB().join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        getAllWords().join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    buttPrevious.performClick();
                    tvCountWords.setText(getCountString());
                    String allWordsText = "All words " + wordsToLearn.size();
                    buttAllWords.setText(allWordsText);
                }
                break;
            case R.id.buttCheckWord:
                try {
                    addAutoDescription();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buttToStart:
                if (wordsToLearn.size() > 0) {
                    currentWord = 1;
                    buttPrevious.performClick();
                }
                break;
            case R.id.buttToEnd:
                if (wordsToLearn.size() > 0) {
                    currentWord = wordsToLearn.size() - 2;
                    buttNext.performClick();
                    break;
                }
            case R.id.buttShuffleCards:
                Collections.shuffle(wordsToLearn);
                if (currentWord == 0) {
                    buttPrevious.setEnabled(false);
                }

                if (currentWord == wordsToLearn.size() - 1) {
                    buttNext.setEnabled(false);
                }

                if (!wordsToLearn.isEmpty()) {
                    buttWord.setText(wordsToLearn.get(currentWord).word);
                } else {
                    String noWords = "Add some words to start learning";
                    buttWord.setText(noWords);
                }
                break;
            case R.id.buttList:
                setStateViewCollections();
                setCollections();
                break;
            case R.id.buttGear:
                break;
            case R.id.buttAllWords:
                isCollectionDelete = false;
                setStateViewWords();
                try {
                    getAllWords().join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setWords();
                break;
            case R.id.buttAddCollection:
                createDialogAddCollection();
                break;
        }
    }

    private void createDialogAddCollection() {
        //Получаем вид с файла prompt.xml, который применим для диалогового окна:
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompt, null);

        //Создаем AlertDialog
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);

        //Настраиваем prompt.xml для нашего AlertDialog:
        mDialogBuilder.setView(promptsView);

        //Настраиваем отображение поля для ввода текста в открытом диалоге:
        final EditText userInput = promptsView.findViewById(R.id.input_text);

        //Настраиваем сообщение в диалоговом окне:
        mDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            public void onClick(DialogInterface dialog, int id) {
                                //Вводим текст и отображаем в строке ввода на основном экране:
                                //final_text.setText(userInput.getText());
                                CollectionsToLearn collectionsToLearn = new CollectionsToLearn();
                                collectionsToLearn.name = userInput.getText().toString();
                                try {
                                    addCollectionToDb(collectionsToLearn).join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                setCollections();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        //Создаем AlertDialog:
        AlertDialog alertDialog = mDialogBuilder.create();

        //и отображаем его:
        alertDialog.show();
    }

    private Thread addCollectionToDb(CollectionsToLearn collectionsToLearn) {
        Thread thread = new Thread(() -> WordsDatabase.getInstance(getApplicationContext()).collectionsToLearnDao().insertAll(collectionsToLearn));
        thread.start();
        return thread;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setCollections() {
        try {
            getAllCollections().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            getCountWordsInCollections().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (Objects.nonNull(collections)) {
            for (Button collection : collections) {
                layoutCollectionsForData.removeView(collection);
            }
        }
        collections = new Button[collectionsToLearn.size()];
        for (int i = 0; i < collectionsToLearn.size(); i++) {
            int buttonStyle = R.drawable.button;
            collections[i] = new Button(getApplicationContext());
            Integer count = 0;
            for (int j = 0; j < collectionsWordsCounts.size(); j++) {
                if (collectionsWordsCounts.get(j).collectionId.equals(collectionsToLearn.get(i).collectionId)) {
                    count = collectionsWordsCounts.get(j).countWords;
                }
            }
            String text = collectionsToLearn.get(i).name + " " + count;
            collections[i].setText(text);
            collections[i].setId(View.generateViewId());
            collections[i].setBackgroundResource(buttonStyle);
            collections[i].setTextColor(getResources().getColor(R.color.white));
            int collectionId = collectionsToLearn.get(i).collectionId;
            int numberButton = i;
            collections[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    createDeleteDialog(numberButton);
                    return true;
                }
            });
            collections[i].setOnClickListener(v -> {
                setStateViewWords();
                for (int j = 0; j < collectionsToLearn.size(); j++) {
                    collections[j].setVisibility(View.INVISIBLE);
                }
                try {
                    getWordsFromCollection(collectionId).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setWords();
                isCollectionDelete = false;
            });

            DisplayMetrics dm = getResources().getDisplayMetrics();
            //int dpInPx300 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, dm);
            int dpInPx20 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, dm);

            RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            buttonParams.topMargin = dpInPx20;

            if (i != 0) {
                buttonParams.addRule(RelativeLayout.BELOW, collections[i - 1].getId());
            } else {
                buttonParams.addRule(RelativeLayout.BELOW, buttAllWords.getId());
            }

            layoutCollectionsForData.addView(collections[i], buttonParams);
        }
    }

    private void createDeleteDialog(int numberButt) {
        //Получаем вид с файла prompt.xml, который применим для диалогового окна:
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.delete_dialog, null);

        //Создаем AlertDialog
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);

        //Настраиваем prompt.xml для нашего AlertDialog:
        mDialogBuilder.setView(promptsView);

        //Настраиваем сообщение в диалоговом окне:
        mDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    deleteCollectionFromDb(collectionsToLearn.get(numberButt)).join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                isCollectionDelete = true;
                                setCollections();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        //Создаем AlertDialog:
        AlertDialog alertDialog = mDialogBuilder.create();

        //и отображаем его:
        alertDialog.show();
    }

    private Thread deleteCollectionFromDb(CollectionsToLearn collectionsToLearn ) {
        Thread thread = new Thread(() ->  WordsDatabase.getInstance(getApplicationContext()).collectionsToLearnDao().delete(collectionsToLearn));
        thread.start();
        return thread;
    }

    private Thread getWordsFromCollection(int collectionId) {
        Thread thread = new Thread(() -> wordsToLearn = WordsDatabase.getInstance(getApplicationContext()).wordToCollectionDao().getWordToLearnByCollection(collectionId));
        thread.start();
        return thread;
    }

    private Thread getCountWordsInCollections() {
        Thread thread = new Thread(() -> collectionsWordsCounts = WordsDatabase.getInstance(getApplicationContext()).wordToCollectionDao().getCountWordsInCollections());
        thread.start();
        return thread;
    }

    private Thread getAllCollections() {
        Thread thread = new Thread(() -> collectionsToLearn = WordsDatabase.getInstance(getApplicationContext()).collectionsToLearnDao().getAll());
        thread.start();
        return thread;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBackPressed() {
        if (buttWord.getVisibility() == View.INVISIBLE) {
            setStateViewWords();
            edWord.setText("");
            edDescription.setText("");
            textFromSite = null;
            if (isCollectionDelete) {
                buttAllWords.performClick();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addAutoDescription() throws InterruptedException {
        word = edWord.getText().toString();
        searchComplete = false;
        new HTTPReqTask().execute();
        while (!searchComplete) {
            Thread.sleep(100);
        }

        FragmentManager manager = getSupportFragmentManager();
        ChooseWordDescriptionDialog myDialogFragment = new ChooseWordDescriptionDialog();
        final String[] variants = new String[descriptionTexts.size()];
        for (int i = 0; i < descriptionTexts.size(); i++) {
            variants[i] = descriptionTexts.get(i);
        }
        descriptionTexts.clear();
        Bundle args = new Bundle();
        if (variants.length == 0) {
            args.putBoolean("isEmpty", true);
        } else {
            args.putBoolean("isEmpty", false);
            args.putStringArray("array", variants);
        }
        myDialogFragment.setArguments(args);

        myDialogFragment.show(manager, "chooseWordDescriptionDialog");
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onLinkItemSelected(String link) {
        edDescription.setText(link);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .remove(manager.findFragmentByTag("chooseWordDescriptionDialog"))
                .commit();
    }

    private static class HTTPReqTask extends AsyncTask<Void, Void, Void> {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL("https://www.macmillandictionary.com/dictionary/british/" + word);
                urlConnection = (HttpURLConnection) url.openConnection();

                int code = urlConnection.getResponseCode();
                if (code != 200) {
                    throw new IOException("Invalid response from server: " + code);
                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    stringBuilder.append(line);
                }
                Document doc = Jsoup.parse(stringBuilder.toString());
                Elements elements = doc.select("span").addClass("DEFINITION");
                for (int i = 0; i < elements.size(); i++) {
                    Element element = elements.get(i);
                    if (element.parent().attributes().get("class").equals("SENSE-CONTENT")
                            && element.attributes().get("class").equals("DEFINITION")) {
                        Log.i("parse line span", "number = " + i + " " + element.text());
                        descriptionTexts.add(element.text());
                    }
                }
                if (descriptionTexts.isEmpty()) {

                }
                searchComplete = true;
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addWordToDB() {
        WordToLearn wordToLearn = new WordToLearn();
        wordToLearn.word = edWord.getText().toString();
        wordToLearn.description = edDescription.getText().toString();

        try {
            addToDB(wordToLearn).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setStateViewWords();
        try {
            getAllWords().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        currentWord = wordsToLearn.size() - 2;
        buttNext.performClick();
        edWord.setText("");
        edDescription.setText("");
        tvCountWords.setText(getCountString());
        textFromSite = null;
        String allWordsText = "All words " + wordsToLearn.size();
        buttAllWords.setText(allWordsText);
    }

    private void setStateViewCollections() {
        buttAddWord.setVisibility(View.INVISIBLE);
        buttWord.setVisibility(View.INVISIBLE);
        buttPrevious.setVisibility(View.INVISIBLE);
        buttNext.setVisibility(View.INVISIBLE);
        buttDeleteWord.setVisibility(View.INVISIBLE);
        tvCountWords.setVisibility(View.INVISIBLE);
        buttToStart.setVisibility(View.INVISIBLE);
        buttToEnd.setVisibility(View.INVISIBLE);
        buttShuffleCards.setVisibility(View.INVISIBLE);

        edWord.setVisibility(View.INVISIBLE);
        edDescription.setVisibility(View.INVISIBLE);
        buttAdd.setVisibility(View.INVISIBLE);
        buttCheckWord.setVisibility(View.INVISIBLE);
        tvFromSite.setVisibility(View.INVISIBLE);

        buttAllWords.setVisibility(View.VISIBLE);
        buttAddCollection.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setStateViewWords() {
        buttAddWord.setVisibility(View.VISIBLE);
        buttWord.setVisibility(View.VISIBLE);
        buttPrevious.setVisibility(View.VISIBLE);
        buttNext.setVisibility(View.VISIBLE);
        buttDeleteWord.setVisibility(View.VISIBLE);
        tvCountWords.setVisibility(View.VISIBLE);
        buttToStart.setVisibility(View.VISIBLE);
        buttToEnd.setVisibility(View.VISIBLE);
        buttShuffleCards.setVisibility(View.VISIBLE);

        edWord.setVisibility(View.INVISIBLE);
        edDescription.setVisibility(View.INVISIBLE);
        buttAdd.setVisibility(View.INVISIBLE);
        buttCheckWord.setVisibility(View.INVISIBLE);
        tvFromSite.setVisibility(View.INVISIBLE);
        buttAllWords.setVisibility(View.INVISIBLE);
        buttAddCollection.setVisibility(View.INVISIBLE);
        if (Objects.nonNull(collections)) {
            for (Button collection : collections) {
                collection.setVisibility(View.INVISIBLE);
            }
        }
    }

    private Thread deleteFromDB() {
        Thread thread = new Thread(() -> WordsDatabase.getInstance(getApplicationContext()).wordToLearnDao().delete(wordsToLearn.get(currentWord)));
        thread.start();
        return thread;
    }

    private Thread addToDB(WordToLearn wordToLearn) {
        Thread thread = new Thread(() -> WordsDatabase.getInstance(getApplicationContext()).wordToLearnDao().insertAll(wordToLearn));
        thread.start();
        return thread;
    }

    private void addNewWord() {
        buttAddWord.setVisibility(View.INVISIBLE);
        buttWord.setVisibility(View.INVISIBLE);
        buttPrevious.setVisibility(View.INVISIBLE);
        buttNext.setVisibility(View.INVISIBLE);
        buttDeleteWord.setVisibility(View.INVISIBLE);
        tvCountWords.setVisibility(View.INVISIBLE);
        buttToStart.setVisibility(View.INVISIBLE);
        buttToEnd.setVisibility(View.INVISIBLE);
        buttShuffleCards.setVisibility(View.INVISIBLE);

        edWord.setVisibility(View.VISIBLE);
        edDescription.setVisibility(View.VISIBLE);
        buttAdd.setVisibility(View.VISIBLE);
        buttCheckWord.setVisibility(View.VISIBLE);
        tvFromSite.setVisibility(View.VISIBLE);
    }

    private void setData() {

        try {
            getAllWords().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setWords();
    }

    private void setWords() {

        tvCountWords.setText(getCountString());

        if (currentWord == 0) {
            buttPrevious.setEnabled(false);
        }

        if (currentWord == wordsToLearn.size() - 1) {
            buttNext.setEnabled(false);
        }

        if (!wordsToLearn.isEmpty()) {
            buttWord.setText(wordsToLearn.get(currentWord).word);
        } else {
            String noWords = "Add some words to start learning";
            buttWord.setText(noWords);
        }
    }

    private String getCountString() {
        return wordsToLearn.size() == 0 ? "0/0" : (currentWord + 1) + "/" + wordsToLearn.size();
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        setData();
//    }

    private synchronized Thread getAllWords() {
        Thread thread = new Thread(() -> wordsToLearn = WordsDatabase.getInstance(getApplicationContext()).wordToLearnDao().getAll());
        thread.start();
        return thread;
    }
}