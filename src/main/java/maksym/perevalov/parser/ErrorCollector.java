package maksym.perevalov.parser;

import static maksym.perevalov.parser.ParserError.*;
import static maksym.perevalov.parser.SyntaxParser.*;
import static maksym.perevalov.parser.Tokenizer.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ErrorCollector {

    private final List<ParserError> errors = new ArrayList<>();

    public <T extends ParserError> void add(T error) {
        errors.add(error);
    }

    public void addAll(List<? extends ParserError> errors) {
        this.errors.addAll(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> report() {
        return errors.stream()
              .sorted(Comparator.comparing(ParserError::position))
              .map(this::toReadableMessage)
              .toList();
    }

    private String toReadableMessage(ParserError syntaxError) {
        return switch (syntaxError) {
            case CommaError e ->
                  "Comma is not inside function at position '%s'".formatted(e.rowToken().position());
            case IncorrectIdentifierNameError e ->
                  "Incorrect identifier name '%s' at openBracketPosition '%s'".formatted(e.currentToken().value(), e.currentToken().position());
            case IncorrectTokenPositionError e -> {
                if (e.current().type().equals(SyntaxTokenType.Start)) {
                    yield "Math expression cannot start with %s".formatted(stringify(e.next()));
                }
                if (e.next().type().equals(SyntaxTokenType.End)) {
                    yield "Math expression cannot end with %s".formatted(stringify(e.current()));
                }
                yield "%s cannot go before %s at position '%s'".formatted(stringify(e.current()), stringify(e.next()), e.next().position());
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

    private String stringify(SyntaxToken token) {
        List<SyntaxTokenType> staticValue = List.of(
              SyntaxTokenType.ClosedFunctionBracket,
              SyntaxTokenType.ClosedPrecedenceBracket,
              SyntaxTokenType.OpenFunctionBracket,
              SyntaxTokenType.OpenPrecedenceBracket,
              SyntaxTokenType.Comma
        );
        if (staticValue.contains(token.type())) {
            return "'%s'".formatted(token.value());
        } else {
            return "%s '%s'".formatted(token.type(), token.value());
        }
    }
}
