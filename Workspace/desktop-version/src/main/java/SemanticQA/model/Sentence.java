package SemanticQA.model;

import java.util.List;

public class Sentence {

	protected List<SemanticToken> semanticConstituents;
	
	public void setConstituent(List<SemanticToken> tokens) {
		
	}

	public void putConstituent(SemanticToken token) {
		
	}

	public void replaceConstituent(List<SemanticToken> constituents) {
		this.semanticConstituents = constituents;
	}
	
	public SemanticToken getContituent(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SemanticToken> getConstituents() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFunction(String f) {
		// TODO Auto-generated method stub
		
	}

	public void setType(String type) {
		// TODO Auto-generated method stub
		
	}

	public String getFunction() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

}
