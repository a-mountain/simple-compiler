package maksym.perevalov.parser;

import static maksym.perevalov.parser.Tokenizer.TokenType.*;

import java.util.List;
import java.util.Map;

import maksym.perevalov.parser.Tokenizer.RowToken;
import maksym.perevalov.parser.Tokenizer.TokenType;

public class PositionValidator {
    private static final Map<TokenType, List<TokenType>> ALLOWED_NEIGHBORS = Map.ofEntries(
          Map.entry(Start, List.of(OpenBracket, Variable, Function, Number)),
          Map.entry(Number, List.of(Operator, Comma, ClosedBracket, End)),
          Map.entry(Variable, List.of(Operator, Comma, ClosedBracket, End)),
          Map.entry(Function, List.of(Operator, Comma, OpenBracket, End)),
          Map.entry(Operator, List.of(OpenBracket, Variable, Number, Function)),
          Map.entry(OpenBracket, List.of(OpenBracket, Variable, Function, Number)),
          Map.entry(ClosedBracket, List.of(Operator, ClosedBracket, Comma, End)),
          Map.entry(Comma, List.of(OpenBracket, Number, Variable, Function))
    );
    private final ErrorCollector errorCollector;

    public PositionValidator(ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
    }

    public void validate(RowToken current, RowToken next) {
        var allowedNextTokens = ALLOWED_NEIGHBORS.get(current.type());
        if (!allowedNextTokens.contains(next.type())) {
            errorCollector.add(new ParserError.IncorrectTokenPositionError(current, next));
        }
    }
}
