package com.example.penaflorida_majoractivity4;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private TextView tvStudentInfo, tvScore, tvRound, tvResult, tvBerries, tvDifficulty, tvHighScore;
    private ImageView ivPokemon;
    private ProgressBar progressBar;
    private Button[] btnOptions = new Button[4];
    private Button btnNext, btnHint;

    private PokeApiService apiService;
    private String correctName;
    private int score = 0;
    private int highScore = 0;
    private int currentRound = 0;
    private int berries = 3;
    private final Random random = new Random();
    
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "PokemonQuizPrefs";
    private static final String KEY_HIGH_SCORE = "highScore";
    
    private boolean isGameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        highScore = sharedPreferences.getInt(KEY_HIGH_SCORE, 0);

        initViews();
        setupRetrofit();
        startNewGame();
    }

    private void initViews() {
        tvStudentInfo = findViewById(R.id.tvStudentInfo);
        tvScore = findViewById(R.id.tvScore);
        tvHighScore = findViewById(R.id.tvHighScore);
        tvRound = findViewById(R.id.tvRound);
        tvResult = findViewById(R.id.tvResult);
        tvBerries = findViewById(R.id.tvBerries);
        tvDifficulty = findViewById(R.id.tvDifficulty);
        ivPokemon = findViewById(R.id.ivPokemon);
        progressBar = findViewById(R.id.progressBar);
        btnNext = findViewById(R.id.btnNext);
        btnHint = findViewById(R.id.btnHint);

        btnOptions[0] = findViewById(R.id.btnOption1);
        btnOptions[1] = findViewById(R.id.btnOption2);
        btnOptions[2] = findViewById(R.id.btnOption3);
        btnOptions[3] = findViewById(R.id.btnOption4);

        tvStudentInfo.setText("Penaflorida | Major Activity 4");

        for (Button btn : btnOptions) {
            btn.setOnClickListener(v -> checkAnswer(btn.getText().toString()));
        }

        btnNext.setOnClickListener(v -> {
            if (isGameOver) {
                startNewGame();
            } else {
                loadRandomPokemon();
            }
        });

        btnHint.setOnClickListener(v -> useBerry());
        
        updateHighScoreDisplay();
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(PokeApiService.class);
    }

    private void startNewGame() {
        score = 0;
        currentRound = 0;
        berries = 3;
        isGameOver = false;
        updateStats();
        btnNext.setText("Next Pokémon");
        loadRandomPokemon();
    }

    private void updateStats() {
        tvScore.setText("SCORE: " + score);
        tvBerries.setText("Berries: " + berries);
        
        if (score > highScore) {
            highScore = score;
            saveHighScore();
            updateHighScoreDisplay();
        }
    }
    
    private void saveHighScore() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_HIGH_SCORE, highScore);
        editor.apply();
    }
    
    private void updateHighScoreDisplay() {
        if (tvHighScore != null) {
            tvHighScore.setText("HIGH SCORE: " + highScore);
        }
    }

    private void loadRandomPokemon() {
        currentRound++;
        tvRound.setText("ROUND " + currentRound);
        tvResult.setText("");
        btnNext.setVisibility(View.INVISIBLE);
        btnHint.setEnabled(false);
        setButtonsEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        ivPokemon.setImageDrawable(null);
        ivPokemon.setColorFilter(Color.BLACK);

        String difficulty;
        int maxId;
        if (currentRound <= 1) {
            difficulty = "Difficulty: Easy";
            maxId = 50;
        } else if (currentRound <= 3) {
            difficulty = "Difficulty: Medium";
            maxId = 100;
        } else {
            difficulty = "Difficulty: Hard";
            maxId = 151;
        }
        tvDifficulty.setText(difficulty);

        int randomId = random.nextInt(maxId) + 1;

        apiService.getPokemon(randomId).enqueue(new Callback<PokemonResponse>() {
            @Override
            public void onResponse(Call<PokemonResponse> call, Response<PokemonResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    displayPokemon(response.body());
                } else {
                    handleError("Failed to load Pokémon data");
                }
            }

            @Override
            public void onFailure(Call<PokemonResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                handleError("Check your internet connection");
            }
        });
    }

    private void displayPokemon(PokemonResponse pokemon) {
        correctName = pokemon.getName();
        
        String imageUrl = null;
        if (pokemon.getSprites() != null) {
            if (pokemon.getSprites().getOther() != null && 
                pokemon.getSprites().getOther().getOfficialArtwork() != null) {
                imageUrl = pokemon.getSprites().getOther().getOfficialArtwork().getFrontDefault();
            }
            if (imageUrl == null) {
                imageUrl = pokemon.getSprites().getFrontDefault();
            }
        }

        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(ivPokemon);
        }

        setupOptions();
        setButtonsEnabled(true);
        btnHint.setEnabled(berries > 0);
    }

    private void setupOptions() {
        List<String> options = new ArrayList<>();
        options.add(correctName);

        int numOptions = currentRound <= 1 ? 2 : (currentRound <= 3 ? 3 : 4);

        while (options.size() < numOptions) {
            String randomName = PokemonData.POKEMON_NAMES[random.nextInt(PokemonData.POKEMON_NAMES.length)];
            if (!options.contains(randomName)) {
                options.add(randomName);
            }
        }

        Collections.shuffle(options);

        for (int i = 0; i < 4; i++) {
            if (i < options.size()) {
                btnOptions[i].setText(options.get(i));
                btnOptions[i].setVisibility(View.VISIBLE);
            } else {
                btnOptions[i].setVisibility(View.GONE);
            }
        }
    }

    private void useBerry() {
        if (berries > 0) {
            List<Button> wrongButtons = new ArrayList<>();
            for (Button btn : btnOptions) {
                if (btn.getVisibility() == View.VISIBLE && btn.isEnabled() && 
                    !btn.getText().toString().equalsIgnoreCase(correctName)) {
                    wrongButtons.add(btn);
                }
            }

            if (!wrongButtons.isEmpty()) {
                berries--;
                updateStats();
                int indexToRemove = random.nextInt(wrongButtons.size());
                wrongButtons.get(indexToRemove).setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Razz Berry used! One wrong answer removed.", Toast.LENGTH_SHORT).show();
            }
            
            if (berries == 0) btnHint.setEnabled(false);
        }
    }

    private void checkAnswer(String selectedName) {
        setButtonsEnabled(false);
        btnHint.setEnabled(false);
        ivPokemon.clearColorFilter();

        if (selectedName.equalsIgnoreCase(correctName)) {
            score += 10;
            tvResult.setText("Gotcha! " + correctName.toUpperCase() + " was caught!");
            tvResult.setTextColor(Color.parseColor("#388E3C"));
            if (random.nextFloat() > 0.7) {
                berries++;
                Toast.makeText(this, "You found a Berry!", Toast.LENGTH_SHORT).show();
            }
            btnNext.setText("Next Pokémon");
            isGameOver = false;
        } else {
            tvResult.setText("Oh no! It's " + correctName.toUpperCase());
            tvResult.setTextColor(Color.RED);
            btnNext.setText("Try Again");
            isGameOver = true;
            Toast.makeText(this, "Game Over! Score: " + score, Toast.LENGTH_LONG).show();
        }

        updateStats();
        btnNext.setVisibility(View.VISIBLE);
    }

    private void setButtonsEnabled(boolean enabled) {
        for (Button btn : btnOptions) {
            btn.setEnabled(enabled);
        }
    }

    private void handleError(String message) {
        tvResult.setText(message);
        tvResult.setTextColor(Color.RED);
        btnNext.setVisibility(View.VISIBLE);
        btnNext.setText("Retry");
        isGameOver = true; // Retry acts as a reset
    }
}
