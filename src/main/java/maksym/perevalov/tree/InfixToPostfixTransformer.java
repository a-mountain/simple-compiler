package maksym.perevalov.tree;

import static maksym.perevalov.parser.SyntaxParser.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import maksym.perevalov.parser.SyntaxParser.SyntaxToken;

public class InfixToPostfixTransformer {
    private static final Map<String, Integer> PRECEDENCE = Map.of(
          "-", 1,
          "+", 1,
          "/", 2,
          "*", 2,
          "^", 4,
          "(", 0,
          ")", 0
    );

    private final List<MathElement> postfix = new ArrayList<>();
    private final Deque<Operator> operations = new ArrayDeque<>();
    private int bonus = 0;

    public List<MathElement> transform(List<SyntaxToken> tokens) {
        for (var token : tokens) {
            if (token.is(SyntaxTokenType.Number) || token.is(SyntaxTokenType.Variable)) {
                addToPostfixExpression(token);
                continue;
            }

            if (token.is(SyntaxTokenType.OpenFunctionBracket)) {
                bonus += 10;
            }

            if (token.is(SyntaxTokenType.ClosedFunctionBracket)) {
                bonus -= 10;
            }

            if (token.is(SyntaxTokenType.Comma) && !operations.peek().isBracket()) {
                addToPostfixExpression(takeSavedLatestOperator());
            }

            if (token.is(SyntaxTokenType.Operator) || token.is(SyntaxTokenType.Function)) {
                while (latestSavedOperatorHasHigherPrecedence(token)) {
                    addToPostfixExpression(takeSavedLatestOperator());
                }
                saveOperator(new Operator(token.value(), getPrecedence(token)));
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
               addToPostfixExpression(takeSavedLatestOperator());
            }

        }

        while (!operations.isEmpty()) {
            addToPostfixExpression(takeSavedLatestOperator());
        }

        return postfix;
    }

    private int getPrecedence(SyntaxToken token) {
        if (token.is(SyntaxTokenType.Function)) return 3 + bonus;
        return PRECEDENCE.get(token.value()) + bonus;
    }

    private boolean isClosedBracket(SyntaxToken token) {
        return token.value().equals(")");
    }

    private boolean isOpenBracket(SyntaxToken token) {
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

    private boolean latestSavedOperatorHasHigherPrecedence(SyntaxToken token) {
        return !operations.isEmpty() && operations.peek().precedence() >= getPrecedence(token);
    }

    private void addToPostfixExpression(SyntaxToken token) {
        if (token.is(SyntaxTokenType.Function)) {
            postfix.add(new MathElement.Function(token.value()));
        } else if (token.is(SyntaxTokenType.Number)) {
            postfix.add(new MathElement.MNumber(token.value()));
        } else if (token.is(SyntaxTokenType.Variable)) {
            postfix.add(new MathElement.Varaible(token.value()));
        } else {
            throw new RuntimeException("Unexpected token - " + token);
        }
    }

    private void addToPostfixExpression(Operator operator) {
        var element = switch (operator.value()) {
            case "+" -> new MathElement.Plus();
            case "-" -> new MathElement.Minus();
            case "/" -> new MathElement.Divide();
            case "*" -> new MathElement.Multiply();
            default -> new MathElement.Function(operator.value());
        };
        postfix.add(element);
    }

    record Operator(String value, int precedence) {
        public boolean isBracket() {
            return value.equals("(") || value.equals(")");
        }
    }
}
