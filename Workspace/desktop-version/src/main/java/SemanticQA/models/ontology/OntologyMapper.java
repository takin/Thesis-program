package SemanticQA.models.ontology;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import SemanticQA.constant.Ontology;
import SemanticQA.helpers.StringManipulation;


public class OntologyMapper extends OntologyLoader {
	
	private ShortFormProvider shortForm = new SimpleShortFormProvider();

	public OWLOntology getOntology(){
		return ontology;
	}
	
	public String getShortForm(OWLEntity e){
		return shortForm.getShortForm(e);
	}
	
	public OWLObject getOWLObject(String name, String type){
		
		switch(type){
		case Ontology.TYPE_CLASS:
			for(OWLClass obj: super.ontology.getClassesInSignature()){
				if(getShortForm(obj).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		case Ontology.TYPE_OBJECT_PROPERTY:
			for(OWLObjectProperty obj: super.ontology.getObjectPropertiesInSignature()){
				if(getShortForm(obj).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		case Ontology.TYPE_DATATYPE_PROPERTY:
			for(OWLDataProperty obj: super.ontology.getDataPropertiesInSignature()){
				if(getShortForm(obj).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		case Ontology.TYPE_INDIVIDUAL:
			for(OWLNamedIndividual obj: super.ontology.getIndividualsInSignature()){
				if(getShortForm(obj).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		}
		
		return null;
	}
	
	public String getType(String prop){
		if(isClass(prop)){
			return Ontology.TYPE_CLASS;
		}
		
		if(isDatatypeProperty(prop)){
			return Ontology.TYPE_DATATYPE_PROPERTY;
		}
		
		if(isObjectProperty(prop)){
			return Ontology.TYPE_OBJECT_PROPERTY;
		}
		
		if(isIndividual(prop)){
			return Ontology.TYPE_INDIVIDUAL;
		}
		
		return null;
	}
	
	public boolean isClass(String prop){
		
		if(isURI(prop)){
			for(OWLClass cls: super.ontology.getClassesInSignature()){
				if(cls.getIRI().toString().equals(prop)){
					return true;
				}
			}
		} else {
			prop = StringManipulation.concate(prop, StringManipulation.MODEL_UNDERSCORE);
			for(OWLClass cls: super.ontology.getClassesInSignature()){
				if(shortForm.getShortForm(cls).equals(prop)){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isDatatypeProperty(String prop){
		
		if(isURI(prop)){
			for(OWLDataProperty dp: super.ontology.getDataPropertiesInSignature()){
				if(dp.getIRI().toString().equals(prop)){
					return true;
				}
			}
		} else {
			prop = StringManipulation.concate(prop, StringManipulation.MODEL_CAMELCASE);
			
			for(OWLDataProperty dp: super.ontology.getDataPropertiesInSignature()){
				if(shortForm.getShortForm(dp).equals(prop)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean isObjectProperty(String prop){
		
		if(isURI(prop)){
			for(OWLObjectProperty op: ontology.getObjectPropertiesInSignature()){
				if(op.getIRI().toString().equals(prop)){
					return true;
				}
			}
		} else {
			prop = StringManipulation.concate(prop, StringManipulation.MODEL_CAMELCASE);
			
			for(OWLObjectProperty op: ontology.getObjectPropertiesInSignature()){
				if(shortForm.getShortForm(op).equals(prop)){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isIndividual(String prop){
		
		if(isURI(prop)){
			for(OWLNamedIndividual in: ontology.getIndividualsInSignature()){
				if(in.getIRI().toString().equals(prop)){
					return true;
				}
			}
		} else {
			prop = StringManipulation.concate(prop, StringManipulation.MODEL_UNDERSCORE);
			
			for(OWLNamedIndividual in: ontology.getIndividualsInSignature()){
				if(shortForm.getShortForm(in).equals(prop)){
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Set<OWLClassAxiom> getRestriction(OWLClass cls){
		return ontology.getAxioms(cls);
	}
	
	public Set<OWLDataPropertyAxiom> getRestriction(OWLDataProperty dp){
		return ontology.getAxioms(dp);
	}
	
	public Set<OWLIndividualAxiom> getRestriction(OWLNamedIndividual individu){
		return ontology.getAxioms(individu);
	}
	
	public Set<OWLObjectPropertyAxiom> getRestriction(OWLObjectProperty op){
		return ontology.getAxioms(op);
	}
	
	private boolean isURI(String str){
		return str.matches("^(https?://).*");
	}
	
}
