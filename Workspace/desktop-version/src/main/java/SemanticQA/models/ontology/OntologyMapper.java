package SemanticQA.models.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import SemanticQA.constant.Ontology;
import SemanticQA.helpers.StringManipulation;


public class OntologyMapper extends OntologyLoader {
	
	private List<Map<String,Object>> classes = new ArrayList<>();
	private List<Map<String,Object>> datatypesProperties = new ArrayList<>();
	private List<Map<String,Object>> objectProperties = new ArrayList<>();
	private List<Map<String,Object>> individuals = new ArrayList<>();
	private ShortFormProvider shortForm = new SimpleShortFormProvider();
	
	public OntologyMapper() {
		super();
		loadEntitiy();
	}

	public OWLOntology getOntology(){
		return ontology;
	}
	
	public String getShortForm(OWLEntity e){
		return shortForm.getShortForm(e);
	}
	
	public List<Map<String, Object>> getClasses(){
		return classes;
	}
	
	public Map<String,Object> getOWLObject(String name, String type){
		
		switch(type){
		case Ontology.TYPE_CLASS:
			for(Map<String,Object> obj: classes){
				if(obj.get(Ontology.KEY_OBJECT_NAME).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		case Ontology.TYPE_OBJECT_PROPERTY:
			for(Map<String,Object> obj: objectProperties){
				if(obj.get(Ontology.KEY_OBJECT_NAME).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		case Ontology.TYPE_DATATYPE_PROPERTY:
			for(Map<String,Object> obj: datatypesProperties){
				if(obj.get(Ontology.KEY_OBJECT_NAME).toString().toLowerCase().equals(name.toLowerCase())){
					return obj;
				}
			}
		case Ontology.TYPE_INDIVIDUAL:
			for(Map<String,Object> obj: individuals){
				if(obj.get(Ontology.KEY_OBJECT_NAME).toString().toLowerCase().equals(name.toLowerCase())){
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
	
	public boolean hasRestriction(String type, OWLObject obj){
		
		switch (type) {
		case Ontology.TYPE_CLASS:
			return ontology.getAxioms((OWLClass) obj).size() > 0;
		case Ontology.TYPE_DATATYPE_PROPERTY:
			return ontology.getAxioms((OWLDataProperty) obj).size() > 0;
		case Ontology.TYPE_INDIVIDUAL:
			return ontology.getAxioms((OWLNamedIndividual) obj).size() > 0;
		case Ontology.TYPE_OBJECT_PROPERTY:
			return ontology.getAxioms((OWLObjectProperty) obj).size() > 0;
		}
		
		return false;
	}
	
	private void loadEntitiy(){
			
		for(OWLClass cls: ontology.getClassesInSignature()){
			Map<String,Object> clsMap = new HashMap<>();
			clsMap.put(Ontology.KEY_OBJECT_NAME, shortForm.getShortForm(cls));
			clsMap.put(Ontology.KEY_OBJECT_URI, cls);
			classes.add(clsMap);
		}

		for(OWLDatatype dp: ontology.getDatatypesInSignature()){
			Map<String,Object> dpMap = new HashMap<>();
			dpMap.put(Ontology.KEY_OBJECT_NAME, shortForm.getShortForm(dp));
			dpMap.put(Ontology.KEY_OBJECT_URI, dp);
			datatypesProperties.add(dpMap);
		}

		for(OWLObjectProperty op: ontology.getObjectPropertiesInSignature()){
			Map<String,Object> opMap = new HashMap<>();
			opMap.put(Ontology.KEY_OBJECT_NAME, shortForm.getShortForm(op));
			opMap.put(Ontology.KEY_OBJECT_URI, op);
			objectProperties.add(opMap);
		}

		for(OWLNamedIndividual in: ontology.getIndividualsInSignature()){
			Map<String,Object> inMap = new HashMap<>();
			inMap.put(Ontology.KEY_OBJECT_NAME, shortForm.getShortForm(in));
			inMap.put(Ontology.KEY_OBJECT_URI, in);
			individuals.add(inMap);
		}
	}
	
	private boolean isURI(String str){
		return str.matches("^(https?://).*");
	}
	
}
