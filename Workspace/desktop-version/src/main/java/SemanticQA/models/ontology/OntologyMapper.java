package SemanticQA.models.ontology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import SemanticQA.constant.Ontology;
import SemanticQA.constant.Token;
import SemanticQA.helpers.StringManipulation;
import SemanticQA.models.nlp.QuestionModel;
import SemanticQA.models.nlp.TokenModel;


public class OntologyMapper extends OntologyLoader {
	
	private ShortFormProvider shortForm = new SimpleShortFormProvider();
	private List<QuestionModel> questionModel;
	
	public OntologyMapper(List<QuestionModel> model) {
		this.questionModel = model;
	}
	
	public List<QuestionModel> map(){
		
		for ( int i = 0; i < this.questionModel.size(); i++ ) {
			
			QuestionModel m = this.questionModel.get(i);
			
			if (m.getType().equals(Token.TYPE_PRONOMINA) ||
					m.getType().equals(Token.TYPE_FRASA_PRONOMINAL) ||
					m.getType().equals(Token.TYPE_KONJUNGSI) ) {
				continue;
			}
			
			List<TokenModel> originalConstituents = m.getConstituents();
			
			if ( originalConstituents.size() > 0 ) {
				List<TokenModel> constituents = checkType(new ArrayList<String>(), new ArrayList<TokenModel>(), originalConstituents);
				
				/**
				 * Oleh karena proses mapping dalam method checkType dimulai dari 
				 * token yang paling belakang, maka hasil proses mappingnya akan terbalik
				 * sehingga perlu di balik untuk mendapatkan urutan aslinya.
				 */
				Collections.reverse(constituents);
				/**
				 * Ganti isi arraylist konstituen yang lama dengan konstituen yang sudah di mapping
				 * hal ini harus dilakukan karena ada kemungkinan beberapa konstituen
				 * digabungkan menjadi satu, misalnya:
				 * 
				 * lombok + timur, dalam proses mapping menjadi satu yaitu lombok_timur
				 */
				m.replaceConstituent(constituents);
			}
			
			this.questionModel.set(i, m);
		}
		
		
		return this.questionModel;
	}
	
	private List<TokenModel> checkType(List<String> previousTokens, List<TokenModel> res, List<TokenModel> data) {
		
		TokenModel m = data.remove(data.size() - 1);
		TokenModel lastInserted = res.size() > 0 ? res.get(res.size() - 1) : null;
		
		String token = m.getToken();
		String tipe = getType(token);
		
		System.out.println("current -> " + token); 
		System.out.print("prev[" + previousTokens.size() + "] => ");
		for(String x:previousTokens) {
			if (x == previousTokens.get(previousTokens.size() - 1)) {
				System.out.print(x);
			} else {
				System.out.print(x+", ");
			}
		}
		System.out.println("");
		
		if ( tipe == null ) {
			
			if ( !previousTokens.isEmpty() ) {
				
				String newToken = token + "_" + String.join("_", previousTokens);
				
				tipe = getType(newToken);
				
				if ( tipe != null ) {
					
					m.setToken(newToken);
					m.setTokenOWLType(tipe);
					
					if (lastInserted != null && previousTokens.contains(lastInserted.getToken())){
						res.set(res.size() - 1, m);
					} else {
						res.add(m);
					}
					
					previousTokens.add(token);
				} else {
					
					/**
					 * Jika setelah penggabungan tetap tidak ditemukan mappingnya,
					 * maka cek terlebih dahulu apakah member dari previousTokens
					 * ada di dalam array res.
					 * 
					 * Jika ada, artinya previous item pernah digunakan untuk menyambung
					 * maka kosongkan array previousTokens karena kata tersebut sudah 
					 * tidak mungkin memiliki mapping lagi di dalam ontologi.
					 */
					String prevTokenJoined = String.join("_", previousTokens);
					if ( lastInserted != null && lastInserted.getToken().equals(prevTokenJoined) ) {
						System.out.println("must cleared");
						previousTokens.clear();
					}
				}
			}
			
			previousTokens.add(token);
		} 
		// Jika proses mapping berhasil
		else {
			
			m.setTokenOWLType(tipe);
			
			if ( previousTokens.isEmpty() ) {
				
				res.add(m);
				previousTokens.add(token);
				
			} else {
				
				String newToken = token + "_" + String.join("_", previousTokens);
				
				System.out.println(newToken);
				
				tipe = getType(newToken);
				
				if ( tipe != null ) {
					
					m.setToken(newToken);
					
					m.setTokenOWLType(tipe);
					
					if (lastInserted != null && previousTokens.contains(lastInserted.getToken())){
						res.set(res.size() - 1, m);
					} else {
						res.add(m);
					}
					
					Collections.reverse(previousTokens);
					previousTokens.add(token);
					
				} else {
					res.add(m);
					previousTokens.clear();
					previousTokens.add(token);
				}
			}
		}
		
		System.out.println("");
		
		if ( data.size() > 0 ) {
			Collections.reverse(previousTokens);
			checkType(previousTokens, res, data);
		}
		
		return res;
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
