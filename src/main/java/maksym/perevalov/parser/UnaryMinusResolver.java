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
            RowToken currentToken = tokens.get(i - 1);
            RowToken next = tokens.get(i);

            if (isMinus(currentToken) && (isValue(next) && prevIsNotValue())) {
                skipNext = true;
                var newToken = new RowToken("-" + next.value(), currentToken.position(), next.type());
                result.add(newToken);
            } else {
                result.add(currentToken);
            }
        }

        result.add(RowToken.endToken(result.size() + 1));

        return result;
    }

    private boolean prevIsNotValue() {
        return prev() == null || !isValue(prev());
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

    private boolean isValue(RowToken token) {
        return token.is(TokenType.Number) || token.is(TokenType.Variable);
    }
}
