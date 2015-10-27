package SemanticQA.models.nlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import SemanticQA.constant.Token;
import SemanticQA.helpers.QATokenModel;

public class Parser {

	public static interface ParserListener{
		public void onParseSuccess(TreeMap<String, String> parseTree);
	}
	
	public Map<String,Object> parse(List<QATokenModel> taggedToken){
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("success", false);
		
		List<QATokenModel> phrases = createPhrase(null, taggedToken, new ArrayList<QATokenModel>());
		result.put("data", analyzeSyntacticFunction(phrases));
		
		return result;
	}
	
	private List<QATokenModel> createPhrase(QATokenModel previousToken, List<QATokenModel> data, List<QATokenModel> result){
		
		int index = result.isEmpty() ? -1 : result.size() - 1;
		QATokenModel currentToken = (data.size() > 0) ? data.remove(0) : null;
		QATokenModel currentPhrase = index == -1 ? null : result.get(index);
		
		if(currentToken != null)
		{
			
			if(previousToken != null)
			{
			
				switch (currentToken.getWordType()) 
				{
				
				case Token.TYPE_PRONOMINA:
					if(currentToken.getWord().matches("(siapa|dimana)"))
					{
						result.add(currentToken);
					} 
					else if(isPhrasePronominal(currentPhrase) || isPhraseNominal(currentPhrase) || currentPhrase.getWordType().equals(Token.TYPE_PREPOSISI)) {
						if(isPhraseNominal(currentPhrase))
						{
							currentPhrase.setWordType(Token.TYPE_FRASA_NOMINAL);
						} 
						else {
							currentPhrase.setWordType(Token.TYPE_FRASA_PRONOMINAL);
						}
						currentPhrase.setWord(currentPhrase.getWord() + " " + currentToken.getWord());
						result.set(index, currentPhrase);
					}
					
					break;
				case Token.TYPE_NOMINA:
					if(previousToken.getWordType().equals(Token.TYPE_NOMINA))
					{
						currentPhrase.setWord(currentPhrase.getWord() + " " + currentToken.getWord());
						currentPhrase.setWordType(Token.TYPE_FRASA_NOMINAL);
						result.set(index, currentPhrase);
					} 
					
					else if(previousToken.getWordType().equals(Token.TYPE_PREPOSISI) && 
							(currentPhrase.getWordType().equals(Token.TYPE_PREPOSISI) || 
									currentPhrase.getWordType().equals(Token.TYPE_FRASA_PREPOSISIONAL))) 
					{
						
						currentPhrase.setWord(currentPhrase.getWord() + " " + currentToken.getWord());
						currentPhrase.setWordType(Token.TYPE_FRASA_PREPOSISIONAL);
						result.set(index, currentPhrase);
						
					}
					else {
						result.add(currentToken);
					}
					
					break;
				case Token.TYPE_KONJUNGSI:
					result.add(currentToken);
					break;
				case Token.TYPE_PREPOSISI:
					if(previousToken.getWordType().equals(Token.TYPE_KONJUNGSI) && 
							(currentPhrase.getWordType().equals(Token.TYPE_PREPOSISI) || 
									currentPhrase.getWordType().equals(Token.TYPE_FRASA_PREPOSISIONAL) ||
									currentPhrase.getWordType().equals(Token.TYPE_KONJUNGSI)))
					{
						currentPhrase.setWord(currentPhrase.getWord() + " " + currentToken.getWord());
						currentPhrase.setWordType(Token.TYPE_FRASA_PREPOSISIONAL);
						result.set(index, currentPhrase);
					} 
					else {
						result.add(currentToken);
					}
					break;
				case Token.TYPE_ADJEKTIVA:
					
					if(isPhraseNominal(currentPhrase))
					{
						currentPhrase.setWord(currentPhrase.getWord() + " " + currentToken.getWord());
						currentPhrase.setWordType(Token.TYPE_FRASA_NOMINAL);
						result.set(index, currentPhrase);
					}
					
					break;
				default:
					result.add(currentToken);
					break;
				}
			} 
			else {
				result.add(currentToken);
			}
			
			createPhrase(currentToken, data, result);
		}
		
		return result;
	}
	
	private List<QATokenModel> analyzeSyntacticFunction(List<QATokenModel> phrases){
		List<QATokenModel> result = new ArrayList<QATokenModel>();
		
		return phrases;
	}
	
	private boolean isPhrasePronominal(QATokenModel model){
		return model.getWordType().equals(Token.TYPE_PRONOMINA) || model.getWordType().equals(Token.TYPE_FRASA_PRONOMINAL);
	}
	
	private boolean isPhraseNominal(QATokenModel model){
		return model.getWordType().equals(Token.TYPE_NOMINA) || model.getWordType().equals(Token.TYPE_FRASA_NOMINAL);
	}
}
