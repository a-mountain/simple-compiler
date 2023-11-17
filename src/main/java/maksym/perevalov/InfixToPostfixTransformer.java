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
          "sin", 3,
          "cos", 3,
          "^", 4,
          "(", IGNORE,
          ")", IGNORE
    );
    private static final List<String> FUNCTIONS = List.of("sin", "cos");

    private final List<Token> postfix = new ArrayList<>();
    private final Deque<Token> operations = new ArrayDeque<>();

    public List<Token> transform(List<Token> tokens) {
        for (Token token : tokens) {

            if (isNumber(token) || isVariable(token)) {
                addToPostfixExpression(token);
                continue;
            }

            if (isOperator(token) || isFunction(token)) {
                while (latestSavedOperatorHasHigherPrecedence(token)) {
                    addToPostfixExpression(takeSavedLatestOperator());
                }
                saveOperator(token);
                continue;
            }

            if (token.value().equals("(")) {
                saveOperator(token);
                continue;
            }

            if (token.value().equals(")")) {
                while (latestSavedOperatorIsOpenBracket()) {
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

    private Token takeSavedLatestOperator() {
        return operations.pop();
    }

    private boolean latestSavedOperatorIsOpenBracket() {
        return !operations.isEmpty() && !operations.peek().value().equals("(");
    }

    private void saveOperator(Token token) {
        operations.push(token);
    }

    private boolean latestSavedOperatorHasHigherPrecedence(Token token) {
        return !operations.isEmpty() && PRECEDENCE.get(operations.peek().value()) > PRECEDENCE.get(token.value());
    }

    private void addToPostfixExpression(Token token) {
        postfix.add(token);
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
}
