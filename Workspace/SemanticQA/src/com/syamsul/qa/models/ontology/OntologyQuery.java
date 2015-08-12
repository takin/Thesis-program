/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.syamsul.qa.models.ontology;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.syamsul.qa.helpers.TaskListener;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

/**
 *
 * @author syamsul
 */
public class OntologyQuery {
    
    private static OWLOntology ontology;
    private static OWLReasoner reasoner;
    /**
     * ArrayList untuk menampung seluruh namespace yang terdapat di dalam ontologi
     * namespace ini nantinya digunakan sebagi prefix dalam pembentukan query
     */
    private static Map<String,List<String>> PREFIX;
    
    private static final String C_TYPE_CLASS = "class";
    private static final String C_TYPE_INDIVIDUAL = "instance";
    private static final String C_TYPE_OBJECT_PROPERTY = "objectProperty";
    private static final String C_TYPE_DATATYPE_PROPERTY = "datatypeProperty";
    
    @SuppressWarnings("unused")
	private static final String CLAUSE_TYPE = "Type";
    @SuppressWarnings("unused")
	private static final String CLAUSE_CLASS = "Class";
    
    public interface Listener extends TaskListener{
    	public void onQueryExecuted(String result);
    }
    
    public OntologyQuery(OWLOntology ontology, OWLReasoner reasoner){
        OntologyQuery.PREFIX = new HashMap<>();
        OntologyQuery.ontology = ontology;
        OntologyQuery.reasoner = reasoner;
    }
    
    /**
     * Static method sebagai entry point untuk melakukan proses query
     * @param ontology -> ontologi yang akan di query
     * @param reasoner -> reasoner yang digunakan sebagai reasoning engine 
     * pada saat sparqldl api melakukan query
     * @return Object OntologyQuery
     */
    public static OntologyQuery build(OWLOntology ontology, OWLReasoner reasoner){
        return new OntologyQuery(ontology, reasoner);
    }
    
    /**
     * Method utama untuk melakukan proses query
     * methhod ini digunakan ketika menggunakan Pub/Sub pattern
     * 
     * @param listener objek listener yang akan menerima setiap hasil proses
     */
    @SuppressWarnings("unused")
	public static void getResult(Listener listener){
        
        // siapkan prefix terlebih dahulu
        buildPrefix();
        
        List<String> namespaces = PREFIX.get(C_TYPE_INDIVIDUAL);
        
//        for(String ns: namespaces){
            try {
                Query q = Query.create(
                        "PREFIX a: <http://id.dbpedia.org/resource/>\n" +
                        "PREFIX b: <http://www.ntbprov.go.id/semweb/resource/>\n" +
                        "PREFIX c: <http://www.w3.org/ns/org#>\n" +
                        "SELECT * WHERE { Type(?x, a:Bupati) }"
                );

                QueryEngine qe = QueryEngine.create(ontology.getOWLOntologyManager(), reasoner);

                QueryResult qr = qe.execute(q);
                
                if(!qr.isEmpty()){
                    listener.onQueryExecuted(qr.toJSON());
                } else {
                    listener.onQueryExecuted("query return empty result");
                }

            } catch (QueryParserException | QueryEngineException ex) {
                listener.onTaskFail(OntologyQuery.class.getName(), "error: " + ex.getMessage());
            }
//        }
        
    }
    
    @SuppressWarnings("unused")
	private static String buildQuery(String ns, String criteria, String value){
        
        String query = "PREFIX qa: " + ns + "\n" +
                       "SELECT ?p WHERE { " + criteria + "(?p, qa:" + value + ") }";
        System.out.println(query);
        return query;
    }
    
