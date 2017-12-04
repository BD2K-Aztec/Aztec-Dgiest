package attributes.date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PublicationDate {

    private String m_date;

    public String getDate() {  return this.m_date; }

    public PublicationDate(JSONObject xmlJSONObj, boolean fromPMC) throws Exception {
        if (fromPMC) {
            this.m_date = extractDateFromPMCXML(xmlJSONObj);
        }
        else {
            this.m_date = extractDateFromCermineXML(xmlJSONObj);
        }

    }

    private String extractDateFromCermineXML(JSONObject xmlJSONObj) {
        try {
            JSONObject value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
            if (value.has("pub-attributes.date")) {
                int date = value.getJSONObject("pub-attributes.date").getInt("year");
                return "01/01/"+Integer.toString(date);
            }
            else if (value.has("history") && value.getJSONObject("history").has("date")) {
                Object date_obj = value.getJSONObject("history").get("date");
                JSONObject date_obj_json;
                if(date_obj instanceof JSONObject){
                    date_obj_json = (JSONObject)date_obj;
                }else {
                    date_obj_json = ((JSONArray)date_obj).getJSONObject(0);
                }
                String month = Integer.toString(date_obj_json.optInt("month", 1));
                String day = Integer.toString(date_obj_json.optInt("day", 1));
                String year = Integer.toString(date_obj_json.optInt("year", 2000));

                return month + "/" + day + "/" + year;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }

    private String extractDateFromPMCXML(JSONObject xmlJSONObj) {
        JSONObject value = null;
        try {
            value = xmlJSONObj.getJSONObject("article").getJSONObject("front").getJSONObject("article-meta");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (value.has("pub-attributes.date")) {
            Object item = null;
            try {
                item = value.get("pub-attributes.date");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (item instanceof JSONObject){
                JSONObject entry = (JSONObject) item;
                String month = "";
                String day = "";
                String year = "";
                if(entry.has("month"))
                    month = Integer.toString(entry.getInt("month"))+"/";
                if(entry.has("day"))
                    day = Integer.toString(entry.getInt("day"))+"/";
                if(entry.has("year"))
                    year = Integer.toString(entry.getInt("year"));
                String date = month+day+year;
                return date;
            }
            else if (item instanceof JSONArray){
                JSONArray list = (JSONArray) item;
                for(int i=0;i<list.length();i++){
                    JSONObject entry = list.getJSONObject(i);
                    String month = "";
                    String day = "";
                    String year = "";
                    if(entry.has("month"))
                        month = Integer.toString(entry.getInt("month"))+"/";
                    if(entry.has("day"))
                        day = Integer.toString(entry.getInt("day"))+"/";
                    if(entry.has("year")){
                        Object item2 = null;
                        try {
                            item2 = entry.get("year");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (item2 instanceof Integer){
                            year = Integer.toString((Integer)item2);
                        }
                        else if (item2 instanceof JSONObject){
                            JSONObject val = (JSONObject) item2;
                            if(val.has("content")){
                                year = Integer.toString(val.getInt("content"));
                            }
                        }
                    }
                    String date = month+day+year;
                    return date;
                }
            }
        }
        return "0";
    }
}
