package aurora.analyzer.lexical;

import aurora.analyzer.lexical.interfaces.IToken;
import aurora.analyzer.lexical.enums.Symbol;
import aurora.analyzer.lexical.enums.Token;
import aurora.analyzer.lexical.interfaces.BufferAnalyzer;
import aurora.analyzer.lexical.utils.LogLexical;
import aurora.analyzer.lexical.utils.TokenContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static aurora.analyzer.lexical.interfaces.BufferAnalyzer.isIdentifier;
import static aurora.analyzer.lexical.interfaces.BufferAnalyzer.isNumber;

/*
 * @project aurora
 * @author Gabriel Honda on 22/02/2020
 */
public class Lexical {

    private Integer line = 0;
    private Integer column = 1;
    private List<TokenContainer> tokens;
    private LogLexical log;

    public Lexical(LogLexical log) {
        this.tokens = new ArrayList<>();
        this.log = log;
    }

    public void analyze(List<String> code) {
        for(String line : code) {
            this.line++;
            analyzeLine(line);
            this.column = 1;
        }
        this.tokens.add(new TokenContainer(Token.FINAL, "$",
                                           this.line, this.column
        ));
    }

    private void analyzeLine(String line) {
        if(line.isEmpty() && line.isBlank()) return;

        AtomicInteger index = new AtomicInteger(0);
        StringBuilder buffer = new StringBuilder();
        Character curr, next;

        while(index.get() < line.length()) {

            curr = line.charAt(index.getAndIncrement());
            next = index.get() < line.length() ? line.charAt(index.get()) : 0;

            if(curr.equals('/') && next.equals('/')) {
                return;
            }
            else if(Character.isWhitespace(curr)) {
                this.column++;
            }
            else if(curr.equals('"')) {
                analyzeQuote(line, index);
                buffer = new StringBuilder();
            }
            else if(Symbol.getValues().contains(curr.toString())) {
                var tk = new TokenContainer(Symbol.toEnum(curr.toString()),
                                            curr.toString(), this.line, this.column
                );
                tokens.add(tk);
                log.message(tk);
                this.column++;
                buffer = new StringBuilder();
            }
            else if(!Character.isLetterOrDigit(next) && !next.equals(':')) {
                buffer.append(curr);
                analyzeBuffer(buffer.toString());
                this.column += buffer.length();
                buffer = new StringBuilder();
            }
            else {
                buffer.append(curr);
            }
        }
    }

    private void analyzeQuote(String line,  AtomicInteger index) {
        StringBuilder buffer = new StringBuilder();
        buffer.append('"');
        Character curr;
        do {
            curr = line.charAt(index.getAndIncrement());
            buffer.append(curr);
            if(index.get() == line.length()) {
                log.error("missing closing quotes", this.line, this.column);
                return;
            }
        } while(!curr.equals('"'));

        var tk = new TokenContainer(Token.STRING, buffer.toString(), this.line, this.column);
        log.message(tk);
        tokens.add(tk);
        this.column += buffer.length();
    }

    private void analyzeBuffer(String buffer){
        Optional<IToken> optToken = BufferAnalyzer.isKeyword().orElse(isIdentifier())
                .orElse(isNumber()).apply(buffer);

        Consumer<IToken> addAndLog = tk -> {
            var temp = new TokenContainer(tk, buffer, this.line, this.column);
            tokens.add(temp);
            log.message(temp);
        };

        Runnable logError =  () -> log.error("the lexeme was not recognized", this.line, this.column);

        optToken.ifPresentOrElse(addAndLog, logError);

    }

    public List<TokenContainer> getTokens() {
        return tokens;
    }
}