    /**
     * Method untuk melakukan proses pebentukan PREFIX yang nantinya 
     * akan digunakan dalam pembentukan query sqprql dl 
     * 
     * PREFIX diambil dari semua URI dari ontologi, baik itu Class, Datatype
     * Object property maupun instance
     */
    private static void buildPrefix(){
        
        // Mula-mula ambil semua property dari ontologi dan bentuk menjadi array
        Object[] classes = ontology.getClassesInSignature().toArray();
        Object[] datatypeProperties = ontology.getDataPropertiesInSignature().toArray();
        Object[] objectProperties = ontology.getObjectPropertiesInSignature().toArray();
        Object[] instances = ontology.getIndividualsInSignature().toArray();
        
        /**
         * buat sebah array list temporary dimana list ini berfungsi untuk 
         * menyatukan semua array objek dari ontologi
         */
        List<Object> temporaryList = new ArrayList<>();
        
        // isikan arraylist dengan semua objek dari ontologi
        temporaryList.addAll(Arrays.asList(classes));
        temporaryList.addAll(Arrays.asList(datatypeProperties));
        temporaryList.addAll(Arrays.asList(objectProperties));
        temporaryList.addAll(Arrays.asList(instances));
        
        /** 
         * --------------------------------------------------------------------
         * Selanjutnya laukan iterasi pada masing-masing objek di dalam array
         * untuk dilakukan pengecekan namespace pada objeck yang bersangkutan
         * 
         * Namespace dalam pembuatan ontologi biasanya diakhiri dengan tanda
         * slash (/) ataupun hash (#), setelah tanda slash terakhir atau tanda
         * hash merupakan nama atau asserted name dari objek yang bersangkutan 
         * misalnya http://id.dbpedia.org/resource/Bupati
         * atau http://www.w3.org/ns#Person
         * ---------------------------------------------------------------------
         */
        
        List<String> classNamespace = new ArrayList<>();
        List<String> opNamespace = new ArrayList<>();
        List<String> dpNamespace = new ArrayList<>();
        List<String> indvNamespace = new ArrayList<>();
        
        for(Object o: temporaryList){
            
            // konversi objek menjadi string
            String iri = o.toString();
            
            Pattern p = Pattern.compile("^(<http://)");
            Matcher m = p.matcher(iri);
            
            if(m.find()){
            
                /**
                 * ambil index terakhir dari slash dan hash di dalam string URI
                 * dengan menggunakan fungsi lastIndexOf dimana fungsi ini akan 
                 * mengembalikan no index dari string dan akan mengembalikan -1 
                 * dalam string tidak ditemukan karakter yang dicari
                 */
                int slashStyle = iri.lastIndexOf("/");
                int hashStyle = iri.lastIndexOf("#");

                /**
                 * index yang akan digunakan sebagai pembatas akhir dari pemotongan string
                 * 
                 * inisal nilanya diberi 1 karena bentuk iri dari hasil query
                 * OWL API adalah <http://....> dimana terdapat tanda "lebih kecil"
                 * di awal, sehingg nantinya apabila misalnya terdapat
                 * bentuk <http://id.dbpedia.org/resource/Bupati> maka hasil pemotongannya 
                 * adalah no index + 1 sehingga menjadi <http://id.dbpedia.org/resource/
               */
                int lastIndex = 1;

                /**
                 * Dalam kasus dimana pembatas URI dengan nama objeknya menggunakan 
                 * tanda hash maka nilai slashStyle dan hashStyle sama-sama true 
                 * (tidak mengembalikan -1), sehingga apabila keduanya bernilai true
                 * maka yang digunakan sebagai pemnatas akhir adalah nilai dari hashStyle
                 * karena hashStyle sudah pasti merupakan bagian yang paling akhir
                 * 
                 * misal http://www.w3.org/ns/org#Person jika menggunakan nilai dari 
                 * slashStyle, maka hasil pemotongannya akan menjadi http://www.w3.org/ns/
                 * dimana ketika digunakan menjadi prefix, namespace ini salah
                 * sedangkan jika menggunakan nilai hashStyle, maka hasil pemotongannya
                 * menjadi http://www.w3.org/ns/org#
                 */
                lastIndex += (slashStyle != -1 && hashStyle != -1) ? hashStyle : (slashStyle != -1) ? slashStyle : 1;

                /**
                 * Potong string mulai dari index ke 0 hingga lastIndex
                 * Hasil pemotongan ini akan berbentuk URI yang diawali dengan tanda
                 * "lebih kecil", untuk itu sebelum dimasukkan ke dalam arraylist
                 * PREFIX, tambahkan tanda "lebih besar" di akhir sehingga nantinya
                 * ketika akan digunakan sebagai prefix, kita tidak perlu menambahkan
                 * hal tersebut lagi
                 */
                String pref = iri.substring(0, lastIndex) + ">";

                
                if((o instanceof OWLClassImpl) && (!classNamespace.contains(pref))){
                    classNamespace.add(pref);
                } 
                
                if((o instanceof OWLObjectPropertyImpl) && (!opNamespace.contains(pref))){
                    opNamespace.add(pref);
                }
                
                if((o instanceof OWLDataPropertyImpl) && (!dpNamespace.contains(pref))){
                    dpNamespace.add(pref);
                }
                
                if((o instanceof OWLNamedIndividualImpl) && (!indvNamespace.contains(pref))) {
                    indvNamespace.add(pref);
                }
            }
        }
        
        PREFIX.put(C_TYPE_CLASS, classNamespace);
        PREFIX.put(C_TYPE_OBJECT_PROPERTY, opNamespace);
        PREFIX.put(C_TYPE_DATATYPE_PROPERTY, dpNamespace);
        PREFIX.put(C_TYPE_INDIVIDUAL, indvNamespace);
    }
    
}
