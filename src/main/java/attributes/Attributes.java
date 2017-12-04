package attributes;

import attributes.abstrakt.Abstract;
import attributes.affiliations.Affiliations;
import attributes.authors.Authors;
import attributes.contact.ContactInfo;
import attributes.date.PublicationDate;
import attributes.doi.DOI;
import attributes.language.Language;
import attributes.title.Title;
import attributes.url.Url;
import org.json.JSONObject;
import org.json.XML;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Attributes implements Runnable {
    private final String title;
    private final String name;
    private final String abstrakt;
    private final String summary="";
    private final List<String> authors;
    private final List<String> affiliations;
    private final List<String> contact;
    private final String doi;
    private final String date;
    private final String status;
    private final List<String> URLs;
    private final List<String> tags=new ArrayList<>();
    private List<String> funding;
    private String language;
    private CodeStats cs;

    private final JSONObject xmlJSONObj;
    private final String m_nlm;
//    private List<FundingInfo> funding_list;

    private JSONObject final_object;

    // ------------------------------------------------------------- //

    public Attributes(String nlm, String filename, boolean fromPMC) throws Exception {
        m_nlm = nlm;
        xmlJSONObj = XML.toJSONObject(nlm);
        funding = null;
        language = null;
        cs = null;

        run();

        Calendar title_start = Calendar.getInstance();
        Title t = new Title(xmlJSONObj);
        title = t.getTitle();
        Calendar title_end = Calendar.getInstance();

        Calendar author_start = Calendar.getInstance();
        Authors au = new Authors(xmlJSONObj,fromPMC);
        authors = au.getAuthors();
        for (int i = 0; i < authors.size(); i++) {
            authors.set(i, authors.get(i).trim());
        }
        Calendar author_end = Calendar.getInstance();
        Calendar aff_start = Calendar.getInstance();
        Affiliations aff = new Affiliations(xmlJSONObj,fromPMC);
        affiliations = aff.getAffiliations();
        for (int i = 0; i < affiliations.size(); i++) {
            affiliations.set(i, affiliations.get(i).trim());
        }
        Calendar aff_end = Calendar.getInstance();
        Calendar contact_start = Calendar.getInstance();
        ContactInfo con = new ContactInfo(nlm, fromPMC);
        contact = con.getContact();
        for (int i = 0; i < contact.size(); i++) {
            contact.set(i, contact.get(i).trim());
        }
        Calendar contact_end = Calendar.getInstance();
        Calendar doi_start = Calendar.getInstance();
        DOI d2 = new DOI(xmlJSONObj, fromPMC);
        doi = d2.getDoi();
        Calendar doi_end = Calendar.getInstance();
        Calendar date_start = Calendar.getInstance();
        PublicationDate d = new PublicationDate(xmlJSONObj, fromPMC);
        date = d.getDate();
        Calendar date_end = Calendar.getInstance();


        Calendar url_start = Calendar.getInstance();
        Url url_link = new Url(xmlJSONObj, filename);
        URLs = url_link.getUrl();
        for (int i = 0; i < URLs.size(); i++) {
            URLs.set(i, URLs.get(i).trim());
        }

        if(URLs.isEmpty()){
            status = "URL links are no longer valid";
        }else{
            status = "Success";
        }
        Calendar url_end = Calendar.getInstance();


        Calendar abstract_start = Calendar.getInstance();
        Abstract a = new Abstract(nlm,fromPMC);
        abstrakt = a.getAbstract().trim();
        Calendar abstract_end = Calendar.getInstance();

        Calendar name_start = Calendar.getInstance();
        ToolName tool_name = new ToolName(title, URLs);

        if(tool_name.getName()==null){
            name = "";
        }else{
            name = tool_name.getName();
        }
        Calendar name_end = Calendar.getInstance();



        Calendar lang_start = Calendar.getInstance();
        String code_url = "";
        for(String url: URLs){
            if(url.contains("github")){
                code_url = url;
                cs = new CodeStats(code_url, name);
                if(cs.hasStats()){
                    break;
                }
            }
            if(url.contains("sourceforge") || url.contains("bitbucket")){
                code_url = url;
            }
        }
        if(cs==null){
            cs = new CodeStats(code_url, name);
        }


        final_object = new JSONObject();
        final_object.put("publicationTitle", title);
        final_object.put("publicationAbstract", abstrakt);
        final_object.put("title", title);
        final_object.put("authors", authors.toArray());
        final_object.put("institutions", affiliations.toArray());
        final_object.put("contactInfo", contact.toArray());
        final_object.put("publicationDOI", doi);
        final_object.put("publicationDate", date);
        final_object.put("URLs", URLs.toArray());
        final_object.put("status", status);
        final_object.put("toolName", name);
        final_object.put("fulltext", this.m_nlm.replaceAll("<.*?>", " "));

        if(cs.hasStats()) {
            final_object.put("programmingLanguage", cs.getLanguage());
            final_object.put("forks", cs.getForks());
            final_object.put("shortDescription", cs.getDescription());
            final_object.put("license", cs.getLicense());
            final_object.put("sourceCodeLink", cs.getRepo_link());
            final_object.put("dateCreated", cs.getDate_created());
            final_object.put("dateUpdated", cs.getDate_updated());
            final_object.put("downloads", cs.getDownloads());
            final_object.put("codeSize", cs.getSize());
            final_object.put("sourceCodeLink", cs.getRepo_link());
            final_object.put("numIssues", cs.getIssues());
            final_object.put("subscribers", cs.getSubscribers());
            if(!cs.getHomepage().isEmpty()) {
                final_object.put("homepage", cs.getHomepage());
            }
            if(cs.getRepo_name()!=null && !cs.getRepo_name().isEmpty()){
                final_object.put("toolName", cs.getRepo_name());
            }

            Calendar lang_end = Calendar.getInstance();
        }
    }

    // ------------------------------------------------------------ //

    public void run() {
        try {
            Calendar funding_start = Calendar.getInstance();
//            Funding f = new Funding(m_nlm, 0);
//            funding_list = f.getFunding();
            Calendar funding_end = Calendar.getInstance();
//            System.out.println("Time funding: ");
//            System.out.println(funding_end.getTimeInMillis() - funding_start.getTimeInMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setID(int id){
        final_object.put("id", id);
    }

    public String getTitle(){
        return title;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getAuthor(){
        return authors;
    }

    public List<String> getAffiliation(){
        return affiliations;
    }

    public String getAbstrakt(){
        return abstrakt;
    }

    public List<String> getContact(){
        return contact;
    }

    public String getDOI(){
        return doi;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getURL(){
        return URLs;
    }

//    private List<FundingInfo> getFundingList() {
//        return funding_list;
//    }

    public List<String> getFundingStr() {
//        List<String> fa = new ArrayList<>();
//        for (FundingInfo fi : getFundingList()) {
//            fa.add(fi.toString());
//        }
//        funding = fa;
        return this.funding;
    }

    public String getLanguage(){
        return language;
    }

    public List<String> getTags(){
        return tags;
    }

    public JSONObject getFinalJSONObject(){
        return final_object;
    }

    public CodeStats getCodeStats() {
        return cs;
    }
}
