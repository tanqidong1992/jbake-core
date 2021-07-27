package com.tqd.flexmark.encryptor.internal;

import org.jetbrains.annotations.NotNull;

import com.vladsch.flexmark.util.ast.DelimitedNode;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class ToBeEncrypteNode  extends Node implements DelimitedNode{

	  protected BasedSequence openingMarker = BasedSequence.NULL;
	    protected BasedSequence text = BasedSequence.NULL;
	    protected BasedSequence closingMarker = BasedSequence.NULL;

	    @NotNull
	    @Override
	    public BasedSequence[] getSegments() {
	        return new BasedSequence[] { openingMarker, text, closingMarker };
	    }

	    @Override
	    public void getAstExtra(@NotNull StringBuilder out) {
	        delimitedSegmentSpan(out, openingMarker, text, closingMarker, "text");
	    }

	    public ToBeEncrypteNode() {
	    }

	    public ToBeEncrypteNode(BasedSequence chars) {
	        super(chars);
	    }

	    public ToBeEncrypteNode(BasedSequence openingMarker, BasedSequence text, BasedSequence closingMarker) {
	        super(openingMarker.baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()));
	        this.openingMarker = openingMarker;
	        this.text = text;
	        this.closingMarker = closingMarker;
	    }

	    public BasedSequence getOpeningMarker() {
	        return openingMarker;
	    }

	    public void setOpeningMarker(BasedSequence openingMarker) {
	        this.openingMarker = openingMarker;
	    }

	    public BasedSequence getText() {
	        return text;
	    }

	    public void setText(BasedSequence text) {
	        this.text = text;
	    }

	    public BasedSequence getClosingMarker() {
	        return closingMarker;
	    }

	    public void setClosingMarker(BasedSequence closingMarker) {
	        this.closingMarker = closingMarker;
	    }

}
