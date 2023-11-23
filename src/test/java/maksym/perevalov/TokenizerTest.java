package maksym.perevalov;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import maksym.perevalov.parser.ErrorCollector;
import maksym.perevalov.parser.Tokenizer;
import maksym.perevalov.parser.Tokenizer.RowToken;
import maksym.perevalov.parser.Tokenizer.TokenType;

class TokenizerTest {

    @Test
    void shouldTokenizeSimpleExpression() {
        var tokenizer = tokenizer();

        var tokens = tokenizer.tokenize("(1 + b) * min(2, 4)");

        assertEquals(List.of(
              RowToken.startToken(),
              new RowToken("(", 1, TokenType.OpenBracket),
              new RowToken("1", 2, TokenType.Number),
              new RowToken("+", 4, TokenType.Operator),
              new RowToken("b", 6, TokenType.Variable),
              new RowToken(")", 7, TokenType.ClosedBracket),
              new RowToken("*", 9, TokenType.Operator),
              new RowToken("min", 11, TokenType.Function),
              new RowToken("(", 14, TokenType.OpenBracket),
              new RowToken("2", 15, TokenType.Number),
              new RowToken(",", 16, TokenType.Comma),
              new RowToken("4", 18, TokenType.Number),
              new RowToken(")", 19, TokenType.ClosedBracket),
              RowToken.endToken(20)
        ), tokens);
    }

    private static Tokenizer tokenizer() {
        return new Tokenizer(new MathContext(List.of("min")), new ErrorCollector());
    }

    @Test
    void shouldTokenizeExpressionWithIncorrectIdentifiers() {
        var tokenizer = tokenizer();

        var tokens = tokenizer.tokenize("a_123s+%4asd2");

        assertEquals(List.of(
              RowToken.startToken(),
              new RowToken("a_", 1, TokenType.Variable),
              new RowToken("123", 3, TokenType.Number),
              new RowToken("s", 6, TokenType.Variable),
              new RowToken("+", 7, TokenType.Operator),
              new RowToken("%", 8, TokenType.Variable),
              new RowToken("4", 9, TokenType.Number),
              new RowToken("asd", 10, TokenType.Variable),
              new RowToken("2", 13, TokenType.Number),
              RowToken.endToken(14)
        ), tokens);
    }
}
