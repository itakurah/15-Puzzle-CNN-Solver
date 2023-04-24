package com.eml.fifteensolver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import solver.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ImageProcessActivity extends AppCompatActivity {

    private static TextView modelDigit;
    private static TextView parsedDigit;
    private static TextView solution;
    private static TextView depth;
    private static Button btn1;
    private static Button btn2;
    private static Button btn3;
    private static Button btn4;
    private static Button btn5;
    private static Button btn6;
    private static Button btn7;
    private static Button btn8;
    private static Button btn9;
    private static Button btn10;
    private static Button btn11;
    private static Button btn12;
    private static Button btn13;
    private static Button btn14;
    private static Button btn15;
    private static Button btn16;
    private List<String> digits;
    private Button abort;
    private Button solve;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        HashMap<String, List<String>> hashMap = (HashMap<String, List<String>>) intent.getSerializableExtra("hashmap");
        digits = new ArrayList<>(Objects.requireNonNull(hashMap.get("parsed")));
        System.out.println(digits.size());

        Log.v("HashMapTest", hashMap.get("raw").toString());
        Log.v("HashMapTest", hashMap.get("parsed").toString());
        Log.v("HashMapTest", hashMap.get("idx").toString());
        System.out.println(digits);
        System.out.println(hashMap.get("idx").get(0));
        System.out.println("#####");
        digits.add(Integer.parseInt(Objects.requireNonNull(hashMap.get("idx")).get(0)), "0");
        String resultModel = String.join(",", hashMap.get("raw"));
        String resultParsed = String.join(",", hashMap.get("parsed"));

        setContentView(R.layout.image_process);
        modelDigit = findViewById(R.id.textView6);
        parsedDigit = findViewById(R.id.textView8);
        solution = findViewById(R.id.textView11);
        depth = findViewById(R.id.textView13);


//        System.out.println(resultModel);
//        System.out.println(resultParsed);
        modelDigit.setText(String.format("%s", resultModel));
        parsedDigit.setText(String.format("%s", resultParsed));
        abort = findViewById(R.id.button4);
        abort.setOnClickListener(v -> reset());
        solve = findViewById(R.id.button5);
        solve.setOnClickListener(v -> solve(digits));
        fillButtons();

        btn1.setOnClickListener(v -> showInput(btn1, 0));
        btn2.setOnClickListener(v -> showInput(btn2, 1));
        btn3.setOnClickListener(v -> showInput(btn3, 2));
        btn4.setOnClickListener(v -> showInput(btn4, 3));
        btn5.setOnClickListener(v -> showInput(btn5, 4));
        btn6.setOnClickListener(v -> showInput(btn6, 5));
        btn7.setOnClickListener(v -> showInput(btn7, 6));
        btn8.setOnClickListener(v -> showInput(btn8, 7));
        btn9.setOnClickListener(v -> showInput(btn9, 8));
        btn10.setOnClickListener(v -> showInput(btn10, 9));
        btn11.setOnClickListener(v -> showInput(btn11, 10));
        btn12.setOnClickListener(v -> showInput(btn12, 11));
        btn13.setOnClickListener(v -> showInput(btn13, 12));
        btn14.setOnClickListener(v -> showInput(btn14, 13));
        btn15.setOnClickListener(v -> showInput(btn15, 14));
        btn16.setOnClickListener(v -> showInput(btn16, 15));
    }

    private void reset() {
        Intent main = new Intent(getApplicationContext(), MainActivity.class);
        ImageProcessActivity.this.startActivity(main);
    }

    private void showInput(Button button, int idx) {
        //create dialog box
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Enter new digit");
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);
        boolean right = false;
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String digit = input.getText().toString();
                Pattern pattern = Pattern.compile("^(?:[0-9]|10|11|12|13|14|15)$");
                Matcher matcher = pattern.matcher(digit);
                boolean matchFound = matcher.find();
                if (!matchFound) {
                    button.setText("0");
                    digits.set(idx, String.valueOf(0));
                } else {
                    button.setText(digit);
                    digits.set(idx, digit);
                }
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        alert.show();
    }

    private void fillButtons() {
        btn1 = findViewById(R.id.button10);
        btn2 = findViewById(R.id.button11);
        btn3 = findViewById(R.id.button12);
        btn4 = findViewById(R.id.button13);
        btn5 = findViewById(R.id.button14);
        btn6 = findViewById(R.id.button15);
        btn7 = findViewById(R.id.button16);
        btn8 = findViewById(R.id.button17);
        btn9 = findViewById(R.id.button18);
        btn10 = findViewById(R.id.button19);
        btn11 = findViewById(R.id.button20);
        btn12 = findViewById(R.id.button21);
        btn13 = findViewById(R.id.button22);
        btn14 = findViewById(R.id.button23);
        btn15 = findViewById(R.id.button24);
        btn16 = findViewById(R.id.button25);
        btn1.setText(digits.get(0));
        btn2.setText(digits.get(1));
        btn3.setText(digits.get(2));
        btn4.setText(digits.get(3));
        btn5.setText(digits.get(4));
        btn6.setText(digits.get(5));
        btn7.setText(digits.get(6));
        btn8.setText(digits.get(7));
        btn9.setText(digits.get(8));
        btn10.setText(digits.get(9));
        btn11.setText(digits.get(10));
        btn12.setText(digits.get(11));
        btn13.setText(digits.get(12));
        btn14.setText(digits.get(13));
        btn15.setText(digits.get(14));
        btn16.setText(digits.get(15));
    }

    private void solve(List<String> digits) {
        int[][] gameState = new int[][]{{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
        StringBuilder puzzle = new StringBuilder();
        int count = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                gameState[i][j] = Integer.parseInt(digits.get(count));
                puzzle.append(" "+Integer.parseInt(digits.get(count)));
                count++;
            }
        }
        Board board = new Board(gameState);
        try {
            System.out.println(puzzle);
            Result result = IDAStar.solve(board, new LinearConflictWithMD(), TimeUnit.NS, DebugMode.ON);
            solution.setText(result.getMoves());
            depth.setText(String.valueOf(result.getDepth()));
        } catch (Exception e) {
            solution.setText(e.getMessage());
            depth.setText("");
            Toast.makeText(getApplicationContext(), "Puzzle is invalid!", Toast.LENGTH_LONG).show();
        }

    }


}
