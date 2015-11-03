package SemanticQA.models.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

public class TokenModel {

	private String token;
	private String tokenType;
	private String tokenOWLType;
	private OWLObject ontologyObject;
	private List<OWLAxiom> tokenRestrictions;
	
	public void setToken(String token){
		this.token = token;
	}
	
	public String getToken(){
		return this.token;
	}
	
	public void setTokenType(String tt){
		this.tokenType = tt;
	}
	
	public String getTokenType(){
		return this.tokenType;
	}
	
	public void setTokenOWLType(String semanticType){
		this.tokenOWLType = semanticType;
	}
	
	public String getTokenOWLType(){
		return this.tokenOWLType;
	}
	
	public void setOntologyObject(OWLObject obj){
		this.ontologyObject = obj;
	}
	
	public OWLObject getOntologyObject(){
		return this.ontologyObject;
	}
	
	public void setTokenRestriction(OWLAxiom axiom){
		/**
		 * proses instansiasi dilakukan di sini untuk menghemat memory
		 * (instansiasi dilakukan hanya jika diperlukan)
		 */
		if ( this.tokenRestrictions == null ) {
			this.tokenRestrictions = new ArrayList<OWLAxiom>();
		}
		
		this.tokenRestrictions.add(axiom);
	}
	
	public void setTokenRestrictions(Set<OWLAxiom> axioms){
		for(OWLAxiom axiom: axioms){
			setTokenRestriction(axiom);
		}
	}
	
	public List<OWLAxiom> getTokenRestriction() {
		return this.tokenRestrictions;
	}
	
	public void clear(){
		this.token = null;
		this.tokenType = null;
		this.ontologyObject = null;
		this.tokenOWLType = null;
		this.tokenRestrictions.clear();
	}
	
	public boolean isEmpty(){
		return token == null && tokenType == null;
	}
}
