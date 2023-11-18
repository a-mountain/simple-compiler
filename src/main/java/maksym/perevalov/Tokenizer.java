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
    private static final String NO_REGEXP = "";

    public List<RowToken> tokenize(String input) {
        var tokens = new ArrayList<>(List.of(RowToken.startToken()));
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(input);
        int position = 0;

        while (matcher.find()) {
            position = matcher.start() + 1;
            var value = matcher.group();
            var type = getType(value);
            if (type == null)
                throw new RuntimeException("Unexpected token, value='%s', position=%s".formatted(value, position));
            tokens.add(new RowToken(value, position, type));
        }

        tokens.add(RowToken.endToken(position + 1));
        return tokens;
    }

    private Tokenizer.TokenType getType(String value) {
        if (value.matches(TokenType.Number.pattern)) {
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
        Start(NO_REGEXP),
        End(NO_REGEXP),
        Number("\\d+(\\.\\d+)?"),
        Identifier("[^+\\-*/^(),\\d\\s]+"),
        Operator("[+\\-*/^]"),
        OpenBracket("\\("),
        ClosedBracket("\\)"),
        Comma(",");

        public final String pattern;

        TokenType(String regexp) {
            this.pattern = regexp;
        }

    }

    public record RowToken(String value, int position, TokenType type) {
        public static RowToken startToken() {
            return new RowToken("", 0, TokenType.Start);
        }

        public static RowToken endToken(int position) {
            return new RowToken("", position, TokenType.End);
        }
    }
}
