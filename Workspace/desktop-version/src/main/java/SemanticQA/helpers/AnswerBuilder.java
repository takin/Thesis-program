package SemanticQA.helpers;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import SemanticQA.constant.Type;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryBinding;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.types.QueryArgumentType;

public class AnswerBuilder {
	
	public static JSONObject json(List<Sentence> question, QueryResult query) {
		JSONObject res = new JSONObject();
		
		String answer = getSubject(question) + " adalah";
		
		for ( QueryBinding b:query ) {
			QueryArgument cls = new QueryArgument(QueryArgumentType.VAR, "type");
			QueryArgument sub = new QueryArgument(QueryArgumentType.VAR, "subject");
			QueryArgument ind = new QueryArgument(QueryArgumentType.VAR, "object");
			QueryArgument val = new QueryArgument(QueryArgumentType.VAR, "value");
	
			if ( b.isBound(sub) ) {
				String s = shorten(b.get(sub).toString());
				s = normalize(s);
				answer += " <b>" + s + "</b>";
			}
			
			if ( b.isBound(cls) ) {
				String c = shorten(b.get(cls).toString());
				c = normalize(c);
				answer += " <b>" + c + "</b>";
			}
			
			if ( b.isBound(ind) ) {
				String i = shorten(b.get(ind).toString());
				i = normalize(i);
				answer += " <b>" + i + "</b>";
			}
			
			if (b.isBound(val)) {
				String v = shorten(b.get(val).toString());
				v = normalize(v);
				answer += "<b>" + v + "</b>";
			}
		}
		
		try {
			res.put("statusCode", 200);
			res.put("answer", answer);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	private static String shorten(String uri) {
		String sf = uri.replaceAll("^[a-z].*.#", "");
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
					if ( c.getType().equals(Type.Token.NOMINA) ) {
						token = normalize(token);
					}
					str.add(token); 
				}	
			}
		}
		
		return String.join(" ", str);
	}
}
