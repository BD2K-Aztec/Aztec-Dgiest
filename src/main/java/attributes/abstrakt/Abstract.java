package attributes.abstrakt;

import org.json.JSONObject;
import org.json.XML;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Abstract {

    private String m_abstract;

    public String getAbstract() {
        return this.m_abstract;
    }

    public Abstract(String nlm, boolean fromPMC) throws Exception {
        JSONObject xmlJSONObj = XML.toJSONObject(nlm);
        if(fromPMC){
            this.m_abstract = this.extractAbstractFromPMCXML(nlm);
        }
        else{
            this.m_abstract = this.extractAbstractFromCermineXML(xmlJSONObj);
        }
    }

    private String extractAbstractFromCermineXML(JSONObject xmlJSONObj) {
        String abstrakt = "";
        try {
            abstrakt = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta").getJSONObject("abstract").getString("p");
        } catch (Exception e) {
            System.out.println("abstract could not be found");
//            e.printStackTrace();
            return "";
        }
        return abstrakt;
    }

    private String extractAbstractFromPMCXML(String nlm) {
        String abstrakt = "";

        //use regex to extract matching parts from xml
        String pattern = "<abstract>.*?<\\/abstract>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(nlm);
        if (m.find()) {
            abstrakt = m.group();
        }
        abstrakt = abstrakt.replaceAll("<[^>]+>", "");
        abstrakt = abstrakt.replaceAll("\n", " ");
        abstrakt = abstrakt.replaceAll("\t", " ");
        abstrakt = abstrakt.trim().replaceAll(" +", " ");

        return abstrakt;
    }
}

