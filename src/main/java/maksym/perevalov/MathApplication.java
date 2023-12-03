package maksym.perevalov;

import java.util.List;

import maksym.perevalov.parser.BracketsContext;
import maksym.perevalov.parser.ErrorCollector;
import maksym.perevalov.parser.SyntaxParser;
import maksym.perevalov.parser.Tokenizer;

public class MathApplication {

    public static void main(String[] args) {
        // a(), -1
        var application = new MathApplication();
        var context = new MathContext(List.of("f", "send", "A"));
        application.run("12.1 + (2 * x^2-5 * x+7)-(0-i)+ (j+1)/(0 - t)-(-f(1, 7-x, 5))/q + send((2 * x+7)/A(j, i), 127.0 + 0.1 ) + 1 - (f())", context);
    }

    public void run(String input, MathContext context) {
        if (input.length() > 500) {
            System.out.println("Input string is too long");
            return;
        }
        if (input.isEmpty()) {
            System.out.println("Input string is empty");
            return;
        }
        var errorCollector = new ErrorCollector();
        var tokenizer = new Tokenizer(context, errorCollector);
        var tokens = tokenizer.tokenize(input);
        tokens.forEach(System.out::println);
        var parser = new SyntaxParser(tokens, new BracketsContext(errorCollector), errorCollector);
        var syntaxTokens = parser.parse();
        if (errorCollector.hasErrors()) {
            System.out.println("---Errors---");
            errorCollector.report().forEach(System.out::println);
        } else {
            System.out.println("---Syntax tokens---");
            syntaxTokens.forEach(System.out::println);
        }
    }
}
