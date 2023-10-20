package maksym.perevalov;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Frontend {
    sealed interface Token {

        int index();

        record StartExpression(int index) implements Frontend.Token {
        }

        record EndExpression(int index) implements Frontend.Token {
        }

        record OpenBracket(int index) implements Frontend.Token {
        }

        record ClosedBracket(int index) implements Frontend.Token {
        }

        record Comma(int index) implements Frontend.Token {
        }

        record Operator(String value, int index) implements Frontend.Token {
        }

        record Number(String value, int index) implements Frontend.Token {
        }

        record Identifier(String value, int index) implements Frontend.Token {
        }
    }

    // a + sin ( 60 )
    public static void main(String[] args) {
        String test = "(*a+b)";
        var parser = new Parser();
        var tokens = parser.parse(test);
        System.out.println("---TOKENS---");
        tokens.forEach(System.out::println);
        var analyzer = new SyntaxAnalyzer();
        var errors = analyzer.validate(tokens);
        System.out.println("---Errors---");
        if (errors.isEmpty()) {
            System.out.println("No errors :)");
        }
        errors.forEach(System.out::println);
    }

    static class SyntaxAnalyzer {
        List<String> validate(List<Token> tokens) {
            Token state = tokens.get(0);
            var errors = new ArrayList<String>();
            for (int i = 1; i < tokens.size(); i++) {
                var token = tokens.get(i);
                switch (state) {
                    case Token.StartExpression _ -> {
                        if (token instanceof Token.Number || token instanceof Token.Identifier || token instanceof Token.OpenBracket) {
                            break;
                        }
                        if (token instanceof Token.Operator op) {
                            if (Objects.equals(op.value, "-") || Objects.equals(op.value, "+")) {
                                break;
                            }
                        }
                        errors.add("Invalid token '%s' after '%s' at index '%s'".formatted(token.getClass().getSimpleName(), state.getClass().getSimpleName(), token.index()));
                    }
                    case Token.OpenBracket _ -> {
                        if (token instanceof Token.Number || token instanceof Token.Identifier) {
                            break;
                        }
                        if (token instanceof Token.Operator op) {
                            if (Objects.equals(op.value, "-") || Objects.equals(op.value, "+")) {
                                break;
                            }
                        }
                        errors.add("Invalid token '%s' after '%s' at index '%s'".formatted(token.getClass().getSimpleName(), state.getClass().getSimpleName(), token.index()));
                    }
                    case Token.ClosedBracket _ -> {
                        if (token instanceof Token.EndExpression) {
                            break;
                        }
                        if (token instanceof Token.Operator op) {
                            if (List.of("+", "-", "*", "/").contains(op.value())) {
                                break;
                            }
                        }
                        errors.add("Invalid token '%s' after '%s' at index '%s'".formatted(token.getClass().getSimpleName(), state.getClass().getSimpleName(), token.index()));
                    }
                    case Token.Number _ -> {
                        if (token instanceof Token.Operator || token instanceof Token.ClosedBracket || token instanceof Token.EndExpression) {
                            break;
                        }
                        errors.add("Invalid token '%s' after '%s' at index '%s'".formatted(token.getClass().getSimpleName(), state.getClass().getSimpleName(), token.index()));
                    }
                    case Token.Identifier _ -> {
                        if (token instanceof Token.Operator || token instanceof Token.ClosedBracket || token instanceof Token.EndExpression) {
                            break;
                        }
                        errors.add("Invalid token '%s' after '%s' at index '%s'".formatted(token.getClass().getSimpleName(), state.getClass().getSimpleName(), token.index()));
                    }
                    case Token.Operator _ -> {
                        if (token instanceof Token.Number || token instanceof Token.Identifier || token instanceof Token.OpenBracket) {
                            break;
                        }
                        errors.add("Invalid token '%s' after '%s' at index '%s'".formatted(token.getClass().getSimpleName(), state.getClass().getSimpleName(), token.index()));
                    }
                    case Token.EndExpression _, Token.Comma _ -> {}
                }
                state = token;
            }
            return errors;
        }
    }

    static class Parser {
        private static final List<Character> OPERATION = List.of('-', '+', '*', '/', '^');

        List<Token> parse(String text) {
            var chars = text.replaceAll(" ", "").toCharArray();
            var tokens = new ArrayList<Token>(List.of(new Token.StartExpression(-1)));
            int i = 0;
            while (i != chars.length) {
                char currentChar = chars[i];
                if (currentChar == '(') {
                    tokens.add(new Token.OpenBracket(i));
                    i++;
                    continue;
                }
                if (currentChar == ')') {
                    tokens.add(new Token.ClosedBracket(i));
                    i++;
                    continue;
                }
                if (currentChar == ',') {
                    tokens.add(new Token.Comma(i));
                    i++;
                    continue;
                }
                if (OPERATION.contains(currentChar)) {
                    tokens.add(new Token.Operator(String.valueOf(currentChar), i));
                    i++;
                    continue;
                }
                if (Character.isDigit(currentChar)) {
                    String number = String.valueOf(currentChar);
                    int startIndex = i;
                    while (i != chars.length - 1) {
                        currentChar = chars[++i];
                        if (Character.isDigit(currentChar)) {
                            number += String.valueOf(currentChar);
                        } else {
                            --i;
                            break;
                        }
                    }
                    i++;
                    tokens.add(new Token.Number(number, startIndex));
                    continue;
                }
                if (Character.isAlphabetic(currentChar)) {
                    String identifier = String.valueOf(currentChar);
                    int startIndex = i;
                    while (i != chars.length - 1) {
                        currentChar = chars[++i];
                        if (Character.isAlphabetic(currentChar)) {
                            identifier += String.valueOf(currentChar);
                        } else {
                            --i;
                            break;
                        }
                    }
                    i++;
                    tokens.add(new Token.Identifier(identifier, startIndex));
                    continue;
                }
                throw new RuntimeException("Unexpected symbol '%s' at index '%s'".formatted(currentChar, i));
            }
            tokens.add(new Token.EndExpression(chars.length));
            return tokens;
        }
    }
}
