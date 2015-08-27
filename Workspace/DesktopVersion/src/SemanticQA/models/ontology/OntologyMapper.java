package SemanticQA.models.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import SemanticQA.constant.Ontology;
import SemanticQA.helpers.StringManipulation;


public class OntologyMapper extends OntologyLoader {
	
	private List<Map<String,Object>> classes = new ArrayList<>();
	private List<Map<String,Object>> datatypesProperties = new ArrayList<>();
	private List<Map<String,Object>> objectProperties = new ArrayList<>();
	private List<Map<String,Object>> individuals = new ArrayList<>();
	
	public OntologyMapper() {
		super();
		
		Thread t1 = new Worker(Ontology.KEY_TYPE_CLASS);
		Thread t2 = new Worker(Ontology.KEY_TYPE_DATATYPE_PROPERTY);
		Thread t3 = new Worker(Ontology.KEY_TYPE_INDIVIDUAL);
		Thread t4 = new Worker(Ontology.KEY_TYPE_OBJECT_PROPERTY);
				
		t1.start();
		t2.start();
		t3.start();
		t4.start();
	}

	public OWLOntology getOntology(){
		return ontology;
	}
	
	public List<Map<String, Object>> getClasses(){
		return classes;
	}
	
	public Map<String,Object> getOWLObject(String name){
		
		for(Map<String,Object> obj: classes){
			if(obj.get(Ontology.KEY_OBJECT_NAME).toString().toLowerCase().equals(name.toLowerCase())){
				return obj;
			}
		}
		
		return null;
	}
	
	public String getType(String prop){
		
		if(isClass(prop)){
			return Ontology.KEY_TYPE_CLASS;
		}
		
		if(isDatatypeProperty(prop)){
			return Ontology.KEY_TYPE_DATATYPE_PROPERTY;
		}
		
		if(isObjectProperty(prop)){
			return Ontology.KEY_TYPE_OBJECT_PROPERTY;
		}
		
		if(isIndividual(prop)){
			return Ontology.KEY_TYPE_INDIVIDUAL;
		}
		
		return null;
	}
	
	public boolean isClass(String prop){
		
		prop = StringManipulation.concate(prop, StringManipulation.MODEL_UNDERSCORE);
		
		for(OWLClass cls: super.ontology.getClassesInSignature()){
			if(cls.getIRI().getFragment().equals(prop)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isDatatypeProperty(String prop){
		
		prop = StringManipulation.concate(prop, StringManipulation.MODEL_CAMELCASE);
		
		for(OWLDatatype dp: super.ontology.getDatatypesInSignature()){
			if(dp.getIRI().getFragment().equals(prop)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isObjectProperty(String prop){
		
		prop = StringManipulation.concate(prop, StringManipulation.MODEL_CAMELCASE);
		
		for(OWLObjectProperty op: ontology.getObjectPropertiesInSignature()){
			if(op.getIRI().getFragment().equals(prop)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isIndividual(String prop){
		prop = StringManipulation.concate(prop, StringManipulation.MODEL_UNDERSCORE);
		
		for(OWLNamedIndividual in: ontology.getIndividualsInSignature()){
			if(in.getIRI().getFragment().equals(prop)){
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasRestriction(String type, OWLObject obj){
		
		switch (type) {
		case Ontology.KEY_TYPE_CLASS:
			return ontology.getAxioms((OWLClass) obj).size() > 0;
		case Ontology.KEY_TYPE_DATATYPE_PROPERTY:
			return ontology.getAxioms((OWLDataProperty) obj).size() > 0;
		case Ontology.KEY_TYPE_INDIVIDUAL:
			return ontology.getAxioms((OWLNamedIndividual) obj).size() > 0;
		case Ontology.KEY_TYPE_OBJECT_PROPERTY:
			return ontology.getAxioms((OWLObjectProperty) obj).size() > 0;
		}
		
		return false;
	}
	
	private class Worker extends Thread {
		
		private Thread t;
		private String type;
		
		public Worker(String type) {
			this.type = type;
			t = this;
		}
		
		@Override
		public synchronized void start() {
			super.start();
			t.run();
		}
		
		@Override
		public void run() {
			
			switch(type){
			case Ontology.KEY_TYPE_CLASS:
				for(OWLClass cls: ontology.getClassesInSignature()){
					Map<String,Object> clsMap = new HashMap<>();
					clsMap.put(Ontology.KEY_OBJECT_NAME, cls.getIRI().getFragment());
					clsMap.put(Ontology.KEY_OBJECT_URI, cls);
					classes.add(clsMap);
				}
				break;
			case Ontology.KEY_TYPE_DATATYPE_PROPERTY:
				for(OWLDatatype dp: ontology.getDatatypesInSignature()){
					Map<String,Object> dpMap = new HashMap<>();
					dpMap.put(Ontology.KEY_OBJECT_NAME, dp.getIRI().getFragment());
					dpMap.put(Ontology.KEY_OBJECT_URI, dp);
					datatypesProperties.add(dpMap);
				}
				break;
			case Ontology.KEY_TYPE_INDIVIDUAL:
				for(OWLObjectProperty op: ontology.getObjectPropertiesInSignature()){
					Map<String,Object> opMap = new HashMap<>();
					opMap.put(Ontology.KEY_OBJECT_NAME, op.getIRI().getFragment());
					opMap.put(Ontology.KEY_OBJECT_URI, op);
					objectProperties.add(opMap);
				}
				break;
			case Ontology.KEY_TYPE_OBJECT_PROPERTY:
				for(OWLNamedIndividual in: ontology.getIndividualsInSignature()){
					Map<String,Object> inMap = new HashMap<>();
					inMap.put(Ontology.KEY_OBJECT_NAME, in.getIRI().getFragment());
					inMap.put(Ontology.KEY_OBJECT_URI, in);
					individuals.add(inMap);
				}
				break;
			}
			
		}
	}
	
}
