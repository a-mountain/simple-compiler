package maksym.perevalov;

public class Frontend {

    /**
     * 4+cos(sin(30))/(9-3)
     * 4 30 cos 9 3 - / +
     */
    public static void main(String[] args) {
        String test = "4+cos(sin(30))/(9-3)";
        var tokenizer = new Tokenizer();
        var parser = new InfixToPostfixTransformer();
        var tokens = tokenizer.tokenize(test);
        var result = parser.transform(tokens);
        System.out.println("Tokenizer");
        tokens.stream().map(Tokenizer.Token::value).forEach(System.out::println);
        System.out.println("Parser");
        result.stream().map(Tokenizer.Token::value).forEach(System.out::println);
    }
}
