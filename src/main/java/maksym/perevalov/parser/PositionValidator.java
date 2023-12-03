package maksym.perevalov.parser;


import java.util.List;
import java.util.Map;

import static maksym.perevalov.parser.SyntaxParser.*;
import static maksym.perevalov.parser.SyntaxParser.SyntaxTokenType.*;

public class PositionValidator {
    private static final Map<SyntaxTokenType, List<SyntaxTokenType>> ALLOWED_NEIGHBORS = Map.ofEntries(
          Map.entry(Start, List.of(OpenFunctionBracket, OpenPrecedenceBracket, Variable, Function, Number)),
          Map.entry(Number, List.of(Operator, Comma, ClosedFunctionBracket, ClosedPrecedenceBracket, End)),
          Map.entry(Variable, List.of(Operator, Comma, ClosedFunctionBracket, ClosedPrecedenceBracket, End)),
          Map.entry(Function, List.of(Operator, Comma, OpenPrecedenceBracket, OpenFunctionBracket, End)),
          Map.entry(Operator, List.of(OpenPrecedenceBracket, OpenFunctionBracket, Variable, Number, Function)),
          Map.entry(OpenFunctionBracket, List.of(OpenFunctionBracket, OpenPrecedenceBracket, ClosedFunctionBracket, Function, Number, Variable)),
          Map.entry(OpenPrecedenceBracket, List.of(OpenFunctionBracket, OpenPrecedenceBracket, Variable, Function, Number)),
          Map.entry(ClosedFunctionBracket, List.of(Operator, ClosedPrecedenceBracket, ClosedFunctionBracket, Comma, End)),
          Map.entry(ClosedPrecedenceBracket, List.of(Operator, ClosedPrecedenceBracket, ClosedFunctionBracket, Comma, End)),
          Map.entry(Comma, List.of(OpenFunctionBracket, OpenPrecedenceBracket, Number, Variable, Function))
    );

    private final ErrorCollector errorCollector;

    public PositionValidator(ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
    }

    public void validate(SyntaxToken current, SyntaxToken next) {
        var allowedNextTokens = ALLOWED_NEIGHBORS.get(current.type());
        if (!allowedNextTokens.contains(next.type())) {
            errorCollector.add(new ParserError.IncorrectTokenPositionError(current, next));
        }
    }
}
