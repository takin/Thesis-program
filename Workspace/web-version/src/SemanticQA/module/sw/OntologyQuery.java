/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SemanticQA.module.sw;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryArgument;
import de.derivo.sparqldlapi.QueryBinding;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import SemanticQA.constant.Ontology;
import SemanticQA.constant.Type;
import SemanticQA.model.QueryResultData;
import SemanticQA.model.QueryResultModel;
import SemanticQA.model.SemanticToken;
import SemanticQA.model.Sentence;

/**
 *
 * @author syamsul
 */
public class OntologyQuery {

	private QueryEngine queryEngine;
	private String queryPattern = "";
	private static final String DBPEDIA_PROPERTY_FILTER = "(id.dbpedia.org/property|(rdf-schema#[(comment|label)])|depiction)";
	
	public static abstract class Key {
		public abstract class Result {
			public static final String DATA = "data";
			public static final String OBJECT = "object";
		}
		public abstract class InferedItem {
			public static final String TYPE = "type";
			public static final String URI = "uri";
		}
	}
	
	///////////////////////////
	// variabel untuk menyimpan URI dari subjek pertanyaan
	// URI ini nantinya akan digunakan untuk melakukan query informasi tambahan 
	// tentang subjek yang bersangkutan
	// URI ini didapatkan pada saat proses pembentukan query dalam method buidQuery()
	////////////////////////////////////////////////
	private Map<String, String> inferredItem;
	private OWLReasoner reasoner;
	private OntologyMapper mapper;
	
	public OntologyQuery(OntologyMapper mapper, OWLReasoner reasoner) {
		this.queryEngine =  QueryEngine.create(mapper.ontology.getOWLOntologyManager(), reasoner);
		this.reasoner = reasoner;
		this.mapper = mapper;
		inferredItem = new HashMap<String, String>();
	}
	
