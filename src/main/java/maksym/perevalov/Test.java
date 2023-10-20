package maksym.perevalov;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

public class Test {
    private static int precedence(char operator) {
        if (operator == '+' || operator == '-') {
            return 1;
        } else if (operator == '*' || operator == '/') {
            return 2;
        }
        return 0;
    }

    // Define supported functions and their corresponding implementations
    private static final Map<String, Function<Double, Double>> functions = new HashMap<>();
    static {
        functions.put("sin", Math::sin);
        functions.put("cos", Math::cos);
    }

    public static void main(String[] args) {
        String expr = "(1 + 2) * -3 + 4".replace(" ", "");
        var result = infixToPostfix(expr);
        System.out.println("result = " + result);
    }

    // Convert an infix expression to postfix notation (RPN)
    private static String infixToPostfix(String infixExpression) {
        StringBuilder postfix = new StringBuilder();
        Stack<Character> operatorStack = new Stack<>();

        for (char c : infixExpression.toCharArray()) {
            if (Character.isDigit(c)) {
                postfix.append(c);
            } else if (c == '(') {
                operatorStack.push(c);
            } else if (c == ')') {
                while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                    postfix.append(operatorStack.pop());
                }
                if (operatorStack.isEmpty()) {
                    return "";
                }
                operatorStack.pop(); // Pop the '('
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!operatorStack.isEmpty() && precedence(c) <= precedence(operatorStack.peek())) {
                    postfix.append(operatorStack.pop());
                }
                operatorStack.push(c);
            } else if (Character.isLetter(c)) {
                // Handle function calls
                StringBuilder functionName = new StringBuilder();
                functionName.append(c);
                while ((c = getNextChar(infixExpression)) != 0 && Character.isLetterOrDigit(c)) {
                    functionName.append(c);
                }
                String funcName = functionName.toString();
                Function<Double, Double> func = functions.get(funcName);
                if (func != null) {
                    operatorStack.push('(');
                    postfix.append(funcName);
                } else {
                    return "";
                }
            } else {
                return "";
            }
        }

        while (!operatorStack.isEmpty()) {
            char top = operatorStack.pop();
            if (top == '(') {
                return "";
            }
            postfix.append(top);
        }

        return postfix.toString();
    }

    // Helper method to get the next character in the expression
    private static char getNextChar(String expression) {
        if (expression.isEmpty()) {
            return 0;
        }
        return expression.charAt(0);
    }
}
