package web;

import attributes.Utilities;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DupDetector {
    public static final String DATA_LOCATION = ".//src//main//resources//data.json";
    public static final String DOCS_LOCATION = ".//src//main//resources//docs.txt";
    public static final String MODEL_LOCATION = ".//src//main//resources//model.zip";

    private ParagraphVectors model;

    class SimilarTool{
        public int id;
        public float score;
    }
    public static boolean retrieveNewDict(){

        JSONObject json = Utilities.getJSON(SolrInterface.SOLR_URL+"select?q=*%3A*&rows=100000&wt=json&indent=true");
        try{
            FileWriter fw = new FileWriter(DATA_LOCATION, false);
            fw.write(json.toString());
            fw.close();
        }catch(IOException ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private HashMap<String, ArrayList<Integer>> name_map;
    private HashMap<String, ArrayList<Integer>> author_map;
    private HashMap<String, ArrayList<Integer>> inst_map;
    private HashMap<String, ArrayList<Integer>> link_map;

    public DupDetector(){
        model = null;
        try {
            String json_string = new String(Files.readAllBytes((Paths.get(DATA_LOCATION))));
            JSONObject data_json = new JSONObject(json_string);
            JSONArray json_arr = data_json.getJSONObject("response").getJSONArray("docs");

            name_map = new HashMap();
            author_map = new HashMap();
            inst_map = new HashMap();
            link_map = new HashMap();

            // insert names
            for(int i = 0; i<json_arr.length(); i++){
                JSONObject obj = json_arr.getJSONObject(i);
                int id = obj.getInt("id");

                String name = obj.optString("name", "");
                if(name_map.containsKey(name)){
                    ArrayList<Integer> list = name_map.get(name);
                    list.add(id);
                    name_map.put(name, list);
                }else {
                    ArrayList<Integer> list = new ArrayList<>();
                    list.add(id);
                    name_map.putIfAbsent(name, list);
                }

                // insert authors
                JSONArray author_list = obj.optJSONArray("authors");
                for(int j = 0; author_list!=null && j<author_list.length(); j++){
                    String author = author_list.getString(j);
                    if(author_map.containsKey(author)){
                        ArrayList<Integer> list = author_map.get(author);
                        list.add(id);
                        author_map.put(name, list);
                    }else {
                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(id);
                        author_map.putIfAbsent(author, list);
                    }

                }

                // insert institutions
                JSONArray inst_list = obj.optJSONArray("institutions");
                for(int j = 0; inst_list!=null && j<inst_list.length(); j++){
                    String inst = inst_list.getString(j);
                    if(inst_map.containsKey(inst)){
                        ArrayList<Integer> list = inst_map.get(inst);
                        list.add(id);
                        inst_map.put(name, list);
                    }else {
                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(id);
                        inst_map.putIfAbsent(inst, list);
                    }

                }

                // insert links
                JSONArray link_list = obj.optJSONArray("linkUrls");
                for(int j = 0; link_list!=null && j<link_list.length(); j++){
                    String link = link_list.getString(j);
                    if(link_map.containsKey(link)){
                        ArrayList<Integer> list = link_map.get(link);
                        list.add(id);
                        link_map.put(name, list);
                    }else {
                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(id);
                        link_map.putIfAbsent(link, list);
                    }

                }
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }

    }

    private SimilarTool score(HashMap<String, ArrayList<Integer>> map, ArrayList<String> list){
        int total = list.size();

        SimilarTool st = new SimilarTool();
        st.id = -1;
        st.score = 0;

        if(total==0){
            return st;
        }

        HashMap<Integer, Integer> id_ctr = new HashMap();
        for(String entry: list){
            if(map.containsKey(entry)){
                ArrayList<Integer> ids = map.getOrDefault(entry, null);
                if(ids!=null){
                    for(int id: ids) {
                        int new_count = id_ctr.getOrDefault(id, 0)+1;
                        id_ctr.put(id, new_count);

                        if(new_count>st.score){
                            st.score = new_count;
                            st.id = id;
                        }
                    }
                }

            }
        }
        st.score/=total;
        return st;
    }

    public boolean isDuplicate(JSONObject obj){

        String name = obj.optString("name", "");
        HashSet<Integer> id_list = new HashSet();
        if(name!="" && this.name_map.containsKey(name)){
            id_list.addAll(name_map.get(name));
        }

        SimilarTool byAuthor = this.score(this.author_map, this.convert2list(obj.getJSONArray("authors")));
        SimilarTool byInst = this.score(this.inst_map, this.convert2list(obj.getJSONArray("institutions")));
        SimilarTool byLinks = this.score(this.link_map, this.convert2list(obj.getJSONArray("URLs")));

        if(id_list.contains(byAuthor.id) && byAuthor.score>0.1){
            return true;
        }else if(id_list.contains(byInst.id) && byInst.score>=0.5){
            return true;
        }else if(id_list.contains(byLinks.id) && byLinks.score>=0.5){
            return true;
        }

        return false;
    }

    private ArrayList<String> convert2list(JSONArray arr){
        ArrayList<String> result = new ArrayList<>();

        for(int i = 0; i < arr.length(); i++){
            result.add((arr.getString(i)));
        }

        return result;
    }

    public boolean trainD2V(boolean retrain){
        try {
            if(retrain) {
                ArrayList<String> ids = new ArrayList<>();
                File f = new File(DOCS_LOCATION);

                String json_string = new String(Files.readAllBytes((Paths.get(DATA_LOCATION))));
                JSONObject data_json = new JSONObject(json_string);
                JSONArray json_arr = data_json.getJSONObject("response").getJSONArray("docs");

                FileWriter fw = new FileWriter(f);
                for (int i = 0; i < json_arr.length(); i++) {
                    JSONObject json = json_arr.getJSONObject(i);
                    if (json.has("description") && json.getString("description").length() > 1) {
                        String text = json.getString("description");
                        text = text.replace('\n', ' ');
                        fw.write(text + '\n');
                        ids.add(Integer.toString(json.getInt("id")));
                    }
                }
                fw.close();

                SentenceIterator iter = new BasicLineIterator(f);
                int count = 0;
                while (iter.hasNext()) {
                    iter.nextSentence();
                    count++;
                }

                if (ids.size() != count) {
                    System.out.println("Mismatch error: Number of IDs does not match the number of documents.");
                    return false;
                }

                AbstractCache<VocabWord> cache = new AbstractCache<>();

                TokenizerFactory t = new DefaultTokenizerFactory();
                t.setTokenPreProcessor(new CommonPreprocessor());

                LabelsSource source = new LabelsSource(ids);

                this.model = new ParagraphVectors.Builder()
                        .minWordFrequency(1)
                        .iterations(5)
                        .epochs(5)
                        .layerSize(400)
                        .learningRate(0.025)
                        .labelsSource(source)
                        .windowSize(5)
                        .iterate(iter)
                        .trainWordVectors(false)
                        .vocabCache(cache)
                        .tokenizerFactory(t)
                        .sampling(0.001)
                        .build();

                this.model.fit();


                WordVectorSerializer.writeParagraphVectors(this.model, MODEL_LOCATION);

            }else{

                this.model = WordVectorSerializer.readParagraphVectors(MODEL_LOCATION);
                TokenizerFactory t = new DefaultTokenizerFactory();
                t.setTokenPreProcessor(new CommonPreprocessor());
                this.model.setTokenizerFactory(t);
                this.model.getConfiguration().setIterations(5);

            }

        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }

        return true;

    }

    public ArrayList<String> getNearestN(String rawtext, int n){
        if(this.model==null){
            return null;
        }
        ArrayList<String> results = new ArrayList(this.model.nearestLabels(rawtext, n));
        return results;
    }

    public double getSimilarity(String rawtext, String label){
        if(this.model==null){
            return -1;
        }
        return this.model.similarityToLabel(rawtext, label);
    }
}
