package maksym.perevalov.parser;

import maksym.perevalov.parser.Tokenizer.RowToken;

public sealed interface ParserError {

    record UnknownTokenError(String value, int position) implements ParserError {
    }

    record NoOpenBracketError(int closedBracketPosition) implements ParserError {
    }

    record NoClosedBracketError(int openBracketPosition) implements ParserError {
    }

    record IncorrectTokenPositionError(RowToken current, RowToken next) implements ParserError {

    }

    record CommaError(RowToken rowToken) implements ParserError {

    }

    record IncorrectIdentifierNameError(RowToken currentToken) implements ParserError {

    }
}
