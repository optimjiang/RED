package org.robotframework.ide.core.testData.text.lexer.matcher;

import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.LowLevelTypesProvider;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.TxtRobotTestDataLexer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.LinkedListMultimap;


/**
 * This class take responsibility for builds up expected tokens. It the first
 * take char by char in
 * {@link #offerChar(CharBuffer, int, LinearPositionMarker)} method and after in
 * {@link #buildTokens()} it returns all matched elements.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see TxtRobotTestDataLexer
 */
public class RobotTokenMatcher {

    private TokenOutput output = new TokenOutput();
    private List<ISingleCharTokenMatcher> oneCharTokenMatchers = new LinkedList<>();


    /**
     * Constructor to use only for testing propose
     * 
     * @param output
     *            used in tests
     */
    @VisibleForTesting
    protected RobotTokenMatcher(final TokenOutput output) {
        this.output = output;
    }


    public RobotTokenMatcher() {
        oneCharTokenMatchers.add(new EndOfLineMatcher());
        oneCharTokenMatchers.add(new WhitespaceMatcher());
        oneCharTokenMatchers.add(new PipeMatcher());
        oneCharTokenMatchers.add(new AsteriskMatcher());
        oneCharTokenMatchers.add(new HashCommentMatcher());
        oneCharTokenMatchers.add(new ScalarVariableBeginSignMatcher());
        oneCharTokenMatchers.add(new ListVariableBeginSignMatcher());
        oneCharTokenMatchers.add(new EnvironmentVariableBeginSignMatcher());
        oneCharTokenMatchers.add(new DictionaryVariableBeginSignMatcher());
        oneCharTokenMatchers.add(new EqualSignMatcher());
        oneCharTokenMatchers.add(new VariableBeginCurlySignMatcher());
        oneCharTokenMatchers.add(new VariableEndCurlySignMatcher());
        oneCharTokenMatchers.add(new IndexBeginSquareSignMatcher());
        oneCharTokenMatchers.add(new IndexEndSquareSignMatcher());
        oneCharTokenMatchers.add(new ColonSignMatcher());
        oneCharTokenMatchers.add(new QuoteMarkSignMatcher());
        oneCharTokenMatchers.add(new DotSignMatcher());
        oneCharTokenMatchers.add(new EscapeBackslashSignMatcher());
    }


    public void offerChar(final CharBuffer tempBuffer, final int charIndex) {
        boolean wasUsed = false;

        for (ISingleCharTokenMatcher matcher : oneCharTokenMatchers) {
            if (matcher.match(output, tempBuffer, charIndex)) {
                wasUsed = true;
                break;
            }
        }

        if (!wasUsed) {
            convertCharToUnknownToken(tempBuffer, charIndex);
        } else {
            tryToRecognizeUnknownTokens(output);
        }
    }


    private void convertCharToUnknownToken(final CharBuffer tempBuffer,
            final int charIndex) {
        boolean useAsNewSingleUnknownToken = true;

        char c = tempBuffer.get(charIndex);

        List<RobotToken> tokens = output.getTokens();
        if (!tokens.isEmpty()) {
            int lastTokenIndex = tokens.size() - 1;
            RobotToken lastToken = tokens.get(lastTokenIndex);
            if (lastToken.getType() == RobotTokenType.UNKNOWN) {
                RobotToken newUnknownToken = new RobotToken(
                        lastToken.getStartPosition(), lastToken.getText()
                                .append(c));
                newUnknownToken.setType(RobotTokenType.UNKNOWN);
                output.setCurrentMarker(newUnknownToken.getEndPosition());
                tokens.set(lastTokenIndex, newUnknownToken);

                useAsNewSingleUnknownToken = false;
            }
        }

        if (useAsNewSingleUnknownToken) {
            StringBuilder text = new StringBuilder().append(c);
            RobotToken unknownToken = new RobotToken(output.getCurrentMarker(),
                    text);
            unknownToken.setType(RobotTokenType.UNKNOWN);
            output.setCurrentMarker(unknownToken.getEndPosition());
            output.getTokensPosition().put(RobotTokenType.UNKNOWN,
                    tokens.size());
            tokens.add(unknownToken);
        }
    }


    public TokenOutput buildTokens() {
        TokenOutput old = output;
        tryToRecognizeUnknownTokens(old);
        output = new TokenOutput();

        return old;
    }


    @VisibleForTesting
    protected List<ISingleCharTokenMatcher> getDeclaredSingleCharMatchers() {
        return oneCharTokenMatchers;
    }


    private void tryToRecognizeUnknownTokens(TokenOutput old) {
        LinkedListMultimap<RobotType, Integer> tokensPosition = old
                .getTokensPosition();
        List<Integer> listOfUnknown = tokensPosition
                .get(RobotTokenType.UNKNOWN);
        if (listOfUnknown != null) {
            List<RobotToken> tokens = old.getTokens();
            for (int i = 0; i < listOfUnknown.size(); i++) {
                Integer tokenPosition = listOfUnknown.get(i);
                RobotToken unknownRobotToken = tokens.get(tokenPosition);
                RobotToken wordToken = convertToWordType(unknownRobotToken);
                tokensPosition.put(wordToken.getType(), tokenPosition);
                tokens.set(tokenPosition, wordToken);
            }

        }

        tokensPosition.removeAll(RobotTokenType.UNKNOWN);
    }


    private RobotToken convertToWordType(RobotToken unknownRobotToken) {
        StringBuilder text = unknownRobotToken.getText();
        RobotToken token = new RobotToken(unknownRobotToken.getStartPosition(),
                text);
        RobotType type = RobotWordType.getToken(text);
        if (type == RobotWordType.UNKNOWN_WORD) {
            type = LowLevelTypesProvider.getTokenType(text);
        }

        token.setType(type);

        return token;
    }

    public static class TokenOutput {

        private LinkedListMultimap<RobotType, Integer> tokenTypeToPositionOfOcurrancy = LinkedListMultimap
                .create();
        private LinearPositionMarker currentMarker = LinearPositionMarker
                .createMarkerForFirstLineAndColumn();
        private List<RobotToken> tokens = new LinkedList<>();


        public LinkedListMultimap<RobotType, Integer> getTokensPosition() {
            return tokenTypeToPositionOfOcurrancy;
        }


        public LinearPositionMarker getCurrentMarker() {
            return currentMarker;
        }


        public void setCurrentMarker(final LinearPositionMarker newMarker) {
            this.currentMarker = newMarker;
        }


        public List<RobotToken> getTokens() {
            return tokens;
        }
    }

}
