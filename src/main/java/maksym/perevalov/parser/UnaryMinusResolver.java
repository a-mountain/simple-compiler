package maksym.perevalov.parser;

import static maksym.perevalov.parser.Tokenizer.*;

import java.util.ArrayList;
import java.util.List;

public class UnaryMinusResolver {
    private final List<RowToken> tokens;
    private int nextIndex = 0;
    private boolean skipNext;

    public UnaryMinusResolver(List<RowToken> tokens) {
        this.tokens = tokens;
    }

    public List<RowToken> resolve() {
        var result = new ArrayList<RowToken>();

        for (int i = 1; i < tokens.size(); i++) {
            if (skipNext) {
                skipNext = false;
                continue;
            }
            this.nextIndex = i;
            var prev = prev();
            var currentToken = tokens.get(i - 1);
            var next = tokens.get(i);

            if (isMinus(currentToken) && (next.is(TokenType.Number) && isLeftNotExpression(prev))) {
                skipNext = true;
                var newToken = new RowToken("-" + next.value(), currentToken.position(), next.type());
                result.add(newToken);
            } else if (isMinus(currentToken) && (isRightExpression(next) && isLeftNotExpression(prev))) {
                skipNext = true;
                var minus = new RowToken("-1", currentToken.position(), TokenType.Number);
                var multiply = new RowToken("*", currentToken.position(), TokenType.Operator);
                result.addAll(List.of(minus, multiply, next));
            } else {
                result.add(new RowToken(currentToken.value(), currentToken.position(), currentToken.type()));
            }
        }

        result.add(RowToken.endToken(result.size() + 1));

        return result;
    }

    private boolean isLeftNotExpression(RowToken token) {
        return token == null || !List.of(TokenType.Number, TokenType.Variable, TokenType.ClosedBracket).contains(token.type());
    }

    private static boolean isMinus(RowToken currentToken) {
        return currentToken.value().equals("-");
    }

    private RowToken prev() {
        try {
            return tokens.get(nextIndex - 2);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private boolean isRightExpression(RowToken token) {
        return List.of(TokenType.Number, TokenType.Variable, TokenType.Function, TokenType.OpenBracket).contains(token.type());
    }
}
