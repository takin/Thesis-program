package SemanticQA.models.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

public class QATokenModel {

	private String word;
	private String wordType;
	private String wordSemanticRole;
	private String syntacticFunction;
	private String owlType;
	private OWLObject ontologyObject;
	private List<OWLAxiom> restrictions;
	
	public void clear(){
		this.word = null;
		this.wordType = null;
		this.ontologyObject = null;
		this.owlType = null;
		this.wordSemanticRole = null;
		this.restrictions = null;
	}
	
	public QATokenModel() {
		restrictions = new ArrayList<OWLAxiom>();
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
	
	public void setOWLType(String semanticType){
		this.owlType = semanticType;
	}
	
	public String getOWLType(){
		return this.owlType;
	}
	
	public void setWord(String word){
		this.word = word;
	}
	
	public String getWord(){
		return this.word;
	}
	
	public void setWordType(String type){
		this.wordType = type;
	}
	
	public String getWordType(){
		return this.wordType;
	}
	
	public void setOntologyObject(OWLObject obj){
		this.ontologyObject = obj;
	}
	
	public OWLObject getOntologyObject(){
		return this.ontologyObject;
	}
	
	public void setRestriction(OWLAxiom axiom){
		restrictions.add(axiom);
	}
	
	public void setRestriction(Set<OWLAxiom> axioms){
		for(OWLAxiom axiom: axioms){
			restrictions.add(axiom);
		}
	}
	
	public List<OWLAxiom> getRestriction() {
		return this.restrictions;
	}
	
	public boolean isEmpty(){
		return word == null;
	}
}
