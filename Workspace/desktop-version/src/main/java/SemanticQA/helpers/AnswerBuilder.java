package SemanticQA.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import SemanticQA.constant.Type;
import SemanticQA.model.QueryResultModel;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;
import SemanticQA.module.sw.OntologyQuery.ResultKey;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryBinding;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.types.QueryArgumentType;

public class AnswerBuilder {
	
	public static JSONObject json(List<Sentence> question, Map<String, Object> result) {
		
		JSONObject res = new JSONObject();
		JSONArray inferedFacts = new JSONArray();
		
		String answer = getSubject(question) + " adalah";
		
		QueryResult query = (QueryResult) result.get(ResultKey.SPARQLDL);
		List<QueryResultModel> inferedObjects = (List<QueryResultModel>) result.get(ResultKey.INFERED_DATA);
		
		for ( QueryResultModel resultModel : inferedObjects ) {
			
			JSONObject item = new JSONObject();
			JSONObject itemData = new JSONObject();
			
			Map<String,String> props = resultModel.getData();
			
			for( String key : props.keySet() ) {
				String shortenedKey = shorten(key);
				String shortnedValue = shorten(props.get(key));
				if ( !shortenedKey.matches("(type)") ) {
					try {
					itemData.put(shortenedKey, shortnedValue);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			
			if ( itemData.length() > 0 ) {
				try {
					item.put("about", shorten(resultModel.getSubject()));
					item.put("data", itemData);
					inferedFacts.put(item);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		boolean classNotBeenAdded = true;
		boolean subjectNotBeenAdded = true;
		boolean objectNotBeenAdded = true;
		
		for ( QueryBinding b:query ) {
			QueryArgument cls = new QueryArgument(QueryArgumentType.VAR, "class");
			QueryArgument sub = new QueryArgument(QueryArgumentType.VAR, "subject");
			QueryArgument obj = new QueryArgument(QueryArgumentType.VAR, "object");
			
			if ( b.isBound(sub) && subjectNotBeenAdded ) {
				String s = shorten(b.get(sub).toString());
				s = normalize(s);
				answer += " " + s;
				subjectNotBeenAdded = false;
			}
			
			if ( b.isBound(cls) && classNotBeenAdded ) {
				String c = shorten(b.get(cls).toString());
				c = normalize(c);
				answer += " " + c;
				classNotBeenAdded = false;
			}
			
			if ( b.isBound(obj) && objectNotBeenAdded ) {
				String i = shorten(b.get(obj).toString());
				i = normalize(i);
				answer += " " + i;
				objectNotBeenAdded = false;
			}
		}
		
		try {
			res.put("text", answer);
			res.put("inferedFacts", inferedFacts);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	private static String shorten(String uri) {
		String sf = uri.replaceAll("^[a-z].*.(#|/)", "");
		return sf;
	}
	
	private static String normalize(String str) {
		
		String arrNormalized[] = str.split("_");
		String normalized[] = new String[arrNormalized.length];
		
		for (int i = 0; i < arrNormalized.length; i++) {
			String n = arrNormalized[i];
			normalized[i] = n.substring(0, 1).toUpperCase() + n.substring(1, n.length());
		}
		
		return String.join(" ", normalized);
	}
	
	private static String getSubject(List<Sentence> question) {
		
		List<String> str = new ArrayList<String>();
		
		for ( Sentence s:question ) {
			
			if ( !s.getType().equals(Type.Token.PRONOMINA) && 
					!s.getType().equals(Type.Phrase.PRONOMINAL) ) {
				
				List<SemanticToken> constituents = s.getConstituents();
				
				for ( SemanticToken c : constituents ) {
					String token = c.getToken();
					if ( str.isEmpty() || c.getType().equals(Type.Token.NOMINA) ) {
						token = normalize(token);
					}
					str.add(token); 
				}	
			}
		}
		
		return String.join(" ", str);
	}
}
