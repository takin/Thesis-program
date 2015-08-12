/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SemanticQA.models.nlp;

import SemanticQA.helpers.Constant;
import SemanticQA.helpers.TaskListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/* ==============================================================================
 POSTagger merupakan bagian dari pre-process NLP
 fungsi kelas ini adalah untuk membentuk token-token dari kata yang diinputkan
 
 Token yang telah dibentuk selanjutnya akan di cek kelas katanya ke dalam 
 database SQL untuk selanjutnya digunakan untuk memberikan TAG (POS TAG)
 sehingga nantinya token tersebut dapat di proses lebih lanjut oleh parser
 
 Apabila sebuah token tidak diketahui kelas katanya, maka kata/token tersebut 
 akan diberikan tag UN (unknwon)
 * @author syamsul
 * =============================================================================*/
public class Tokenizer {
	
	public interface TokenizerListener extends TaskListener{
		public void onTokenizeSuccess(List<String> taggedToken);
	}
    
    public void tokenize(String sentence, TokenizerListener listener){
      
    	Connection conn = initDatabase(listener);
    	List<String> TOKEN = new ArrayList<>();
    	
        try{
            /* ============================================================
             * lakukan tokenisasi terhadap kalimat yang diinputkan
             * token ini nantinya akan digunakan sebagai clausa dalam SQL 
             * untuk mencari tipe kata masing-masing token
             * 
             * buat token dengan menggunakan pemisah spasi
             * ============================================================*/
            String[] token = sentence.split(" ");
            
            /* ===================================================================
             * buat query sql untuk mencari tipe kata di dalam database
             * clause IN dipilih dengan alasan agar proes query ke dalam database 
             * tidak dilakukan berulang-ulang, sehingga dapat meningkatkan 
             * performa, terutama pada query dengan jumlah token yang banyak
             * ===================================================================*/
            String SQL_QUERY = "SELECT katadasar,kode_katadasar FROM tb_katadasar WHERE katadasar IN (";
            
            /* ===============================================================
             * lakukan iterasi untuk memasukkan masing-masing kata yang akan
             * dijadikan sebagai kriteria di dalam kalusa IN
             * ===============================================================*/
            for(int i=0; i < token.length; i++){
                
                /* ===============================================================
                 * Siapkan kata yang akan dicek
                 * kata dalam database menggunakan lowercase, untuk itu pastikan 
                 * kata yang akan dimasukkan juga dalam format lowe case
                 * serta jangan lupa pula untuk melakukan trimming untuk membuang
                 * tanda baca lain seperti koma dan titik yang menjadi satu 
                 * dengan kata
                 * ===============================================================*/
                String word = token[i].toLowerCase().trim();
                
                TOKEN.add(word);
               
                /* ===============================================================
                 * Lakukan concatinate terhadap SQL_QUERY sehingga membentuk
                 * array string yang akan di cek di dalam database
                 * 
                 * Oleh karen kriteria yang akan dicek berupa string, 
                 * maka jangan lupa untuk menambahkan tanda petik (')
                 * pada setiap iterasi
                 * 
                 * Cek juga apakah posisi pointer sudah berada di akhir array 
                 * atau belum, jika belum maka tambahkan tanda koma pada setiap 
                 * akhir kata yang akan digabungkan
                 * ===============================================================*/
                SQL_QUERY += (i == (token.length - 1)) ? "'" + word + "'" : "'" + word + "',";
            }
            
            /* ===========================================================
             * setelah semua string kriteria dimasukkan, tambahkan tanda 
             * kurung tutup pada akhir query sehingga membentuk statement 
             * SQL yang utuk: Select katadasar, kode_katadasar from 
             * tb_katadasar where katadasar in ('a','b',c')
             * ===========================================================*/
            SQL_QUERY += ")";
            
            // buat statement SQL
            Statement stmt = conn.createStatement();
            
            // lakukan query ke database
            ResultSet queryResult = stmt.executeQuery(SQL_QUERY);
            
           // cek apakah query menghasilkan result atau tidak
           if(queryResult.isBeforeFirst()){
               
               while(queryResult.next()){
                   
                   String kata = queryResult.getString("katadasar");
                   String kode = queryResult.getString("kode_katadasar");
                   
                   /* ============================================================
                    * Untuk masing-masing kata yang ditemukan kelas katanya
                    * lakukan proses modifikasi yaitu dengan menambahkan kelasnya 
                    * di akhir dengan dippisahkan oleh tanda ';'
                    * ============================================================*/
                   
                   // ambil index array dari kata yang bersangkutan
                   int indexOfTheToken = TOKEN.indexOf(kata);
                   
                   // modifikasi isi array
                   TOKEN.set(indexOfTheToken, kata + ";" + kode);
               }
           }
           
           queryResult.close();
           stmt.close();
           conn.close();
           
           // broadcast hasil tag
           listener.onTokenizeSuccess(TOKEN);
            
        } catch( SQLException e ){
            listener.onTaskFail(Tokenizer.class.getName(), e.getMessage());
        }
    }
    
    private Connection initDatabase(TokenizerListener listener){
        
        /**
         * Lakukan inisialisasi koneksi ke database lexicon
         * proses ini harus dilakukan setelah proses inisialisasi tokenizerListener
         * agar apabila terjadi error pada tahapan ini, maka notifikasinya
         * dapat di broadcast ke class subscriber
         */
         try{
            Class.forName(Constant.DB_DRIVER).newInstance();
            
            return DriverManager.getConnection(Constant.DB_URL + Constant.DB_NAME, Constant.DB_USER, Constant.DB_PASS);
        }
        catch( IllegalAccessException | ClassNotFoundException | InstantiationException | SQLException e ){
            listener.onTaskFail(Tokenizer.class.getName(), "Koneksi ke database gagal karena: " + e.getMessage());
        }
        return null;
    }
}