	public Map<String, List<? extends QueryResultModel>> execute(List<Sentence> model) throws Exception {
		
		Map<String, List<? extends QueryResultModel>> result = new HashMap<String, List<? extends QueryResultModel>>();
		
		List<QueryResultData> listOfQueryResultData = new ArrayList<QueryResultData>();
		final List<QueryResultModel> listOfQueryResultObject = new ArrayList<QueryResultModel>();
		
		Set<OWLNamedIndividual> boundedIndividuals = new HashSet<OWLNamedIndividual>();
		
		QueryResult sparqldlQueryResult = null;
		
		try {
			Query query = buildQuery(model);
			//////////////////////////////////////////////////
			// Do PARQL-DL Query							//
			//////////////////////////////////////////////////
			sparqldlQueryResult = queryEngine.execute(query);
			
			for ( QueryBinding queryBinding : sparqldlQueryResult ) {
				//////////////////////////////////////////////////////
				// ambil semua variabel binding dari query sparqldl	//
				//////////////////////////////////////////////////////
				Set<QueryArgument> args = queryBinding.getBoundArgs();
				
				for ( QueryArgument arg:args ) {
					
					QueryArgument item = queryBinding.get(arg);
					
					if ( item.isURI() && arg.getValue().matches("(sub|ob)ject")) {		
						
						String itemValue = item.getValue();
						////////////////////////////////////
						// Bentuk OWLNamedIndividual untuk mencari individual yang ber-relasi sameAs dengan 
						// individual ini
						////////////////////////////////////
						IRI currentIndividualIRI = IRI.create(itemValue);
						OWLNamedIndividual currentIndividual = mapper.dataFactory.getOWLNamedIndividual(currentIndividualIRI);
						
						//////////////////////////////
						// Proses pencarian data individual hasil query SPARQL-DL dengan cara melakukan query SPARQL
						// pada method doSPARQLQuery() dilakukan dengan cara terlebih dahulu mencari semua individual 
						// yang sama (owl:sameAs). 
						// Jika terdapat individual yang sama maka semua individual yang sama dilakukan proses query
						// SPARQL secara bersamaan sehingga menghasilkan satu buah objek QueryResultModel yang berisi 
						// properti gabungan dari semua individual.
						//
						// Karena hasil query SPARQL-DL juga akan mengembalikan individual yang sama (same as) 
						// maka untuk menghidari terjadinya duplikasi data untuk 2 buah individual yang memiliki 
						// properti same as, lakukan filtering terlebih dahulu dengan mengecek isi array boundedIndividual
						// jika URI individual yang bersangkutan sudah ada, maka abaikan karena datanya sudah ada
						//
						// Contoh:
						// misal hasil query SPARQL-DL adalah sbb:
						// ?object = http://id.dbpedia.org/resource/Kabupaten_Lombok_Timur
						// ?object = http://semanticweb.techtalk.web.id/dataset#Lombok_Timur
						//
						// kedua objek tersebut memiliki property same as, pada saat iterasi pertama dimana yang akan di proses 
						// adalah http://id.dbpedia.org/resource/Kabupaten_Lombok_Timur, pada saat proses pencarian 
						// sameAs individual melalui reasoner akan menemukan  http://semanticweb.techtalk.web.id/dataset#Lombok_Timur
						// sehingga kedua URI ini akan di proses (doSPARQLQuery()) secara bersamaan, nah pada iterasi selanjutnya
						// dimana URI yang akan di proses adalah http://semanticweb.techtalk.web.id/dataset#Lombok_Timur juga akan 
						// menemukan http://id.dbpedia.org/resource/Kabupaten_Lombok_Timur, sehingga seharusnya iterasi ke-dua diabaikan
						// agar tidak terjadi duplikasi data.
						/////////////////////////////
						if ( !boundedIndividuals.contains(currentIndividual) ) {
							//////////////////////////////////////////////////////////////////
							// Ambil list individual yang sama dengan individual saat ini	//
							//////////////////////////////////////////////////////////////////
							final Set<OWLNamedIndividual> listOfSameIndividuals = reasoner.getSameIndividuals(currentIndividual).getEntities();
							
							//////////////////////////////////////////////////////////////////////////////////////
							// Masukkan semua individual yang ditemukan di dalam proses getSameIndividual()		//
							// sehingga jika terdapat individual yang sama, proses pencarain data dapat di skip	//
							//////////////////////////////////////////////////////////////////////////////////////
							boundedIndividuals.addAll(listOfSameIndividuals);
							
							//////////////////////////////////////////////////////////////////////////////////////////////
							// Siapkan objek QueryResultData															//
							// Objek ini akan menyimpan hasil query sparql yang berupa property dan nilai propertynya	//
							//////////////////////////////////////////////////////////////////////////////////////////////
							final QueryResultData resultModel = new QueryResultData();
							
							//////////////////
							// Karena tingkat akurasi DBPedia lebih rendah dari tingkat akurasi dari ontologi yang di kembangkan 
							// sendiri, maka jika individual yang sedang di proses saat ini memiliki individual yang sama dengan 
							// individual yang berasa dari DBPedai, maka untuk isi dari field subject pada objek resultModel 
							// diisi dengan URI dari ontologi yang dikembangkan sendiri
							// 
							// Oleh karena proses ini memerlukan iterasi, maka untuk mencegah terjadinya blocking thread maka
							// lakukan pada thread terpisah sehingga proses pengerjaan query SPARQL tidak terganggu.
							////////////////////////////
							Runnable decidedTheSubject = new Runnable() {
								
								@Override
								public void run() {
									for (Iterator<OWLNamedIndividual> indv = listOfSameIndividuals.iterator(); indv.hasNext();) {
										OWLNamedIndividual i = indv.next();
										if ( i.toString().matches("^(<?http://semanticweb.techtalk).*") || !indv.hasNext()) {
											
											resultModel.setSubject(i.toStringID());
											Set<OWLClass> individualTypes = reasoner.getTypes(i, true).getFlattened();
										
											for ( OWLClass indvidualType:individualTypes ) {
												
												QueryResultModel classOfIndividualModel = new QueryResultModel();
												QueryResultModel individualModel = new QueryResultModel();
												classOfIndividualModel.setObject(indvidualType.toStringID());
												individualModel.setObject(i.toStringID());
												
												//////////////////////////////////////////////////////////////////////////////////////////////////
												// Khusus untuk query pattern CI misalnya "siapakah bupati kabupaten lombok timur"				//
												// jangan sertakan nama kelas ke dalam daftar summry text agar teks jawaban menjadi 			//
												// lebih natural -> "bupati kabupaten lombok timur adalah ali bin dahlan", jika tidak 			//
												// dibuang, maka akan menghasilkan "bupati kabupaten lombok timur adalah bupati ali bin dahlan"	//
												//////////////////////////////////////////////////////////////////////////////////////////////////
												if ( !queryPattern.matches("CI") ) {
													listOfQueryResultObject.add(classOfIndividualModel);
												}
												//////////
												// masukkan daftar hasil query sparql untuk masing-masing individu ke dalam 
												// array list queryResultObject
												///////////
												listOfQueryResultObject.add(individualModel);
												
												break;
											}
											break;
										}
									}
								}
							};
							
							Thread decideTheSubjectThread = new Thread(decidedTheSubject);
							decideTheSubjectThread.start();
							//////////////////////////////////////////////////////////////////////////////////////////////////////
							// Meskipun individual yang saat ini tidak memiliki individual yang ber-relasi owl:sameAs namun 	//
							// method getSameIndividual().getEntities() minimal akan mengembalikan satu nilai yaitu 			//
							// individual yang saat ini di proses.																//
							//																									//
							// Set tidak langsung dikirimkan ke methd doSPARQLQuery() karena perlu diurutkan terlebih dahulu	//
							// yaitu urutannya adalah variabel ?subject di proses terlebih dahulu 								//
							//////////////////////////////////////////////////////////////////////////////////////////////////////
							LinkedHashMap<String, String> sparqlResult = doSPARQLQuery(listOfSameIndividuals);
							decideTheSubjectThread.join();
							resultModel.addData(sparqlResult);
							
							//////////////////////////////////////////////////////////////////////////////////////
							// Jika individual berasal dari variabel ?subject maka pastikan ia berada 			//
							// di array paling depan supaya hasil summryText sesuai dengan konteks pertanyaan	//
							//////////////////////////////////////////////////////////////////////////////////////
							if ( arg.getValue().equals("subject") ) {
								listOfQueryResultData.add(0, resultModel);
							} else {
								listOfQueryResultData.add(resultModel);
							}
						}
					}
				}
			}
			
			String item = inferredItem.get(Key.InferedItem.URI);
			String ii = item.substring(1, item.length() - 1);
			IRI iiIRI = IRI.create(ii);
			
			OWLNamedIndividual mainIndividu = mapper.dataFactory.getOWLNamedIndividual(iiIRI);
			Set<OWLNamedIndividual> mainIndividuals = reasoner.getSameIndividuals(mainIndividu).getEntities();
			
			LinkedHashMap<String, String> qr = doSPARQLQuery(mainIndividuals);
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Khusus untuk model query PS atau SP yang hanya memiliki mapping Individul (tanpa ada kelas atau properti)	//
			// misalnya "siapakah ali bin dahlan", maka tambahkan nama kelas ke dalam array listOfQueryResultObject			//
			// Hal ini dilakukan karena hasil summery text akan menjadi "ali bin dahlan adalah kabupaten lombok timur"		//
			// karena tidak ada nama kelas. sehingga untuk memperbaikinya maka harus ditambahkan 							//
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if ( queryPattern.matches("I") ) {
				QueryResultModel m = new QueryResultModel();
				Set<OWLClass> classes = reasoner.getTypes(mainIndividu, true).getFlattened();
				for ( OWLClass c:classes ) {
					m.setObject(c.toStringID());
					break;
				}
				
				listOfQueryResultObject.add(0, m);
			}
			
			QueryResultData mainItemModel = new QueryResultData();
			mainItemModel.addData(qr);
			mainItemModel.setSubject(ii);
			
			if ( inferredItem.get(Key.InferedItem.TYPE).equals("subject") ){
				listOfQueryResultData.add(0, mainItemModel);
			} else {
				listOfQueryResultData.add(mainItemModel);			
			}
			
			result.put(Key.Result.DATA, listOfQueryResultData);
			result.put(Key.Result.OBJECT, listOfQueryResultObject);			
			
		} catch (QueryParserException | QueryEngineException e) {
			throw new Exception("Tidak dapat melakukan query terhadap basis pengetahuan!");
		}
		
		
		return result;
	}
	
