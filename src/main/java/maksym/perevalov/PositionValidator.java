package maksym.perevalov;

import static maksym.perevalov.Tokenizer.TokenType.*;

import java.util.List;
import java.util.Map;

import maksym.perevalov.Tokenizer.RowToken;
import maksym.perevalov.Tokenizer.TokenType;

public class PositionValidator {
    private static final Map<TokenType, List<TokenType>> ALLOWED_NEIGHBORS = Map.ofEntries(
          Map.entry(Start, List.of(OpenBracket, Variable, Function, Number)),
          Map.entry(Number, List.of(Operator, Comma, ClosedBracket, End)),
          Map.entry(Variable, List.of(Operator, Comma, ClosedBracket, End)),
          Map.entry(Function, List.of(Operator, Comma, OpenBracket, End)),
          Map.entry(Operator, List.of(OpenBracket, Variable, Number)),
          Map.entry(OpenBracket, List.of(OpenBracket, Variable, Function, Number)),
          Map.entry(ClosedBracket, List.of(Operator, ClosedBracket, Comma, End)),
          Map.entry(Comma, List.of(OpenBracket, Number, Variable, Function))
    );

    public MathError validate(RowToken current, RowToken next) {
        var allowedNextTokens = ALLOWED_NEIGHBORS.get(current.type());
        if (!allowedNextTokens.contains(next.type())) {
            return MathError.formatted("Token '%s' at position '%s' cannot be next to '%s'", current.value(), current.position(), next.value());
        }
        return null;
    }
}
