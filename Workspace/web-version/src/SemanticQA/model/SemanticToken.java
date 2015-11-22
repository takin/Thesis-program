package SemanticQA.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;

public class SemanticToken extends Token {

	private String OWLType;
	private OWLObject OWLPath;
	private List<OWLAxiom> OWLRestrictions;

	public void setOWLType(String type) {
		this.OWLType = type;
	}

	public void setOWLPath(OWLObject entity) {
		this.OWLPath = entity;
	}

	public void setOWLRestriction(Set<OWLAxiom> axioms) {
		for(OWLAxiom axiom: axioms){
			setOWLRestriction(axiom);
		}
	}

	public String getOWLType() {
		return this.OWLType;
	}

	public OWLObject getOWLPath() {
		return this.OWLPath;
	}

	public void setOWLRestriction(OWLAxiom axiom) {
		if ( this.OWLRestrictions == null ) {
			this.OWLRestrictions = new ArrayList<OWLAxiom>();
		}
		this.OWLRestrictions.add(axiom);
	}

	public OWLAxiom getOWLRestriction(int index) {
		return OWLRestrictions.get(index);
	}
	
	@Override
	public void clear() {
		super.clear();
		OWLPath = null;
		OWLType = null;
		OWLRestrictions.clear();
	}
	
	@Override
	public boolean isEmpty() {
		return OWLPath == null && OWLType == null && OWLRestrictions.isEmpty() && super.isEmpty();
	}
}