	private LinkedHashMap<String, String> doSPARQLQuery(Set<OWLNamedIndividual> individuals) throws Exception {
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		
		Repository repo = null;
		RepositoryConnection repositoryConnection = null;
		String query = "";
		
		for ( OWLNamedIndividual individual : individuals ) {
			
			String path = individual.toString();
			
			if ( path.matches("^(<http://id.dbpedia.org).*") ) {
				
				try {
					repo = new SPARQLRepository(Ontology.Path.DBPEDIA_ENDPOINT);
					repo.initialize();
					repositoryConnection = repo.getConnection();
					///////////////
					// objek path tidak perlu ditambahkan tanda < dan > karena
					// hasil konversi OWLNamedIndividual menjadi string sudah otomatis memiliki 
					// tanda < dan >
					////////////////					
					query = "SELECT * WHERE {\n"
							+ path + " ?prop ?value .\n"
							+ "FILTER(regex(?prop, \"" + DBPEDIA_PROPERTY_FILTER + "\", \"i\"))\n"
							+ "}";
					
				} catch (RepositoryException e) {
					throw new Exception("Gagal melakukan koneksi dengan server DBPedia");
				}
			} else {				
				try {
					repo = new SailRepository(new MemoryStore());
					repo.initialize();
					
					URL localRDFFile = new URL(Ontology.Path.DATASET);
					String localPath = "http://semanticweb.techtalk.web.id/dataset";
									
					repositoryConnection = repo.getConnection();
					repositoryConnection.add(localRDFFile, localPath, RDFFormat.TURTLE);
					
					///////////////
					// objek path tidak perlu ditambahkan tanda < dan > karena
					// hasil konversi OWLNamedIndividual menjadi string sudah otomatis memiliki 
					// tanda < dan >
					////////////////
					query = "SELECT * WHERE {\n"
							+ path + " ?prop ?value .\n"
							+ "}";
					
				} catch (Exception e) {
					throw new Exception("Gagal melakukan koneksi dengan server DATASET");
				}
			}
			
			try {
				TupleQuery tupleQuery = repositoryConnection.prepareTupleQuery(query);
				
				try(TupleQueryResult queryResult = tupleQuery.evaluate()) {
					List<String> boundVariables = queryResult.getBindingNames();
					
					if ( queryResult.hasNext() ) {
						
						while ( queryResult.hasNext() ) {
							
							BindingSet bs = queryResult.next();
							
							Value prop = bs.getValue(boundVariables.get(0));
							Value val = bs.getValue(boundVariables.get(1));
							
							////////
							// ambil hanya property yang memiliki nilai
							///////
							if ( val.stringValue().matches("[a-zA-Z0-9]+.*") ) {
								result.put(prop.stringValue(), val.stringValue());
							}
						}
					}
					
				} catch (Exception e) {
					// jangan throw exception karena jika koneksi ke DBPedia gagal
					// akan mengakibatkan gagal secara keseluruhan 
					// termasuk proses query lokal repository
				}
				
			} catch (OpenRDFException e) {
				throw new Exception("Gagal membentuk query SPARQL");
			}
			
			repositoryConnection.close();
			repo.shutDown();
		}
		
		
		return result;
	}
	
