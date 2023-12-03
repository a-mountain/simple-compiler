package maksym.perevalov.parser;

import static maksym.perevalov.parser.SyntaxParser.*;

import maksym.perevalov.parser.Tokenizer.RowToken;

public sealed interface ParserError {

    int position();

    record UnknownTokenError(String value, int position) implements ParserError {
    }

    record NoOpenBracketError(int closedBracketPosition) implements ParserError {
        @Override
        public int position() {
            return closedBracketPosition;
        }
    }

    record NoClosedBracketError(int openBracketPosition) implements ParserError {
        @Override
        public int position() {
            return openBracketPosition;
        }
    }

    record IncorrectTokenPositionError(SyntaxToken current, SyntaxToken next) implements ParserError {
        @Override
        public int position() {
            return current.position();
        }
    }

    record CommaError(RowToken rowToken) implements ParserError {

        @Override
        public int position() {
            return rowToken.position();
        }
    }

    record IncorrectIdentifierNameError(RowToken currentToken) implements ParserError {
        @Override
        public int position() {
            return currentToken.position();
        }
    }
}
