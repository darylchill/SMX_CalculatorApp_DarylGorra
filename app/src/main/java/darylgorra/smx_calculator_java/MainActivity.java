package darylgorra.smx_calculator_java;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private TextView txtNumberOp;
    private Button btnAdd,btnSubtract,btnMultiply,btnDivide, btnEquals,btnClear;
    private Button btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btn0;

    private StringBuilder expression = new StringBuilder();
    private boolean isNewInput = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Functions needed
        initViews();
        setNumberClickListeners();
        setOperatorClickListeners();
        setSpecialClickListeners();
    }

    void initViews(){
        // Initialize views from layout
        txtNumberOp = findViewById(R.id.txtNumberOp);
        btnClear = findViewById(R.id.btnClear);
        btnAdd = findViewById(R.id.btnAdd);
        btnSubtract = findViewById(R.id.btnSubtract);
        btnMultiply = findViewById(R.id.btnMultiply);
        btnDivide= findViewById(R.id.btnDivide);
        btnEquals = findViewById(R.id.btnEquals);

        btn0 = findViewById(R.id.btn0);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
    }

    // Set click listeners for number buttons
    private void setNumberClickListeners() {
        Button[] numbers = { btn0, btn1, btn2, btn3,btn4, btn5, btn6, btn7, btn8, btn9 };

        View.OnClickListener numbersClickListener = v -> {
            Button btn = (Button) v;
            //Letting the input to wait for first number
            if (isNewInput) {
                expression.setLength(0);
                isNewInput = false;
            }

            //Saving the numbers
            expression.append(btn.getText().toString());

            //Displaying the numbers
            txtNumberOp.setText(expression.toString());
        };

        //Loop the listener to all buttonsNumbers
        for (Button numberButton : numbers) {
            numberButton.setOnClickListener(numbersClickListener);
        }

    }

    // Set click listeners for operator buttons
    private void setOperatorClickListeners() {
        Button[] operators = { btnAdd, btnSubtract, btnMultiply, btnDivide};

        View.OnClickListener operatorClickListener = v -> {
            Button btn = (Button) v;
            //Checking if the last character is not an operator
            if (expression.length() > 0 && !isOperator(expression.charAt(expression.length() - 1))) {
                expression.append(btn.getText().toString()); // Append operator
                txtNumberOp.setText(expression.toString());
                isNewInput = false;
            }
        };

        for (Button operatorButton : operators) {
            operatorButton.setOnClickListener(operatorClickListener);
        }
    }


    // Set click listeners for special buttons (clear, equals)
    private void setSpecialClickListeners() {
        btnClear.setOnClickListener(v -> {
            expression.setLength(0);
            txtNumberOp.setText("0");
            isNewInput = true;
        });

        // Set click listener for equals button
       btnEquals.setOnClickListener(v -> {
            if (expression.length() > 0) {
                try {
                    // Evaluate the expression
                    double result = evaluateExpression(expression.toString());
                    txtNumberOp.setText(String.valueOf(result));
                    expression.setLength(0); // Clear expression
                    isNewInput = true;
                } catch (Exception e) {
                    txtNumberOp.setText(R.string.error);
                }
            }
        });
    }


    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '×' || c == '÷';
    }

    private double evaluateExpression(String exp) {
        // Convert to valid Java syntax
        exp = exp.replace("×", "*").replace("÷", "/"); // Convert to valid Java syntax
        return evaluate(exp);
    }

    private double evaluate(String expression) {
        //https://github.com/brettshollenberger/shunting-yard-algorithm/blob/master/README.md
        //Stack-based approach because it allows us to properly evaluate arithmetic expressions while following operator precedence.
      /*  How It Works (Step-by-Step)
        Let's evaluate the expression: 3 + 5 × 2
        Push numbers into the numbers stack: [3]
        Encounter +, push it into the operators stack: ['+']
        Push 5 into the numbers stack: [3, 5]
        Encounter ×, push it into the operators stack: ['+', '×']
        Push 2 into the numbers stack: [3, 5, 2]
        Now resolve the stacks:
        × has higher precedence than +, so evaluate 5 × 2 = 10
        Replace 5 and 2 in numbers stack → [3, 10]
        Apply +: 3 + 10 = 13
        Final result = 13*/


        Stack<Double> numbers = new Stack<>();
        Stack<Character> operations = new Stack<>();

        StringBuilder numBuffer = new StringBuilder();

        // Iterate through each character in the expression string
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            // If the character is a digit or a decimal point, append it to numBuffer (handling multi-digit numbers and decimals)
            if (Character.isDigit(ch) || ch == '.') {
                numBuffer.append(ch);
            } else {
                // When an operator is encountered, push the accumulated number into the numbers stack
                numbers.push(Double.parseDouble(numBuffer.toString()));
                numBuffer.setLength(0); // Reset numBuffer for the next number

                // Process any existing operators in the stack that have higher or equal precedence
                while (!operations.isEmpty() && precedence(operations.peek()) >= precedence(ch)) {
                    // Pop the top operator and apply it to the last two numbers in the stack
                    numbers.push(applyOperation(operations.pop(), numbers.pop(), numbers.pop()));
                }

                // Push the current operator onto the stack
                operations.push(ch);
            }
        }

        // Push the last remaining number into the numbers stack
        numbers.push(Double.parseDouble(numBuffer.toString()));

        // Process the remaining operators in the stack
        while (!operations.isEmpty()) {
            numbers.push(applyOperation(operations.pop(), numbers.pop(), numbers.pop()));
        }

        // Return the final computed result
        return numbers.pop();

    }

    private int precedence(char op) {
        //PEMDAS
        if (op == '+' || op == '-') return 1; // Low precedence (evaluated last)
        if (op == '*' || op == '/') return 2; // High precedence (evaluated first)
        return 0; // Default (invalid operator case)
    }

    private double applyOperation(char op, double b, double a) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return (b == 0) ? 0 : a / b;
        }
        return 0;
    }

}