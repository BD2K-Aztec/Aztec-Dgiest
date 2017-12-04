package attributes;

import attributes.url.Url;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static attributes.Utilities.readJsonFromUrl;

public class CodeStats {
    public static final String GITHUB_URL = "https://api.github.com/repos/";
    public static final String GITHUB_CLIENT_ID = "046041908fb0240cb92e&client_secret=5d1cf3216d6af9aff470fbb6047b1644af7c0c7f";
    public static final String SOURCEFORGE_URL = "https://sourceforge.net/rest/p/";
    public static final String BITBUCKET_URL = "https://api.bitbucket.org/2.0/repositories/";

    private boolean m_hasStats;
    private String repo;
    private String repo_id;
    private String repo_name;
    private String repo_link;
    private int forks;
    private int subscribers;
    private int issues;
    private int downloads;
    private int size;
    private String description;
    private String date_created;
    private String date_updated;
    private String homepage;
    private String language;
    private String license;

    public CodeStats(String url, String name) {
        this.m_hasStats = false;

        this.repo_link = "";
        this.description = "";
        this.date_created = "";
        this.date_updated = "";
        this.homepage = "";
        this.license = "";
        this.repo_name = name.replace(" ", "");

        try {
            if (!Url.checkLink(url) && !url.contains(this.repo_name)) {
                url = url.substring(0, url.lastIndexOf('/')) + "/" + this.repo_name;
            }
            if (url.contains("sourceforge")) {
                this.repo = "sourceforge";
                this.setup(url);
                this.getSourceforgeInfo();
            } else if (url.contains("github")) {
                this.repo = "github";
                this.setup(url);
                this.getGithubInfo();
            } else if (url.contains("bitbucket")) {
                this.repo = "bitbucket";
                this.setup(url);
                this.getBitbucketInfo();
            } else {
                this.repo = "";
                this.m_hasStats = !this.m_hasStats;
            }
            if(this.language==null || this.language=="null"){
                this.language = "";
            }
        }catch( Exception ex){
            this.m_hasStats = false;
            System.out.println("Error checking link: "+url);
        }

    }


    private void setup(String url){

        this.repo_id = null;
        String pattern = "(www\\.)?"+this.repo+".(com|net|org|io)\\/[\\w-]+(\\/[\\w-]+)?";
        if(this.repo.equals("sourceforge")){
            pattern = "(www\\.)?"+this.repo+".(com|net|org|io)\\/projects\\/[\\w-]+";
        }

        Pattern regex = Pattern.compile(pattern);
        Matcher match = regex.matcher(url);
        if(match.find()){
            String found_link = match.group(0);
            if(this.repo.equals("sourceforge")){
                this.repo_id = found_link.substring(found_link.lastIndexOf("/")+1);
            }else {
                this.repo_id = found_link.substring(found_link.indexOf("/") + 1);
            }

        }
        if(this.repo_id==null || !this.repo_id.contains("/")){
            pattern = "([\\w-]+.)?"+this.repo+".(com|net|org|io)\\/[\\w-]+";
            regex = Pattern.compile(pattern);
            match = regex.matcher(url);
            if(match.find()){
                String found_link = match.group(0);
                if(found_link.startsWith(this.repo) || found_link.startsWith("www")){
                    this.repo_id = found_link.substring(found_link.lastIndexOf('/') + 1)+"/"+this.repo_name;
                }else {
                    this.repo_id = found_link.substring(0, found_link.indexOf(".")) + "/" +
                            found_link.substring(found_link.lastIndexOf('/') + 1);
                }
            }
        }
    }

    private void getGithubInfo() {
        this.repo_link = "https://github.com/"+this.repo_id;
        String query_url = GITHUB_URL + this.repo_id + "?client_id=" + GITHUB_CLIENT_ID;

        try {
            JSONObject github_json = readJsonFromUrl(query_url);
            if(github_json==null) {
                this.m_hasStats = false;
                return;
            }
            this.repo_name = github_json.getString("name");
            this.forks = github_json.getInt("forks");
            this.subscribers = github_json.getInt("subscribers_count");
            this.issues = github_json.getInt("open_issues_count");
            this.description = github_json.optString("description", "");
            this.language = github_json.optString("language", "");
            this.date_created = github_json.getString("created_at");
            this.date_updated = github_json.getString("updated_at");
            this.homepage = github_json.optString("homepage", "");
            this.size = github_json.getInt("size");

            JSONObject license_json = github_json.optJSONObject("license");
            if (license_json != null) {
                this.license = license_json.optString("name", "No License");
            } else {
                String query_license_url = GITHUB_URL + this.repo_id + "/license?client_id=" + GITHUB_CLIENT_ID;
                license_json = readJsonFromUrl(query_license_url);

                if(license_json !=null){
                    String license_txt = license_json.getJSONObject("license").optString("name", "No License");
                    if(license_txt.equals("Other")){
                        this.license = license_json.getJSONObject("_links").optString("html", "Other");
                    }else{
                        this.license = license_txt;
                    }

                }else {
                    if(Url.checkLink(this.repo_link+"/blob/master/LICENSE")){
                        this.license = this.repo_link+"/blob/master/LICENSE";
                    }else {
                        this.license = "No License";
                    }
                }
            }
            this.m_hasStats = true;

        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("Exception connecting to "+this.repo_link);
            this.m_hasStats = false;
        }
    }

