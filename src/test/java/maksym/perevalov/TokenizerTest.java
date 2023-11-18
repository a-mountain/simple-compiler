package maksym.perevalov;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import maksym.perevalov.Tokenizer.RowToken;
import maksym.perevalov.Tokenizer.TokenType;

class TokenizerTest {

    @Test
    void shouldTokenizeSimpleExpression() {
        var tokenizer = new Tokenizer();

        var tokens = tokenizer.tokenize("(1 + b) * min(2, 4)");

        assertEquals(List.of(
              RowToken.startToken(),
              new RowToken("(", 1, TokenType.OpenBracket),
              new RowToken("1", 2, TokenType.Number),
              new RowToken("+", 4, TokenType.Operator),
              new RowToken("b", 6, TokenType.Identifier),
              new RowToken(")", 7, TokenType.ClosedBracket),
              new RowToken("*", 9, TokenType.Operator),
              new RowToken("min", 11, TokenType.Identifier),
              new RowToken("(", 14, TokenType.OpenBracket),
              new RowToken("2", 15, TokenType.Number),
              new RowToken(",", 16, TokenType.Comma),
              new RowToken("4", 18, TokenType.Number),
              new RowToken(")", 19, TokenType.ClosedBracket),
              RowToken.endToken(20)
        ), tokens);
    }

    @Test
    void shouldTokenizeExpressionWithIncorrectIdentifiers() {
        var tokenizer = new Tokenizer();

        var tokens = tokenizer.tokenize("a_123s+%4asd2");

        assertEquals(List.of(
              RowToken.startToken(),
              new RowToken("a_", 1, TokenType.Identifier),
              new RowToken("123", 3, TokenType.Number),
              new RowToken("s", 6, TokenType.Identifier),
              new RowToken("+", 7, TokenType.Operator),
              new RowToken("%", 8, TokenType.Identifier),
              new RowToken("4", 9, TokenType.Number),
              new RowToken("asd", 10, TokenType.Identifier),
              new RowToken("2", 13, TokenType.Number),
              RowToken.endToken(14)
        ), tokens);
    }
}
