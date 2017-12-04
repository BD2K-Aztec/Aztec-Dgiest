package attributes.contact;

import attributes.Utilities;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactInfo {

    private List<String> m_contact;

    public List<String> getContact() {
        return this.m_contact;
    }

    public ContactInfo(String nlm, boolean fromPMC) throws Exception {
        JSONObject xmlJSONObj = XML.toJSONObject(nlm);
        if(fromPMC) {
            this.m_contact = extractContactFromPMCXML(nlm);
        }
        else{
            this.m_contact = extractContactFromCermineXML(xmlJSONObj);
        }

    }

    private List<String> extractContactFromCermineXML(JSONObject xmlJSONObj) {
        ArrayList<String> arraylist = new ArrayList<String>();
        try {
            JSONObject article_meta = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
            if (article_meta.has("contrib-group")) {
                JSONObject group = article_meta.getJSONObject("contrib-group");
                if (group.has("contrib")) {
                    Object item = group.get("contrib");
                    if (item instanceof JSONArray) {
                        JSONArray contacts = (JSONArray) item;
                        for (int i = 0; i < contacts.length(); i++) {
                            if (contacts.getJSONObject(i).has("email")) {
                                String contact = contacts.getJSONObject(i).getString("email");
                                arraylist.add(contact);
                            }
                        }
                    } else if (item instanceof String) {
                        String contact = (String) item;
                        arraylist.add(contact);
                    }
                }
            }
            if(arraylist.isEmpty()){
                JSONObject abstract_json = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("abstract");
                String abstract_txt = abstract_json.getString("p");

                arraylist.addAll(Utilities.findEmail(abstract_txt));

                JSONArray paragraph_json = xmlJSONObj.getJSONObject("article").getJSONObject("body").getJSONArray("sec");

                for (int i = 0; i < paragraph_json.length(); i++) {
                    JSONObject obj = paragraph_json.getJSONObject(i);

                    if(obj.getString("title").toLowerCase().contains("author")){
                        arraylist.addAll(Utilities.findEmail(obj.toString()));
                    }
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("Could not find contact info");
        }
        return arraylist;
    }

    private List<String> extractContactFromPMCXML(String nlm) {
        ArrayList<String> arraylist = new ArrayList<String>();
        try {
            String pattern = "(?s)<email.*?<\\/email>";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(nlm);
            while (m.find()) {
                String contact = m.group().split("<email.*?>")[1];
                contact = contact.split("</email>")[0];
                arraylist.add(contact);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return arraylist;
    }
}