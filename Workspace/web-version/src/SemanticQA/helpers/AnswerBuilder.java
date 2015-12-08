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
	
	private static List<String> questionConstituents;
	
	public static JSONObject json(List<Sentence> question, Map<String, Object> result) {
		
		JSONObject res = new JSONObject();
		JSONArray inferedFacts = new JSONArray();
		String answer = getSubject(question) + " adalah";
		
		boolean classNotBeenAdded = true;
		boolean subjectNotBeenAdded = true;
		boolean objectNotBeenAdded = true;
		List<String> orderOfBoundedVars = new ArrayList<String>();
		
		QueryResult query = (QueryResult) result.get(ResultKey.SPARQLDL);
		List<QueryResultModel> inferedObjects = (List<QueryResultModel>) result.get(ResultKey.INFERED_DATA);
		
		System.out.println(query.toJSON());
		
		for ( int queryBindingIndex = 0; queryBindingIndex < query.size(); queryBindingIndex++ ) {
			
			QueryBinding binding = query.get(queryBindingIndex);
			
			QueryArgument cls = new QueryArgument(QueryArgumentType.VAR, "class");
			QueryArgument sub = new QueryArgument(QueryArgumentType.VAR, "subject");
			QueryArgument obj = new QueryArgument(QueryArgumentType.VAR, "object");
			
			if ( binding.isBound(sub)) {
				String subject = binding.get(sub).toString();
				String s = shorten(subject);
				s = normalize(s);
				
				/////////
				// pertimbangkan implikasi kejamakan
				/////////
				if ( AnswerBuilder.questionConstituents.contains("saja") || subjectNotBeenAdded) {
					if ( query.size() > 0 && queryBindingIndex > 0 && queryBindingIndex < query.size() - 1 ) {
						answer += ", " + s;
					} else if ( query.size() > 0 && queryBindingIndex > 0 && queryBindingIndex == query.size() - 1 ) {
						answer += " dan " + s;
					} else {
						answer += " " + s;
					}
					orderOfBoundedVars.add(subject);
					subjectNotBeenAdded = false;
				}
			}
			
			if ( binding.isBound(cls) && classNotBeenAdded ) {
				String c = shorten(binding.get(cls).toString());
				c = normalize(c);
				answer += " " + c;
				classNotBeenAdded = false;
			}
			
			if ( binding.isBound(obj) ){
				String object = binding.get(obj).toString();
				String i = shorten(object);
				i = normalize(i);
				//////
				// pertimbangkan implikasi kejamakan
				//////
				if ( AnswerBuilder.questionConstituents.contains("saja") || objectNotBeenAdded) {
					if ( query.size() > 0 && queryBindingIndex > 0 && queryBindingIndex < query.size() - 1 ) {
						answer += ", " + i;
					} else if ( query.size() > 0 && queryBindingIndex > 0 && queryBindingIndex == query.size() - 1 ) {
						answer += " dan " + i;
					} else {
						answer += " " + i;
					}
					orderOfBoundedVars.add(object);
					objectNotBeenAdded = false;
				}
			}
		}
		
		
		for ( QueryResultModel resultModel : inferedObjects ) {
			
			JSONObject item = new JSONObject();
			JSONObject itemData = new JSONObject();
			
			Map<String,String> props = resultModel.getData();
			
			for( String key : props.keySet() ) {
				String value = props.get(key);
				String shortenedKey = shorten(key);
				//////
				// jika key adalah http://id.dbpedia.org/property/web atau 
				// value dari item berekstensi jpeg/jpg/gif, jangan di shorten
				// karena alamat aslinya dibutuhkan
				///////
				String shortnedValue  = ( shortenedKey.matches("(web|depiction)") || value.matches("(jpe?g|gif|png)$") ) ?
					value : normalize(shorten(value));
				
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
					String topic = shorten(resultModel.getSubject());
					topic = normalize(topic);
					item.put("about", topic);
					item.put("data", itemData);
					
					////////
					// pertimbangkan order list sesuai dengan urutan jawaban 
					////
					int pos = orderOfBoundedVars.indexOf(resultModel.getSubject());
					if ( pos != -1 ) {
						inferedFacts.put(pos, item);
					} else {
						inferedFacts.put(item);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
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
		String concatinatedNormalized = String.join(" ", normalized);
		return concatinatedNormalized.replaceAll("(,)", "");
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
			
			if ( s.getType().matches("(" + Type.Token.PRONOMINA + "|" + Type.Phrase.PRONOMINAL + ")") ) {
				List<SemanticToken> qConstituents = s.getConstituents();
				AnswerBuilder.questionConstituents = new ArrayList<String>(qConstituents.size());
				for ( int qsize = 0; qsize < qConstituents.size(); qsize++ ) {
					AnswerBuilder.questionConstituents.add(qConstituents.get(qsize).getToken());
				}
			}
			
		}
		
		return String.join(" ", str);
	}
}
