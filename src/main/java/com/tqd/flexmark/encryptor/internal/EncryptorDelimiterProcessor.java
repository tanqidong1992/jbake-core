package com.tqd.flexmark.encryptor.internal;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.delimiter.DelimiterRun;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class EncryptorDelimiterProcessor implements DelimiterProcessor {

	@Override
	public char getOpeningCharacter() {
		return '%';
	}

	@Override
	public char getClosingCharacter() {
		return '%';
	}

	@Override
	public int getMinLength() {
		return 2;
	}

	@Override
	public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer) {
        if (opener.length() >= 2 && closer.length() >= 2) {
            // Use exactly two delimiters even if we have more, and don't care about internal openers/closers.
            return 2;
        } else {
            return 0;
        }
	}

	@Override
	public void process(Delimiter opener, Delimiter closer, int delimitersUsed) {
		ToBeEncrypteNode en=new ToBeEncrypteNode(opener.getTailChars(delimitersUsed), BasedSequence.NULL, closer.getLeadChars(delimitersUsed));
		opener.moveNodesBetweenDelimitersTo(en, closer);
	}

	@Override
	public Node unmatchedDelimiterNode(InlineParser inlineParser, DelimiterRun delimiter) {
		return null;
	}

	@Override
	public boolean canBeOpener(String before, String after, boolean leftFlanking, boolean rightFlanking,
			boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
			boolean afterIsWhiteSpace) {
		return leftFlanking;
	}

	@Override
	public boolean canBeCloser(String before, String after, boolean leftFlanking, boolean rightFlanking,
			boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
			boolean afterIsWhiteSpace) {
		return rightFlanking;
	}

	@Override
	public boolean skipNonOpenerCloser() {
		return false;
	}

}
