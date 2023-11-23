package maksym.perevalov.parser;

import static maksym.perevalov.parser.SyntaxError.*;
import static maksym.perevalov.parser.Tokenizer.*;

import java.util.ArrayList;
import java.util.List;

public class ErrorCollector {

    private final List<SyntaxError> errors = new ArrayList<>();

    public <T extends SyntaxError> void add(T error) {
        errors.add(error);
    }

    public void addAll(List<? extends SyntaxError> errors) {
        this.errors.addAll(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> report() {
        return errors.stream()
              .map(this::toReadableMessage)
              .toList();
    }

    private String toReadableMessage(SyntaxError syntaxError) {
        return switch (syntaxError) {
            case CommaError e ->
                  "Comma is not inside function at openBracketPosition '%s'".formatted(e.rowToken().position());
            case IncorrectIdentifierNameError e ->
                  "Incorrect identifier name '%s' at openBracketPosition '%s'".formatted(e.currentToken().value(), e.currentToken().position());
            case IncorrectTokenPositionError e -> {
                if (e.current().is(TokenType.Start)) {
                    yield "Math expression cannot start with %s".formatted(stringify(e.next()));
                }
                if (e.next().is(TokenType.End)) {
                    yield "Math expression cannot end with %s".formatted(stringify(e.current()));
                }
                yield "%s cannot go after %s at position '%s'".formatted(stringify(e.current()), stringify(e.next()), e.next().position());
            }
            case NoClosedBracketError e -> "No closed bracket for '(' at position '%s'".formatted(e.openBracketPosition());
            case NoOpenBracketError e -> "No open bracket for ')' at position '%s'".formatted(e.closedBracketPosition());
            case UnknownTokenError e ->
                  "Unknown token '%s' at position '%s'".formatted(e.value(), e.position());
        };
    }

    private String stringify(RowToken token) {
        List<TokenType> staticValue = List.of(TokenType.ClosedBracket, TokenType.OpenBracket, TokenType.Comma);
        if (staticValue.contains(token.type())) {
            return "'%s'".formatted(token.value());
        } else {
            return "%s '%s'".formatted(token.type(), token.value());
        }
    }
}
