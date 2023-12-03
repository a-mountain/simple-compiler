package maksym.perevalov.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import maksym.perevalov.parser.BracketsContext.BracketType;
import maksym.perevalov.parser.ParserError.CommaError;
import maksym.perevalov.parser.ParserError.IncorrectIdentifierNameError;
import maksym.perevalov.parser.Tokenizer.RowToken;
import maksym.perevalov.parser.Tokenizer.TokenType;

public class SyntaxParser {
    private final List<RowToken> rowTokens;
    private final List<SyntaxToken> syntaxTokens = new ArrayList<>();
    private final ErrorCollector errorCollector;
    private final BracketsContext bracketsContext;
    private final PositionValidator positionValidator;
    private int nextIndex = 0;

    public SyntaxParser(List<RowToken> tokens, BracketsContext bracketsContext, ErrorCollector errorCollector) {
        this.rowTokens = new UnaryMinusResolver(tokens).resolve();
        this.bracketsContext = bracketsContext;
        this.errorCollector = errorCollector;
        this.positionValidator = new PositionValidator(errorCollector);
    }

    public List<SyntaxToken> parse() {
        for (int i = 1; i < rowTokens.size(); i++) {
            this.nextIndex = i;
            RowToken currentToken = rowTokens.get(i - 1);

            transformToSyntaxTokenIfPossible(currentToken);

            if (currentToken.is(TokenType.OpenBracket)) {
                processOpenBracket(currentToken);
            }

            if (currentToken.is(TokenType.ClosedBracket)) {
                processClosedBracket(currentToken);
            }

            if (currentToken.is(TokenType.Variable) && isIncorrectName(currentToken)) {
                errorCollector.add(new IncorrectIdentifierNameError(currentToken));
            }

            if (currentToken.is(TokenType.Comma) && !bracketsContext.insideFunctionParams()) {
                errorCollector.add(new CommaError(currentToken));
            }
        }

        syntaxTokens.add(new SyntaxToken("END", rowTokens.size() + 1, SyntaxTokenType.End));

        for (int i = 1; i < rowTokens.size(); i++) {
            this.nextIndex = i;
            SyntaxToken currentToken = syntaxTokens.get(i - 1);
            SyntaxToken next = syntaxTokens.get(i);
            positionValidator.validate(currentToken, next);
        }

        bracketsContext.collectErrors();
        return syntaxTokens;
    }

    private boolean isIncorrectName(RowToken currentToken) {
        return !currentToken.value().chars().allMatch(Character::isAlphabetic);
    }

    private void transformToSyntaxTokenIfPossible(RowToken currentToken) {
        var lexemeType = SyntaxTokenType.of(currentToken.type());
        if (lexemeType != null) {
            addSyntaxToken(currentToken, lexemeType);
        }
    }

    private void processClosedBracket(RowToken curr) {
        var bracketType = bracketsContext.addClosedBracket(curr);
        if (bracketType.equals(BracketType.FunctionBracket)) {
            addSyntaxToken(curr, SyntaxTokenType.ClosedFunctionBracket);
        } else {
            addSyntaxToken(curr, SyntaxTokenType.ClosedPrecedenceBracket);
        }
    }

    private void processOpenBracket(RowToken curr) {
        RowToken token = prev();
        if (isOpenFunctionBracket(token)) {
            bracketsContext.addOpenFunctionBracket(curr);
            addSyntaxToken(curr, SyntaxTokenType.OpenFunctionBracket);
        } else {
            bracketsContext.addOpenPrecedenceBracket(curr);
            addSyntaxToken(curr, SyntaxTokenType.OpenPrecedenceBracket);
        }
    }

    private boolean isOpenFunctionBracket(RowToken token) {
        return prev() != null && token.is(TokenType.Function);
    }

    private RowToken prev() {
        try {
            return rowTokens.get(nextIndex - 2);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private void addSyntaxToken(RowToken token, SyntaxTokenType type) {
        syntaxTokens.add(new SyntaxToken(token.value(), token.position(), type));
    }

    public enum SyntaxTokenType {
        Start,
        End,
        Variable,
        Function,
        OpenFunctionBracket,
        ClosedFunctionBracket,
        Number,
        Operator,
        Comma,
        OpenPrecedenceBracket,
        ClosedPrecedenceBracket;

        private static final Map<TokenType, SyntaxTokenType> ONE_TO_ONE_MAPPING = Map.ofEntries(
              Map.entry(TokenType.Start, SyntaxTokenType.Start),
              Map.entry(TokenType.End, SyntaxTokenType.End),
              Map.entry(TokenType.Number, SyntaxTokenType.Number),
              Map.entry(TokenType.Variable, SyntaxTokenType.Variable),
              Map.entry(TokenType.Function, SyntaxTokenType.Function),
              Map.entry(TokenType.Operator, SyntaxTokenType.Operator),
              Map.entry(TokenType.Comma, SyntaxTokenType.Comma));

        public static SyntaxTokenType of(TokenType type) {
            return ONE_TO_ONE_MAPPING.get(type);
        }
    }

    public record SyntaxToken(String value, int position, SyntaxTokenType type) {
    }
}
