package SemanticQA.controllers;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.derivo.sparqldlapi.QueryResult;
import SemanticQA.constant.Ontology;
import SemanticQA.helpers.AnswerBuilder;
import SemanticQA.model.MySQLDatabase;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;
import SemanticQA.module.nlp.Parser;
import SemanticQA.module.nlp.Tokenizer;
import SemanticQA.module.sw.OntologyMapper;
import SemanticQA.module.sw.OntologyQuery;
import sun.net.www.protocol.http.HttpURLConnection;


@SuppressWarnings("restriction")
@Path("/qa")
public class Main {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(@QueryParam("q") String question) throws JSONException{
		
		Response responseObject = null;
		
		String[] ontologies = new String[]{
				Ontology.Path.ONTOPAR, 
				Ontology.Path.ONTOGEO, 
				Ontology.Path.ONTOGOV,
				Ontology.Path.DATASET
		};
		
		if ( question == null || question.matches("/[^a-z0-9 ]+/i") ) {
			responseObject = question == null ? 
					Response.status(HttpURLConnection.HTTP_NO_CONTENT).entity("Tidak ada pertanyaan untuk di proses!").build() :
					Response.status(HttpURLConnection.HTTP_NOT_ACCEPTABLE).entity("Kalimat tanya tidak valid!").build();
			
			throw new WebApplicationException(responseObject);
		}
				
		try {
			Tokenizer tokenizer = new Tokenizer(new MySQLDatabase());
			Parser parser = new Parser();
			OntologyMapper ontologyMapper = new OntologyMapper(ontologies, Ontology.Path.MERGED_URI);
			
			OWLOntology ontology = ontologyMapper.getOntology();
			OWLReasoner reasoner = new Reasoner(ontology);
			OntologyQuery queryEngine = new OntologyQuery(ontologyMapper, reasoner);
			
			List<SemanticToken> tokenizerResult = tokenizer.tokenize(question);
			List<Sentence> parsingResult = parser.parse(tokenizerResult);
			List<Sentence> mappingResult = ontologyMapper.map(parsingResult);
			
			QueryResult queryResult = queryEngine.execute(mappingResult);
			JSONObject finalResult = AnswerBuilder.json(queryResult);
			
			responseObject = Response.status(HttpURLConnection.HTTP_OK).entity(finalResult).build();
			
		} catch (Exception e) {
			responseObject = Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(e.getMessage()).build();
		}
		
		return responseObject;
	}
	
}
