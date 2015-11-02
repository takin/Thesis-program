package SemanticQA.models.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

public class TokenModel {

	private String word;
	private String type;
	private String owlType;
	private OWLObject ontologyObject;
	private List<OWLAxiom> restrictions;
	
	public void setWord(String word){
		this.word = word;
	}
	
	public String getWord(){
		return this.word;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setOWLType(String semanticType){
		this.owlType = semanticType;
	}
	
	public String getOWLType(){
		return this.owlType;
	}
	
	public void setOntologyObject(OWLObject obj){
		this.ontologyObject = obj;
	}
	
	public OWLObject getOntologyObject(){
		return this.ontologyObject;
	}
	
	public void setRestriction(OWLAxiom axiom){
		/**
		 * proses instansiasi dilakukan di sini untuk menghemat memory
		 * (instansiasi dilakukan hanya jika diperlukan)
		 */
		if ( this.restrictions == null ) {
			this.restrictions = new ArrayList<OWLAxiom>();
		}
		
		restrictions.add(axiom);
	}
	
	public void setRestriction(Set<OWLAxiom> axioms){
		for(OWLAxiom axiom: axioms){
			setRestriction(axiom);
		}
	}
	
	public List<OWLAxiom> getRestriction() {
		return this.restrictions;
	}
	
	public void clear(){
		this.word = null;
		this.type = null;
		this.ontologyObject = null;
		this.owlType = null;
		this.restrictions.clear();
	}
	
	public boolean isEmpty(){
		return word == null && type == null;
	}
}
