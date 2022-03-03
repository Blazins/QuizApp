package com.example.trivia;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.trivia.Data.Repository;
import com.example.trivia.databinding.ActivityMainBinding;
import com.example.trivia.model.Question;
import com.example.trivia.model.Score;
import com.example.trivia.util.Prefs;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  {
    private ActivityMainBinding binding;
    private int currentQuestionIndex = 0;
    List<Question> questionList;
    private int scoreCounter = 0;
    private Score score;
    private Prefs prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        score = new Score();
        binding.scoreText.setText(String.format("Current Score: %s", score.getScore()));
        prefs = new Prefs(MainActivity.this);
       // Log.d("app","High_score: "+prefs.getHighestScore());
        currentQuestionIndex = prefs.getState();
        binding.highestScoreText.setText(String.format("Highest %s", prefs.getHighestScore()));
        //Retrieve the last state


        questionList = new Repository().getQuestions(questionArrayList ->{

            binding.questionTextview.setText( questionArrayList.get(currentQuestionIndex).getAnswer());
            updateCounter(questionArrayList);

        });

        binding.buttonNext.setOnClickListener(v -> {
            getNextQuestion();
//                prefs.saveHighestScore(scoreCounter);
//                Log.d("High","High_score: "+prefs.getHighestScore());
        });
        binding.buttonTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
                updateQuestion();
            }
        });
        binding.buttonFalse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
                updateQuestion();
            }
        });


    }

    /**
     *
     */
    private void getNextQuestion() {
        currentQuestionIndex =(currentQuestionIndex +1) % questionList.size();
        updateQuestion();
    }

    private void updateCounter(java.util.ArrayList<Question> questionArrayList) {
        binding.textViewOutOf.setText(String.format(getString(R.string.question_text_format), currentQuestionIndex, questionArrayList.size()));
    }


    private void checkAnswer(boolean userChoice) {
        boolean answer = questionList.get(currentQuestionIndex).getAnswerTrue();
        int snackMessageID =0;
        if(userChoice == answer){
            snackMessageID = R.string.correct_answer;
            fadeAnimation();
            addPoints();
        }else{
            snackMessageID = R.string.incorrect_answer;
            shakeAnimation();
            deductPoints();
        }
        Snackbar.make(binding.cardView, snackMessageID,Snackbar.LENGTH_SHORT)
                .show();

    }
    private void fadeAnimation(){
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f,0.0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatCount(Animation.REVERSE);

        binding.cardView.setAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.questionTextview.setTextColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.questionTextview.setTextColor(Color.WHITE);
                getNextQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void updateQuestion() {
        String currentQuestion = questionList.get(currentQuestionIndex).getAnswer();
        binding.questionTextview.setText(currentQuestion);
        updateCounter((ArrayList<Question>) questionList);
    }
    private void shakeAnimation(){
        Animation animation = AnimationUtils.loadAnimation(MainActivity.this,R.anim.shaky);
        binding.cardView.setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.questionTextview.setTextColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.questionTextview.setTextColor(Color.WHITE);
                getNextQuestion();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void addPoints(){
        scoreCounter+=10;
        score.setScore(scoreCounter);
        binding.scoreText.setText(String.format("Your Score is %s", score.getScore()));

    }
    private void deductPoints(){
        if(scoreCounter >0){
            scoreCounter-=10;
            score.setScore(scoreCounter);
            binding.scoreText.setText(String.format("Current Score: %s", score.getScore()));
        }else {
            scoreCounter=0;
            score.setScore(scoreCounter);
        }
    }

    @Override
    protected void onPause() {
        prefs.saveHighestScore(score.getScore());
        prefs.setState(currentQuestionIndex);
        Log.d("State", "saving State: "+prefs.getState());
        Log.d("Pause", "On Pause: "+prefs.getHighestScore());
        super.onPause();
    }
}
