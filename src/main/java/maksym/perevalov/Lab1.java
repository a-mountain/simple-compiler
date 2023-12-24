package maksym.perevalov;

import java.util.List;

import maksym.perevalov.parser.BracketsContext;
import maksym.perevalov.parser.ErrorCollector;
import maksym.perevalov.parser.SyntaxParser;
import maksym.perevalov.parser.Tokenizer;
import maksym.perevalov.tree.MathContext;

public class Lab1 {

    public static void main(String[] args) {
        // Lab 1
        // a(), -1
        var app = new Lab1();
        var context = new MathContext(List.of("f", "send", "A"));
        app.run("ю.1 -* ((2 * -x^2-5*x+7)(", context);
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
