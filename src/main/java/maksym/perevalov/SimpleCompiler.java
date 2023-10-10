package maksym.perevalov;

import java.util.ArrayList;
import java.util.List;

import picocli.CommandLine.Command;

@Command(name = "checksum")
public class Main {
    private static final List<Character> OPERATIONS = List.of('-', '+', '*', '/');

    // number, operation, start, end, id
    public static void main(String[] args) {
        var expression = "(a+b)/cos(30)";

        var tokenizer = new Tokenizer(expression.toCharArray());
        var tokens = tokenizer.readAll();
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    sealed interface Token {
        record Start() implements Token {}
        record End() implements Token {}
        record BeginBracket() implements Token {}
        record EndBracket() implements Token {}
        record Operation(String value) implements Token {}
        record Number(String value) implements Token {}
        record Id(String value) implements Token {}
    }

    static class Tokenizer {
        private final char[] content;
        private final int lastIndex;
        private int currentIndex;

        public Tokenizer(char[] input) {
            content = input;
            lastIndex = input.length -1;
            currentIndex = -1;
        }

        public Token nextToken() {
            if (currentIndex == -1) {
                currentIndex++;
                return new Token.Start();
            }
            if (currentIndex > lastIndex) {
                return new Token.End();
            }

            Character character = next();

            return switch (character) {
                case '(' -> new Token.BeginBracket();
                case ')' -> new Token.EndBracket();
                case Character c when OPERATIONS.contains(c) -> new Token.Operation(c.toString());
                case Character c when Character.isDigit(c) -> nextNumber(c);
                case Character c when Character.isAlphabetic(c) -> nextId(c);
                default -> throw new RuntimeException("Illegal token");
            };
        }

        public List<Token> readAll() {
            var tokens = new ArrayList<Token>();
            var token = nextToken();
            while (!(token instanceof Token.End)) {
                tokens.add(token);
                token = nextToken();
            }
            tokens.add(new Token.End());
            return tokens;
        }

        private Token.Number nextNumber(Character firstDigit) {
            String number = String.valueOf(firstDigit);
            while (hasNext()) {
                var next = next();
                if (Character.isDigit(next)) {
                    number = number + next;
                } else {
                    currentIndex--;
                    return new Token.Number(number);
                }
            }
            return new Token.Number(number);
        }

        private Token.Id nextId(Character firstDigit) {
            String number = String.valueOf(firstDigit);
            while (hasNext()) {
                var next = next();
                if (Character.isAlphabetic(next)) {
                    number = number + next;
                } else {
                    currentIndex--;
                    return new Token.Id(number);
                }
            }
            return new Token.Id(number);
        }

        private char next() {
            return content[currentIndex++];
        }


        private boolean hasNext() {
            return currentIndex <= lastIndex;
        }
    }
}
