package maksym.perevalov.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import maksym.perevalov.parser.BracketsContext.BracketType;
import maksym.perevalov.parser.SyntaxError.CommaError;
import maksym.perevalov.parser.SyntaxError.IncorrectIdentifierNameError;
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
        this.rowTokens = tokens;
        this.bracketsContext = bracketsContext;
        this.errorCollector = errorCollector;
        this.positionValidator = new PositionValidator(errorCollector);
    }

    public List<SyntaxToken> analyze() {
        for (int i = 1; i < rowTokens.size(); i++) {
            this.nextIndex = i;
            RowToken currentToken = rowTokens.get(i - 1);
            RowToken next = rowTokens.get(i);

            positionValidator.validate(currentToken, next);

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

            if (currentToken.is(TokenType.Comma) && !bracketsContext.insideFunction()) {
                errorCollector.add(new CommaError(currentToken));
            }
        }

        bracketsContext.collectErrors();
        return syntaxTokens;
    }

    private static boolean isIncorrectName(RowToken currentToken) {
        return !currentToken.value().chars().allMatch(Character::isAlphabetic);
    }

    private void transformToSyntaxTokenIfPossible(RowToken currentToken) {
        var lexemeType = LexemeType.of(currentToken.type());
        if (lexemeType != null) {
            addSyntaxToken(currentToken, lexemeType);
        }
    }

    private void processClosedBracket(RowToken curr) {
        var bracketType = bracketsContext.addClosedBracket(curr);
        if (bracketType.equals(BracketType.FunctionBracket)) {
            addSyntaxToken(curr, LexemeType.ClosedFunctionBracket);
        } else {
            addSyntaxToken(curr, LexemeType.ClosedBracket);
        }
    }

    private void processOpenBracket(RowToken curr) {
        RowToken token = prev();
        if (prev() != null && token.is(TokenType.Function)) {
            bracketsContext.addOpenFunctionBracket(curr);
            addSyntaxToken(curr, LexemeType.OpenFunctionBracket);
        } else {
            bracketsContext.addOpenPrecedenceBracket(curr);
            addSyntaxToken(curr, LexemeType.OpenBracket);
        }
    }

    public RowToken prev() {
        try {
            return rowTokens.get(nextIndex - 2);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private void addSyntaxToken(RowToken token, LexemeType type) {
        syntaxTokens.add(new SyntaxToken(token.value(), token.position(), type));
    }

    public enum LexemeType {
        Start,
        End,
        Variable,
        Function,
        OpenFunctionBracket,
        ClosedFunctionBracket,
        Number,
        Operator,
        Comma,
        OpenBracket,
        ClosedBracket;

        private static final Map<TokenType, LexemeType> ONE_TO_ONE_MAPPING = Map.ofEntries(
              Map.entry(TokenType.Start, LexemeType.Start),
              Map.entry(TokenType.End, LexemeType.End),
              Map.entry(TokenType.Number, LexemeType.Number),
              Map.entry(TokenType.Variable, LexemeType.Variable),
              Map.entry(TokenType.Function, LexemeType.Function),
              Map.entry(TokenType.Operator, LexemeType.Operator),
              Map.entry(TokenType.Comma, LexemeType.Comma));

        public static LexemeType of(TokenType type) {
            return ONE_TO_ONE_MAPPING.get(type);
        }
    }

    public record SyntaxToken(String value, int position, LexemeType type) {
    }
}
