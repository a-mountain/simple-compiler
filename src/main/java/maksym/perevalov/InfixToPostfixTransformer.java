package maksym.perevalov;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import maksym.perevalov.Tokenizer.Token;

public class InfixToPostfixTransformer {
    private static final int IGNORE = 0;
    private static final Map<String, Integer> PRECEDENCE = Map.of(
          "-", 1,
          "+", 1,
          "/", 2,
          "*", 2,
          "^", 4,
          "(", IGNORE,
          ")", IGNORE
    );
    private static final List<String> FUNCTIONS = List.of("sin", "cos", "pow", "sqrt", "min", "max");

    private final List<String> postfix = new ArrayList<>();
    private final Deque<Operator> operations = new ArrayDeque<>();
    private boolean insideFunction = false;
    private int bonus = 0;
    private int counter = 0;

    public List<String> transform(List<Token> tokens) {
        for (Token token : tokens) {
            if (isNumber(token) || isVariable(token)) {
                addToPostfixExpression(token);
                continue;
            }

            if (insideFunction && isOpenBracket(token)) {
                counter++;
                bonus += 10;
            }

            if (insideFunction && isClosedBracket(token)) {
                counter--;
                bonus -= 10;

                if (counter == 0) {
                    insideFunction = false;
                }
            }

            if (token.type().equals(Tokenizer.TokenType.Comma) && !isBracket(operations.peek())) {
                addToPostfixExpression(takeSavedLatestOperator());
            }

            if (isOperator(token) || isFunction(token)) {
                while (latestSavedOperatorHasHigherPrecedence(token)) {
                    addToPostfixExpression(takeSavedLatestOperator());
                }
                saveOperator(new Operator(token.value(), getPrecedence(token)));
                if (isFunction(token)) {
                    insideFunction = true;
                }
                continue;
            }

            if (isOpenBracket(token)) {
                saveOperator(new Operator(token.value(), 0));
                continue;
            }

            if (isClosedBracket(token)) {
                while (latestSavedOperatorIsNotOpenBracket()) {
                    addToPostfixExpression(takeSavedLatestOperator());
                }
                takeSavedLatestOperator();
            }

        }

        while (!operations.isEmpty()) {
            addToPostfixExpression(takeSavedLatestOperator());
        }

        return postfix;
    }

    private boolean isBracket(Operator token) {
        return isClosedBracket(token) || isOpenBracket(token);
    }


    private int getPrecedence(Token token) {
        if (isFunction(token)) return 3 + bonus;
        return PRECEDENCE.get(token.value()) + bonus;
    }

    private boolean isClosedBracket(Token token) {
        return token.value().equals(")");
    }

    private boolean isClosedBracket(Operator token) {
        return token.value().equals(")");
    }


    private boolean isOpenBracket(Token token) {
        return token.value().equals("(");
    }

    private boolean isOpenBracket(Operator token) {
        return token.value().equals("(");
    }

    private Operator takeSavedLatestOperator() {
        return operations.pop();
    }

    private boolean latestSavedOperatorIsNotOpenBracket() {
        return !operations.isEmpty() && !isOpenBracket(operations.peek());
    }

    private void saveOperator(Operator token) {
        operations.push(token);
    }

    private boolean latestSavedOperatorHasHigherPrecedence(Token token) {
        return !operations.isEmpty() && operations.peek().precedence() >= getPrecedence(token);
    }

    private void addToPostfixExpression(Token token) {
        postfix.add(token.value());
    }

    private void addToPostfixExpression(Operator operator) {
        postfix.add(operator.value());
    }

    private boolean isOperator(Token token) {
        return token.type().equals(Tokenizer.TokenType.Operator);
    }

    private boolean isNumber(Token token) {
        return token.type().equals(Tokenizer.TokenType.Number);
    }

    private boolean isVariable(Token token) {
        return token.type().equals(Tokenizer.TokenType.Identifier) && !isFunction(token);
    }

    private boolean isFunction(Token token) {
        return token.type().equals(Tokenizer.TokenType.Identifier) && FUNCTIONS.contains(token.value());
    }

    record Operator(String value, int precedence) {
    }

}
