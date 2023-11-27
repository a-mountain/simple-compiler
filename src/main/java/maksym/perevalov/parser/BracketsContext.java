package maksym.perevalov.parser;

import java.util.ArrayDeque;
import java.util.Deque;

import maksym.perevalov.parser.ParserError.NoClosedBracketError;
import maksym.perevalov.parser.ParserError.NoOpenBracketError;
import maksym.perevalov.parser.Tokenizer.RowToken;

public class BracketsContext {
    private final Deque<Bracket> stack = new ArrayDeque<>();
    private final ErrorCollector errorCollector;
    private int functionNestedLevel = 0;

    public BracketsContext(ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
    }

    public boolean insideFunctionParams() {
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
            errorCollector.add(new NoOpenBracketError(token.position()));
            return BracketType.PrecedenceBracket;
        }
        var bracket = stack.pop();
        if (bracket.type.equals(BracketType.FunctionBracket)) functionNestedLevel--;
        return bracket.type();
    }

    public void collectErrors() {
        var noClosedBrackets = stack.stream()
              .map(bracket -> new NoClosedBracketError(bracket.position()))
              .toList();
        errorCollector.addAll(noClosedBrackets);
    }

    private record Bracket(BracketType type, int position) {
    }

    public enum BracketType {
        FunctionBracket,
        PrecedenceBracket
    }
}
