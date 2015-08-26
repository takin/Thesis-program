package SemanticQA.models.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import SemanticQA.helpers.StringManipulation;


public class OntologyMapper extends OntologyLoader {
	
	public static final String ONTOLOGY_TYPE_CLASS = "class";
	public static final String ONTOLOGY_TYPE_OBJECT_PROPERTY = "objectProperty";
	public static final String ONTOLOGY_TYPE_DATATYPE_PROPERTY = "datatypeProperty";
	public static final String ONTOLOGY_TYPE_INDIVIDUAL = "individual";
	
	public static final String KEY_URI = "uri";
	public static final String KEY_NAME = "name";
	
	private List<Map<String,Object>> classes = new ArrayList<>();
	private List<Map<String,Object>> datatypesProperties = new ArrayList<>();
	private List<Map<String,Object>> objectProperties = new ArrayList<>();
	private List<Map<String,Object>> individuals = new ArrayList<>();
	
	public OntologyMapper() {
		super();
		
		Thread t1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(OWLClass cls: ontology.getClassesInSignature()){
					Map<String,Object> clsMap = new HashMap<>();
					clsMap.put(KEY_NAME, cls.getIRI().getFragment());
					clsMap.put(KEY_URI, cls);
					classes.add(clsMap);
				}
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(OWLDatatype dp: ontology.getDatatypesInSignature()){
					Map<String,Object> dpMap = new HashMap<>();
					dpMap.put(KEY_NAME, dp.getIRI().getFragment());
					dpMap.put(KEY_URI, dp);
					datatypesProperties.add(dpMap);
				}
			}
		});
		
		Thread t3 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(OWLObjectProperty op: ontology.getObjectPropertiesInSignature()){
					Map<String,Object> opMap = new HashMap<>();
					opMap.put(KEY_NAME, op.getIRI().getFragment());
					opMap.put(KEY_URI, op);
					objectProperties.add(opMap);
				}
			}
		});
		
		Thread t4 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(OWLNamedIndividual in: ontology.getIndividualsInSignature()){
					Map<String,Object> inMap = new HashMap<>();
					inMap.put(KEY_NAME, in.getIRI().getFragment());
					inMap.put(KEY_URI, in);
					individuals.add(inMap);
				}
			}
		});
		
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
	
	public String getType(String prop){
		if(isClass(prop)){
			return ONTOLOGY_TYPE_CLASS;
		}
		
		if(isDatatypeProperty(prop)){
			return ONTOLOGY_TYPE_DATATYPE_PROPERTY;
		}
		
		if(isObjectProperty(prop)){
			return ONTOLOGY_TYPE_OBJECT_PROPERTY;
		}
		return null;
	}
	
	public boolean isClass(String prop){
		
		prop = StringManipulation.concate(prop, "_");
		
		for(OWLClass cls: super.ontology.getClassesInSignature()){
			if(cls.getIRI().getFragment().equals(prop)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isDatatypeProperty(String prop){
		
		prop = StringManipulation.makeCamelCase(prop);
		
		for(OWLDatatype dp: super.ontology.getDatatypesInSignature()){
			if(dp.getIRI().getFragment().equals(prop)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isObjectProperty(String prop){
		
		prop = StringManipulation.makeCamelCase(prop);
		
		for(OWLObjectProperty op: ontology.getObjectPropertiesInSignature()){
			if(op.getIRI().getFragment().equals(prop)){
				return true;
			}
		}
		return false;
	}
	
}
