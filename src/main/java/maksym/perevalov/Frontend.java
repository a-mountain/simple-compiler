package maksym.perevalov;

import java.util.List;

import maksym.perevalov.parser.ErrorCollector;
import maksym.perevalov.parser.Tokenizer;

public class Frontend {

    /**
     * 4 4 1 + 5 2 / sin -
     *
     */
    public static void main(String[] args) {
        String test = "4 + pow(5+6, pow(3 / 2, 6 + 4))";
//        String test = "5+6 + 1-3";
        var errorCollector = new ErrorCollector();
        var tokenizer = new Tokenizer(new MathContext(List.of("pow")), errorCollector);
        var parser = new InfixToPostfixTransformer();
        var tokens = tokenizer.tokenize(test);
        var result = parser.transform(tokens);
        System.out.println("Tokenizer");
        tokens.stream().map(Tokenizer.RowToken::value).forEach(System.out::println);
        System.out.println("Parser");
        result.stream().forEach(System.out::println);
    }
}