    private void getSourceforgeInfo(){
        this.repo_link = "https://sourceforge.net/projects/"+this.repo_id;
        String query_url = SOURCEFORGE_URL+this.repo_id;
        try{
            System.setProperty("https.protocols", "TLSv1");
            JSONObject sf_json = readJsonFromUrl(query_url);

            if(sf_json==null) {
                this.m_hasStats = false;
                return;
            }

            // if there is a handshake error:
            // http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
            String moved_url = sf_json.optString("moved_to_url", "");
            this.homepage = sf_json.optString("external_homepage", "");

            if(moved_url.contains("github") || this.homepage.contains("github")){
                String pattern = "(www\\.)?github.(com|net|org|io)\\/[\\S]+(\\/[\\w.-]+)?";
                Pattern regex = Pattern.compile(pattern);
                Matcher match = regex.matcher(this.homepage);

                if(match.find()) {
                    String found_link = match.group(0);
                    this.repo_id = found_link.substring(found_link.indexOf("/") + 1);
                    this.getGithubInfo();
                    return;
                }
            }

            this.repo_name = sf_json.getString("name");

            JSONObject cat_json = sf_json.getJSONObject("categories");
            if(cat_json.has("language")){
                JSONArray lang_array = cat_json.getJSONArray("language");
                if(lang_array.length()!=0) {
                    this.language =lang_array.getJSONObject(0).optString("fullname", "");
                }
                else{
                    this.language = "";
                }
            }

            if(cat_json.has("license")){
                JSONArray lang_array = cat_json.getJSONArray("license");
                if(lang_array.length()!=0) {
                    this.license =lang_array.getJSONObject(0).optString("fullname", "No License");
                }else{
                    this.license = "No License";
                }
            }

            this.forks = sf_json.optInt("forks", 0);
            this.description = sf_json.optString("short_description", "");
            this.date_created = sf_json.getString("creation_date");

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();

            String stats_url = this.repo_link+"/files/stats/json?start_date=2000-1-1&end_date="+dateFormat.format(date);
            JSONObject stats_json = readJsonFromUrl(stats_url);

            this.downloads = stats_json.getInt("total");

            String activity_url = query_url+"/activity";
            JSONObject activity_json = readJsonFromUrl(activity_url);

            JSONObject timeline_json = activity_json.getJSONArray("timeline").getJSONObject(0);
            Date sf_date = new Date(timeline_json.getLong("published"));
            this.date_updated = dateFormat.format(sf_date);
            this.m_hasStats = true;

        }catch(IOException e){
//            e.printStackTrace();
            System.out.println("Exception connecting to "+this.repo_link);
            this.m_hasStats = false;
        }
    }

    private void getBitbucketInfo() {
        this.repo_link = "https://bitbucket.org/"+this.repo_id;
        String query_url = BITBUCKET_URL + this.repo_id;
        try {
            JSONObject bitbucket_json = readJsonFromUrl(query_url);

            if(bitbucket_json==null) {
                this.m_hasStats = false;
                return;
            }

            this.repo_name = bitbucket_json.getString("name");
            this.description = bitbucket_json.optString("description", "");
            this.language = bitbucket_json.optString("language", "");
            this.date_created = bitbucket_json.getString("created_on");
            this.date_updated = bitbucket_json.getString("updated_on");
            this.homepage = bitbucket_json.optString("website", "");
            this.size = bitbucket_json.getInt("size");

            String fork_url = query_url+"/forks";
            JSONObject fork_json = readJsonFromUrl(fork_url);

            this.forks = fork_json.getInt("size");

            String watcher_url = query_url+"/watchers";
            JSONObject watcher_json = readJsonFromUrl(watcher_url);
            this.subscribers = watcher_json.getInt("size");

            this.m_hasStats = true;
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Exception connecting to "+this.repo_link);
            this.m_hasStats = false;
        }
    }


    public String getRepo() {
        return repo;
    }

    public String getRepo_id() {
        return repo_id;
    }

    public String getRepo_name() {
        return repo_name;
    }

    public String getRepo_link() {
        return repo_link;
    }

    public int getForks() {
        return forks;
    }

    public int getSubscribers() {
        return subscribers;
    }

    public int getIssues() {
        return issues;
    }

    public int getDownloads() {
        return downloads;
    }

    public int getSize() {
        return size;
    }

    public String getDescription() {
        return description;
    }

    public String getDate_created() {
        return date_created;
    }

    public String getDate_updated() {
        return date_updated;
    }

    public String getHomepage() {
        return homepage;
    }

    public String getLanguage() {
        return language;
    }

    public String getLicense() {
        return license;
    }

    public boolean hasStats() {
        return m_hasStats;
    }
}
