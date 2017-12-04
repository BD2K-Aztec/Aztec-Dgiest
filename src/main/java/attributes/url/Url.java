package attributes.url;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Url {

    private List<String> m_url;

    public List<String> getUrl() {  return this.m_url; }

    public Url(JSONObject xmlJSONObj, String name) throws Exception {
        this.m_url = extractURL(xmlJSONObj, name);
    }

    private List<String> extractURL(JSONObject xmlJSONObj, String name) {
        Set<String> all_links = new HashSet<>();
        Set<String> bad_links = new HashSet<>();

        try {
            name = name.split(".pdf")[0];

            //check URLs without http
            String line = xmlJSONObj.toString();
            line = line.replaceAll("\\\\","");
            String pattern = "[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&\\/=]*)[^.\\-\\\\ @\"'\\)\\(]";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(line);
            while (m.find( )) {
                String link = m.group();
                if(bad_links.contains(link)){
                    continue;
                }

                if(!link.contains("w3") && !link.contains("creativecommons") && !link.contains("@") && !link.contains("niso")){
                    if(this.checkLink(link)) {
                        all_links.add(link);
                    }else if(link.contains("github") || link.contains("sourceforge") || link.contains("bitbucket")) {
                        if(!link.contains(".Author") && !link.contains(".Contact") && !link.contains(".Supplementary")){
                            all_links.add(link);
                        }else{
                            all_links.add(link.substring(0, link.lastIndexOf('.')));
                        }
                    }else{
                        bad_links.add(link);
                    }
                }
                String lowercase_link = link.toLowerCase();

                if (name != null && name.split(".pdf").length >= 1) {
                    name = name.split(".pdf")[0];
                } else {
                    continue;
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LinkedList<>(all_links);
    }

    //to check if the m_url link is valid or not
    public String getStatus(List<String> good_links) throws Exception {
        //check if good_links are valid
        for(int i=0;i<good_links.size();i++){
            if(checkLink(good_links.get(i))==false){
                return "URL link no longer valid.";
            }
        }
        return "Success";
    }

    //helper function: get HTML content
    public static boolean checkLink(String urlToRead) throws Exception {
        try{
            if(!urlToRead.startsWith("http")){
                urlToRead = "https://"+urlToRead;
            }
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1500);
            int code = conn.getResponseCode() ;
//            System.out.println(urlToRead);
//            System.out.println(code);
            if(code==404){
                return false;
            }
            return true;
        }
        catch (Exception e){
//            e.printStackTrace();
            System.out.println("Cannot connect to "+urlToRead);
            return false;
        }
    }

}

