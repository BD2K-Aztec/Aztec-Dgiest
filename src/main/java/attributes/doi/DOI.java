package attributes.doi;

import org.json.JSONArray;
import org.json.JSONObject;

public class DOI {

    private String m_doi;

    public String getDoi() {  return this.m_doi; }

    public DOI(JSONObject xmlJSONObj, boolean fromPMC) throws Exception {
        if(fromPMC) {
            this.m_doi = extractDOIFromPMCXML(xmlJSONObj);
        }
        else{
            this.m_doi = extractDOIFromCermineXML(xmlJSONObj);
        }
    }

    private String extractDOIFromCermineXML(JSONObject xmlJSONObj) {
        try {
            JSONObject value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
            if (value.has("article-id")) {
                String DOI = value.getJSONObject("article-id").getString("content");
                return DOI;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "None";
    }

    private String extractDOIFromPMCXML(JSONObject xmlJSONObj) {
        try {
            JSONObject value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
            if (value.has("article-id")) {
                JSONArray list = value.getJSONArray("article-id");
                for (int i = 0; i < list.length(); i++) {
                    JSONObject entry = list.getJSONObject(i);
                    if (entry.getString("pub-id-type").equals("doi")) {
                        return entry.getString("content");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "None";
    }
}

