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
	
	public void setPhrases(String type, String word){
		TokenModel m = new TokenModel();
		
		m.setWord(word);
		m.setType(type);
		
		setPhrases(m);
	}
	
	public void setPhrases(TokenModel phrase){
		this.phrases.add(phrase);
	}
	
	public List<TokenModel> getPhrases(){
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
