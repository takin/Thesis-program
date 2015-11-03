package SemanticQA.models.nlp;

import java.util.ArrayList;
import java.util.List;

public class QuestionModel {
	
	private String phraseType;
	private List<TokenModel> phrases;
	private String wordSemanticRole;
	private String syntacticFunction;
	
	public QuestionModel(TokenModel m) {
		this.phrases = new ArrayList<TokenModel>();
		this.phrases.add(m);
	}
	
	public void setType(String type) {
		this.phraseType = type;
	}
	
	public String getType() {
		return this.phraseType;
	}
	
	public void addConstituent(String type, String word){
		TokenModel m = new TokenModel();
		
		m.setToken(word);
		m.setTokenType(type);
		
		addConstituent(m);
	}
	
	public void addConstituent(TokenModel phrase){
		this.phrases.add(phrase);
	}
	
	public void setConstituent(int position, TokenModel constituent) {
		this.phrases.set(position, constituent);
	}
	
	public void replaceConstituent(List<TokenModel> constituents) {
		this.phrases = constituents;
	}
	
	public List<TokenModel> getConstituents(){
		return this.phrases;
	}
	
	public void setSyntacticFunction(String f){
		this.syntacticFunction = f;
	}
	
	public String getSyntacticFunction(){
		return this.syntacticFunction;
	}
	
	public void setWordSemanticRole(String role){
		this.wordSemanticRole = role;
	}
	
	public String getWordSemanticRole(){
		return this.wordSemanticRole;
	}
	
	public void clear() {
		this.phraseType = null;
		this.wordSemanticRole = null;
		this.phrases.clear();
	}
}
