package web;

import attributes.Attributes;
import attributes.CodeStats;
import attributes.Utilities;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONObject;

import java.util.Calendar;


public class SolrInterface {
    public static String SOLR_URL = "http://10.44.115.202:8983/solr/BD2K/";

    public int getID(){
        JSONObject json = Utilities.getJSON(this.SOLR_URL+"select?q=*%3A*&wt=json&indent=true");
        if(json!=null) {

            int num_tools = json.getJSONObject("response").optInt("numFound", 0);
            return num_tools;
        }

        return -1;
    }

    public int newID(){
        return this.getID()+1;
    }


    public boolean submit(Attributes attr){
        CodeStats cs = attr.getCodeStats();
        String link = null;
        if (cs != null) {
             link = cs.getRepo_link();
        }
        if(checkDup(attr.getDOI(), link)){
            return false;
        }

        try {
            SolrClient sc = new HttpSolrClient.Builder(this.SOLR_URL).build();
            SolrInputDocument doc = new SolrInputDocument();
            doc.addField("id", this.newID());
            doc.addField("name", attr.getName());
            doc.addField("institutions", attr.getAffiliation());
            doc.addField("linkUrls", attr.getURL());
            doc.addField("description", attr.getAbstrakt());
            doc.addField("authors", attr.getAuthor());
            doc.addField("publicationDOI", attr.getDOI());
            doc.addField("publicationTitle", attr.getTitle());
            doc.addField("publicationDate", attr.getDate());

            if (cs != null) {
                doc.addField("repo", cs.getRepo());
                doc.addField("repoName", cs.getRepo_name());
                doc.addField("codeRepoURL", cs.getRepo_link());
                doc.addField("repoForks", cs.getForks());
                doc.addField("repoDownloads", cs.getDownloads());
                doc.addField("repoDescription", cs.getDescription());
                doc.addField("license", cs.getLicense());
                doc.addField("repoHomepage", cs.getHomepage());
                doc.addField("repoUpdatedDate", cs.getDate_updated());
                doc.addField("repoCreationDate", cs.getDate_created());
                doc.addField("language", cs.getLanguage());
            }

            Calendar now = Calendar.getInstance();
            String cur_time = now.YEAR + "-" + now.MONTH + "-" + now.DAY_OF_MONTH + "T" +
                    now.HOUR + ":" + now.MINUTE + ":" + now.SECOND + "." + now.MILLISECOND + "Z";

            sc.add(doc);
            sc.commit();
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }

        return false;

    }

    public boolean checkDup(String doi, String src_url){
        String build_url = this.SOLR_URL+"select?rows=100&wt=json&indent=true&q=";

        if(doi!=null) {
            build_url += "publicationDOI%3A\""+doi+"\"%2C+";
        }

        if(src_url!=null){
            build_url += "codeRepoURL%3A\""+src_url+"\"";
        }

        JSONObject json = Utilities.getJSON(build_url);

        if(json!=null){
            return json.getJSONObject("response").optInt("numFound", 0) > 0;
        }
        System.out.println("Error in getting DOI json");
        return false;
    }
}
