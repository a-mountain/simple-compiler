package maksym.perevalov.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import maksym.perevalov.tree.MathContext;
import maksym.perevalov.parser.ParserError.UnknownTokenError;

public class Tokenizer {
    private static final String PATTERN = "(%s)|(%s)|(%s)|(%s)|(%s)|(%s)".formatted(
          TokenType.Number.pattern, TokenType.Variable.pattern, TokenType.Comma.pattern,
          TokenType.ClosedBracket.pattern, TokenType.OpenBracket.pattern, TokenType.Operator.pattern
    );
    private static final String NO_REGEXP = "";

    private final MathContext mathContext;
    private final ErrorCollector errorCollector;

    public Tokenizer(MathContext mathContext, ErrorCollector errorCollector) {
        this.mathContext = mathContext;
        this.errorCollector = errorCollector;
    }

    public List<RowToken> tokenize(String input) {
        var tokens = new ArrayList<>(List.of(RowToken.startToken()));
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(input);
        int position = 0;

        while (matcher.find()) {
            position = matcher.start() + 1;
            var value = matcher.group();
            var type = getType(value);
            if (type == null) {
                errorCollector.add(new UnknownTokenError(value, position));
            } else {
                tokens.add(new RowToken(value, position, type));
            }
        }

        tokens.add(RowToken.endToken(position + 1));
        return tokens;
    }

    private Tokenizer.TokenType getType(String value) {
        if (value.matches(TokenType.Number.pattern)) {
            return TokenType.Number;
        } else if (value.matches(TokenType.Variable.pattern)) {
            if (mathContext.isFunction(value)) {
                return TokenType.Function;
            } else {
                return TokenType.Variable;
            }
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
        Variable("[^+\\-*/^(),\\d\\s]+"),
        Function(NO_REGEXP),
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

        public boolean is(TokenType type) {
            return this.type.equals(type);
        }

        public static RowToken startToken() {
            return new RowToken("START", 0, TokenType.Start);
        }

        public static RowToken endToken(int position) {
            return new RowToken("END", position, TokenType.End);
        }
    }
}
