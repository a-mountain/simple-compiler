package maksym.perevalov;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import maksym.perevalov.Tokenizer.RowToken;

public class BracketsContext {
    private final Deque<Bracket> stack = new ArrayDeque<>();
    private final List<SyntaxError> errors = new ArrayList<>();
    private int functionNestedLevel = 0;

    public boolean insideFunction() {
        return functionNestedLevel > 0;
    }

    public void addOpenFunctionBracket(RowToken token) {
        functionNestedLevel++;
        stack.push(new Bracket(BracketType.FunctionBracket, token.position()));
    }

    public void addOpenPrecedenceBracket(RowToken token) {
        stack.push(new Bracket(BracketType.PrecedenceBracket, token.position()));
    }

    public BracketType addClosedBracket(RowToken token) {
        if (stack.isEmpty()) {
            errors.add(new MathError("No open bracket for ')' at position '%s'".formatted(token.position())));
            return BracketType.PrecedenceBracket;
        }
        var bracket = stack.pop();
        if (bracket.type.equals(BracketType.FunctionBracket)) functionNestedLevel--;
        return bracket.type();
    }

    public List<SyntaxError> collectErrors() {
        var noClosedBrackets = stack.stream()
              .map(bracket -> MathError.formatted("No closed bracket for '(' at position '$s'", bracket.position()))
              .toList();
        errors.addAll(noClosedBrackets);
        return errors;
    }

    private record Bracket(BracketType type, int position) {
    }

    public enum BracketType {
        FunctionBracket,
        PrecedenceBracket
    }
}
