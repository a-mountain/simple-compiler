package maksym.perevalov.parser;

import maksym.perevalov.parser.Tokenizer.RowToken;

public sealed interface SyntaxError {

    record UnknownTokenError(String value, int position) implements SyntaxError {
    }

    record NoOpenBracketError(int closedBracketPosition) implements SyntaxError {
    }

    record NoClosedBracketError(int openBracketPosition) implements SyntaxError {
    }

    record IncorrectTokenPositionError(RowToken current, RowToken next) implements SyntaxError {

    }

    record CommaError(RowToken rowToken) implements SyntaxError {

    }

    record IncorrectIdentifierNameError(RowToken currentToken) implements SyntaxError {

    }
}
