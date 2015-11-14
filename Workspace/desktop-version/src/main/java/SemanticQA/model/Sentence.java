package SemanticQA.model;

import java.util.ArrayList;
import java.util.List;

public class Sentence {

	private List<SemanticToken> semanticConstituents;
	private String syntacticFunction;
	private String phraseType;
	
	public void setConstituent(List<SemanticToken> tokens) {
		this.semanticConstituents = tokens;
	}
	
	public void putConstituents(List<SemanticToken> tokens) {
		this.semanticConstituents.addAll(tokens);
	}

	public void putConstituent(SemanticToken token) {
		if ( semanticConstituents == null ) {
			this.semanticConstituents = new ArrayList<SemanticToken>();
		}
		this.semanticConstituents.add(token);
	}

	public void replaceConstituent(List<SemanticToken> constituents) {
		this.semanticConstituents = constituents;
	}
	
	public SemanticToken getContituent(int index) {
		return this.semanticConstituents.get(index);
	}

	public List<SemanticToken> getConstituents() {
		return this.semanticConstituents;
	}

	public void setFunction(String f) {
		this.syntacticFunction = f;
	}

	public void setType(String type) {
		this.phraseType = type;
	}

	public String getFunction() {
		return this.syntacticFunction;
	}

	public String getType() {
		return this.phraseType;
	}

	public void clear() {
		this.semanticConstituents.clear();
		this.phraseType = null;
		this.syntacticFunction = null;
	}

	public boolean isEmpty() {
		return this.semanticConstituents.isEmpty() && this.phraseType == null && this.syntacticFunction == null;
	}

}
