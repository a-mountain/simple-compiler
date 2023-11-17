package maksym.perevalov;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    private static final String PATTERN = "(%s)|(%s)|(%s)|(%s)|(%s)|(%s)".formatted(
          TokenType.Number.pattern, TokenType.Identifier.pattern, TokenType.Comma.pattern,
          TokenType.ClosedBracket.pattern, TokenType.OpenBracket.pattern, TokenType.Operator.pattern
    );

    public List<Token> tokenize(String input) {
        var tokens = new ArrayList<Token>();
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            var position = matcher.start();
            var value = matcher.group();
            var type = getType(value);
            if (type == null)
                throw new RuntimeException("Unexpected token, value='%s', position=%s".formatted(value, position));
            tokens.add(new Token(value, position, type));
        }

        return tokens;
    }

    private Tokenizer.TokenType getType(String value) {
        if (value.matches("\\d+(\\.\\d+)?")) {
            return TokenType.Number;
        } else if (value.matches(TokenType.Identifier.pattern)) {
            return TokenType.Identifier;
        } else if (value.matches(TokenType.Operator.pattern)) {
            return TokenType.Operator;
        } else if (value.equals(",")) {
            return TokenType.Comma;
        } else if (value.equals("(")) {
            return TokenType.OpenBracket;
        } else if (value.equals(")")) {
            return TokenType.ClosedBracket;
        }
        return null;
    }

    public enum TokenType {
        Number("\\d+(\\.\\d+)?"),
        Identifier("[a-zA-Z][a-zA-Z0-9]*"),
        Operator("[+\\-*/^]"),
        Comma(","),
        OpenBracket("\\("),
        ClosedBracket("\\)");

        public final String pattern;

        TokenType(String regexp) {
            this.pattern = regexp;
        }
    }

    public record Token(String value, int position, TokenType type) {
    }
}
