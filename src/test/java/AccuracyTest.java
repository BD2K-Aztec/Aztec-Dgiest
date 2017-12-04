import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.json.JSONString;
import org.junit.*;
import web.DigestPDF;
import web.DupDetector;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccuracyTest {
    public static String PDF_PATH = ".//src//test//resources//pdfs//";
    public static String TEST_JSON_PATH = ".//src//test//resources//test.json";
    public static String DUP_TEST_JSON_PATH = ".//src//test//resources//dup_test.json";

    public static final int NUM_TO_TEST = 10;

    public JSONObject result_json = null;
//    @Before
    public void init(){
        ArrayList<File> fnm_files = new ArrayList();
        ArrayList<String> f_filenames = new ArrayList();

        File folder = new File(PDF_PATH);
        File[] files = folder.listFiles();

        Random rand = new Random();

        for(int i = 0; i < NUM_TO_TEST; i++){
            int id = rand.nextInt(files.length)+1;
            File file = new File(PDF_PATH+Integer.toString(id)+".pdf");
            fnm_files.add(file);
            f_filenames.add(file.getName());
        }

        DigestPDF result = new DigestPDF(fnm_files, f_filenames);



        result_json = result.getData();
    }

    @Test
    public void testDuplicate(){
        try {
            String json_string = new String(Files.readAllBytes((Paths.get(DUP_TEST_JSON_PATH))));
            JSONObject test_json = new JSONObject(json_string);
            DupDetector dup = new DupDetector();
            assertTrue("Tool is a duplicate", dup.isDuplicate(test_json));
            assertTrue("Doc2Vec index", dup.trainD2V(false));
            ArrayList<String> results = dup.getNearestN(test_json.getString("description"), 5);
            for(String result: results){
                double score = dup.getSimilarity(test_json.getString("description"), result);

                if(score>0.8){
                    System.out.println("Duplicate tool: "+result);
                }else if(score>0.4){
                    System.out.println("Similar tool: "+ result);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
//    @Test
    public void testAccuracy(){
        try {
            File test_file = new File(TEST_JSON_PATH);


//            JSONObject test_json = new JSONObject();
            String json_string = new String(Files.readAllBytes((Paths.get(TEST_JSON_PATH))));
            JSONObject test_json = new JSONObject(json_string);

            Iterator<String> it = this.result_json.keys();
            while(it.hasNext()){
                String filename = it.next();
                JSONObject entry = test_json.getJSONObject(filename);

                System.out.println("JSON to be tested:");
                System.out.println(result_json.getJSONObject(filename).toString(4));
                System.out.println("-------\nCompare to baseline JSON:");
                System.out.println(entry.toString(4));
                compareFields(entry, result_json.getJSONObject(filename));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void compareFields(JSONObject baseline, JSONObject test){
        assertTrue("Forks are up-to-date",baseline.optInt("forks", 0) <= test.optInt("forks", 0));
        assertTrue("Downloads are up-to-date",baseline.optInt("downloads", 0) <= test.optInt("downloads", 0));
        assertEquals("Tool name is up-to-date", baseline.optString("toolName", ""), test.optString("toolName", ""));
        assertEquals("License is up-to-date", baseline.optString("license", ""), test.optString("license", ""));
        assertEquals("Title is up-to-date", baseline.optString("publicationTitle", ""), test.optString("publicationTitle", ""));
        assertEquals("Language is up-to-date", baseline.optString("programmingLanguage", ""), test.optString("programmingLanguage", ""));
        assertEquals("DOI is up-to-date", baseline.optString("publicationDOI", ""), test.optString("publicationDOI", ""));
        assertEquals("Source Link is up-to-date", baseline.optString("sourceCodeLink", ""), test.optString("sourceCodeLink", ""));

        assertTrue("Issues are up-to-date",!(baseline.has("numIssues") ^ test.has("numIssues")));
        assertTrue("Subscribers are up-to-date",!(baseline.has("subscribers") ^ test.has("subscribers")));
        assertTrue("Code size are up-to-date",!(baseline.has("codeSize") ^ test.has("codeSize")));


        assertTrue("Has abstract", !(baseline.has("publicationAbstract") ^ test.has("publicationAbstract")) || test.has("publicationAbstract"));
        assertTrue("Has short description", !(baseline.has("shortDescription") ^ test.has("shortDescription")) || test.has("shortDescription"));
        assertTrue("Has full text", !(baseline.has("fulltext") ^ test.has("fulltext")) || test.has("fulltext"));
//        try {
//            System.out.println((Collection<String>)test.get("contactInfo"));
//        }catch(Exception e){
//            e.printStackTrace();
//        }
    }
    public static void main(String[] args) {

    }
}
