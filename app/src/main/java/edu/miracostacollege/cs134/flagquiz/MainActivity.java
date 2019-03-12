// TODO: Trinidad and Tobago should be North America despite being 3 feet off the shore of Venezuela (literally unplayable)

package edu.miracostacollege.cs134.flagquiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.miracostacollege.cs134.flagquiz.model.Country;
import edu.miracostacollege.cs134.flagquiz.model.JSONLoader;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Flag Quiz";

    private static final String REGIONS = "pref_regions";          // Pref key for region
    private static final String CHOICES = "pref_numberOfChoices";  // Pref key for number of choices
    private static final int FLAGS_IN_QUIZ = 10;

    private int mChoices;                       // number of choices for each flag (preference)
    private String mRegion;                     // the region countries are picked from (preference)
    private Button[] mButtons = new Button[8];  // array of all button objects (displayed or not)
    private List<Country> mAllCountriesList;    // all the countries loaded from JSON
    private List<Country> mQuizCountriesList;   // countries in current quiz (just 10 of them)
    private Country mCorrectCountry;            // correct country for the current question
    private int mTotalGuesses;                  // number of total guesses made
    private int mCorrectGuesses;                // number of correct guesses
    private SecureRandom rng;                   // used to randomize the quiz
    private Handler handler;                    // used to delay loading next country

    private TextView mQuestionNumberTextView;   // shows current question #
    private ImageView mFlagImageView;           // displays a flag
    private TextView mAnswerTextView;           // displays correct answer

    // Preference listener object
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                    Log.i(TAG, "key: " + key);
                    if (key.equals(REGIONS))
                    {
                        String region = sharedPreferences.getString(REGIONS,
                                getString(R.string.default_region));
                        updateRegion(region);
                        resetQuiz();
                    }
                    else if (key.equals(CHOICES))
                    {
                        mChoices = Integer.parseInt(sharedPreferences.getString(CHOICES,
                                getString(R.string.default_choices)));
                        updateChoices();
                        resetQuiz();
                    }
                    Toast.makeText(MainActivity.this, R.string.restarting_quiz,
                            Toast.LENGTH_SHORT).show();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate list and helper objects
        mQuizCountriesList = new ArrayList<>(FLAGS_IN_QUIZ);
        rng = new SecureRandom();
        handler = new Handler();

        // Get preference values
        // DONE: Get a reference to SharedPreferences object
        try {
            mChoices = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(CHOICES, getString(R.string.default_choices)));
            mRegion = PreferenceManager.getDefaultSharedPreferences(this).getString(REGIONS,
                    getString(R.string.default_region)).replaceAll("_", " ");
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }

        // DONE: Get references to GUI components (textviews and imageview)
        mQuestionNumberTextView = findViewById(R.id.questionNumberTextView);
        mFlagImageView = findViewById(R.id.flagImageView);
        mAnswerTextView = findViewById(R.id.answerTextView);

        // DONE: Put all 4 buttons in the array (mButtons)
        mButtons[0] = findViewById(R.id.button);
        mButtons[1] = findViewById(R.id.button2);
        mButtons[2] = findViewById(R.id.button3);
        mButtons[3] = findViewById(R.id.button4);
        mButtons[4] = findViewById(R.id.button5);
        mButtons[5] = findViewById(R.id.button6);
        mButtons[6] = findViewById(R.id.button7);
        mButtons[7] = findViewById(R.id.button8);

        // Loop through buttons, set buttons higher than number of choices to invisible
        for (int i = 0; i < mButtons.length; i++) {
            mButtons[i].setVisibility((i < mChoices) ? View.VISIBLE : View.GONE);
        }

        // DONE: Set mQuestionNumberTextView's text to the appropriate strings.xml resource
        mQuestionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));

        // DONE: Load all the countries from the JSON file using the JSONLoader
        try {
            mAllCountriesList = JSONLoader.loadJSONFromAsset(this);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        // Attach preference listener
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);

        // DONE: Call the method resetQuiz() to start the quiz.
        resetQuiz();
    }

    /**
     * Sets up and starts a new quiz.
     */
    public void resetQuiz() {

        // DONE: Reset the number of correct guesses made
        mCorrectGuesses = 0;
        // DONE: Reset the total number of guesses the user made
        mTotalGuesses = 0;
        // DONE: Clear list of quiz countries (for prior games played)
        mQuizCountriesList.clear();

        // DONE: Randomly add FLAGS_IN_QUIZ (10) countries from the mAllCountriesList into the mQuizCountriesList
        int size = mAllCountriesList.size();
        int randomPosition;
        Country randomCountry;
        while (mQuizCountriesList.size() <= FLAGS_IN_QUIZ) {
            Log.i(TAG, "Entering resetQuiz while loop. Looking for region tag " + mRegion);
            randomPosition = rng.nextInt(size);
            randomCountry = mAllCountriesList.get(randomPosition);
            Log.i(TAG, "Found region tag " + randomCountry.getRegion());
            // DONE: Ensure no duplicate countries (e.g. don't add a country if it's already in mQuizCountriesList)
            // Check for duplicates (contains)
            // If quiz list doesn't contain random country AND (region matches restriction OR has none)...
            if (!mQuizCountriesList.contains(randomCountry) &&
                    (mRegion.equals("All") || mRegion.equals(randomCountry.getRegion()))) {
                // Add it!
                mQuizCountriesList.add(randomCountry);
            }
        }

        // DONE: Start the quiz by calling loadNextFlag
        loadNextFlag();
    }

    /**
     * Method initiates the process of loading the next flag for the quiz, showing
     * the flag's image and then 8 buttons, one of which contains the correct answer.
     */
    private void loadNextFlag() {
        // DONE: Initialize the mCorrectCountry by removing the item at position 0 in the mQuizCountries
        mCorrectCountry = mQuizCountriesList.get(0);
        mQuizCountriesList.remove(0);
        // DONE: Clear the mAnswerTextView so that it doesn't show text from the previous question
        mAnswerTextView.setText("");
        // DONE: Display current question number in the mQuestionNumberTextView
        mQuestionNumberTextView.setText(getString(R.string.question, mCorrectGuesses + 1, FLAGS_IN_QUIZ));

        // DONE: Use AssetManager to load next image from assets folder
        AssetManager am = getAssets();

        try {
            InputStream stream = am.open(mCorrectCountry.getFileName());
            Drawable image = Drawable.createFromStream(stream, mCorrectCountry.getName());
            mFlagImageView.setImageDrawable(image);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        // DONE: Get an InputStream to the asset representing the next flag
        // DONE: and try to use the InputStream to create a Drawable
        // DONE: The file name can be retrieved from the correct country's file name.
        // DONE: Set the image drawable to the correct flag.

        // DONE: Shuffle the order of all the countries (use Collections.shuffle)
        do {
            Collections.shuffle(mAllCountriesList);
        }
        while (mAllCountriesList.subList(0, mChoices).contains(mCorrectCountry));

        // DONE: Loop through n buttons to be enabled, enable them and set them to the first n countries
        for (int i = 0; i < mChoices; i++) {
            mButtons[i].setEnabled(true);
            // DONE: in the all countries list
            mButtons[i].setText(mAllCountriesList.get(i).getName());
        }

        // DONE: After the loop, randomly replace one of the active buttons with the name of the correct country
        mButtons[rng.nextInt(mChoices)].setText(mCorrectCountry.getName());

    }

    /**
     * Handles the click event of one of the 4 buttons indicating the guess of a country's name
     * to match the flag image displayed.  If the guess is correct, the country's name (in GREEN) will be shown,
     * followed by a slight delay of 2 seconds, then the next flag will be loaded.  Otherwise, the
     * word "Incorrect Guess" will be shown in RED and the button will be disabled.
     * @param v
     */
    public void makeGuess(View v) {

        mTotalGuesses++;
        // DONE: Downcast the View v into a Button (since it's one of the 4 buttons)
        Button clickedButton = (Button) v;

        // DONE: Get the country's name from the text of the button
        String guessedName = clickedButton.getText().toString();

        // DONE: If the guess matches the correct country's name, increment the number of correct guesses,
        // If answer is correct
        if (guessedName.equalsIgnoreCase(mCorrectCountry.getName())) {

            mCorrectGuesses++;
            // If quiz is not over
            if (mCorrectGuesses < FLAGS_IN_QUIZ) {

                // DONE: then display correct answer in green text.  Also, disable all 4 buttons (can't keep guessing once it's correct)
                for (int i = 0; i < mButtons.length; i++) {
                    mButtons[i].setEnabled(false);

                    // Change answer text to correct answer
                    mAnswerTextView.setText(mCorrectCountry.getName());
                    mAnswerTextView.setTextColor(getResources().getColor(R.color.correct_answer));
                }
                // Pause before going to next flag
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextFlag();
                    }
                }, 2000);
            }
            // If quiz is over
            else {
                // DONE: Nested in this decision, if the user has completed all 10 questions, show an AlertDialog
                // Create AlertDialog with text and button to reset quiz
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                double percentage = (double) mCorrectGuesses / mTotalGuesses * 100.0;
                builder.setMessage(getString(R.string.results, mTotalGuesses, percentage));
                builder.setPositiveButton(getString(R.string.reset_quiz), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetQuiz();
                    }
                });
                // Disable the cancel operation (can't cancel dialog)
                builder.setCancelable(false);
                builder.create();
                builder.show();
                // DONE: with the statistics and an option to Reset Quiz
            }
        }
        // If answer is incorrect
        else {
            // DONE: Else, the answer is incorrect, so display "Incorrect Guess!" in red
            mAnswerTextView.setText(getString(R.string.incorrect_answer));
            mAnswerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));
            // DONE: and disable just the incorrect button.
            clickedButton.setEnabled(false);
        }
    }

    // Change the region, replaces underscore with space to match Model values
    public void updateRegion(String region) {
        mRegion = region.replaceAll("_", " ");
    }

    // Changes the number of choices visible to the user
    public void updateChoices() {

        // Loop through buttons, set buttons higher than number of choices to invisible
        for (int i = 0; i < mButtons.length; i++) {
            mButtons[i].setVisibility((i < mChoices) ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
        return super.onOptionsItemSelected(item);
    }

}