	private Query buildQuery(List<Sentence> model) throws QueryParserException {
		
		String analyzedQuery = "";
		List<OWLObject> listOfObjects = new ArrayList<OWLObject>();
		
		for ( int modelIndex = 0; modelIndex < model.size(); modelIndex++ ) {
			
			Sentence m = model.get(modelIndex);
			List<SemanticToken> tm = m.getConstituents();
			
			String prevTokenType = null;
			
			if ( !m.getType().matches("("+Type.Phrase.PRONOMINAL + "|" + Type.Token.PRONOMINA + ")") ) {
				
				for ( SemanticToken t:tm ) {
					
					switch (t.getOWLType()) {
					case Type.Ontology.CLASS:
						if ( queryPattern.matches("C")){
							
							if ( !prevTokenType.equals(Type.Token.NOMINA) ){
								listOfObjects.set(listOfObjects.size() - 1, t.getOWLPath());								
							}
							
						} else {
							queryPattern += "C";							
							listOfObjects.add(t.getOWLPath());
						}
						break;
					case Type.Ontology.OBJECT_PROPERTY:
						queryPattern += "OP";
						listOfObjects.add(t.getOWLPath());
						break;
					case Type.Ontology.DATATYPE_PROPERTY:
//						queryPattern += "DP";
//						listOfObjects.add(t.getOWLPath());
						break;
					case Type.Ontology.INDIVIDUAL:
						queryPattern += "I";
						listOfObjects.add(t.getOWLPath());
						break;
					}
					
					prevTokenType = t.getType();
				}
				

				switch (queryPattern) {
				case "CI":
					
					inferredItem.put(Key.InferedItem.TYPE, "object");
					inferredItem.put(Key.InferedItem.URI, listOfObjects.get(1).toString());
					
					analyzedQuery = "{\n"
							+ "ObjectProperty(?op),"
							+ "Type(?object," + listOfObjects.get(0) +"),\n"
							+ "PropertyValue(?object, ?op, " + listOfObjects.get(1) + ")"
							+ "\n}";
					break;
				case "OPCI":
					
					inferredItem.put(Key.InferedItem.TYPE, "subject");
					inferredItem.put(Key.InferedItem.URI, listOfObjects.get(2).toString());
					
					analyzedQuery = "{\n"
							+ "Type(" + listOfObjects.get(2) + "," + listOfObjects.get(1) + "),\n"
							+ "Transitive(" + listOfObjects.get(0) + "),\n"
							+ "PropertyValue(" + listOfObjects.get(2) + ", " + listOfObjects.get(0) + ", ?object),\n"
							+ "DirectType(?object, ?class)"
							+ "\n}";
					break;
				case "OPCOPI":
					
					inferredItem.put(Key.InferedItem.TYPE, "object");
					inferredItem.put(Key.InferedItem.URI, listOfObjects.get(3).toString());
					
					analyzedQuery = "{\n "
							+ "Type(?subject," + listOfObjects.get(1) + "),\n "
							+ "Transitive(" + listOfObjects.get(2) + "),\n"
							+ "PropertyValue(?subject, " + listOfObjects.get(2) + "," + listOfObjects.get(3)+")"
							+ "\n}";
					break;
				case "COPI":
					
					inferredItem.put(Key.InferedItem.TYPE, "object");
					inferredItem.put(Key.InferedItem.URI, listOfObjects.get(2).toString());
					
					analyzedQuery = "{\n "
							+ "Type(?subject," + listOfObjects.get(0) + "),\n "
							+ "Transitive(" + listOfObjects.get(1) + "),\n"
							+ "PropertyValue(?subject, " + listOfObjects.get(1) + "," + listOfObjects.get(2) + ")"
							+ "\n}";
					break;
				case "DPCI":
					
					inferredItem.put(Key.InferedItem.TYPE, "object");
					inferredItem.put(Key.InferedItem.URI, listOfObjects.get(2).toString());
					
					analyzedQuery = "{\n"
							+ "Type("+ listOfObjects.get(2) +"," + listOfObjects.get(1) + "),\n"
//							+ "DataProperty(" + listOfObjects.get(0) + "),"
							+ "PropertyValue(" + listOfObjects.get(2) + "," + listOfObjects.get(0) + ", ?object)"
							+ "}";
					break;
				case "COPCI":
					
					inferredItem.put(Key.InferedItem.TYPE, "object");
					inferredItem.put(Key.InferedItem.URI, listOfObjects.get(3).toString());
					
					analyzedQuery = "{\n"
							+ "Type(?subject, " + listOfObjects.get(0) + "),"
							+ "Transitive(" + listOfObjects.get(1) + "),\n"
							+ "PropertyValue(?subject, " + listOfObjects.get(1) + ", " + listOfObjects.get(3) + ")"
							+ "}";
					break;
				case "I":
					
					inferredItem.put(Key.InferedItem.TYPE, "subject");
					inferredItem.put(Key.InferedItem.URI, listOfObjects.get(0).toString());
					
					analyzedQuery = "{\n"
							+ "DirectType(" + listOfObjects.get(0) + ",?class),\n"
							+ "ObjectProperty(?op),"
							+ "PropertyValue("+ listOfObjects.get(0) +",?op, ?object)\n"
							+ "}";
					break;
				}
			}
		}

		
		String query = "select * where " + analyzedQuery;
		return Query.create(query);
	}
	
}
