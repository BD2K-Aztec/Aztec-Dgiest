package attributes;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {


    public static List<String> findEmail(String text){
        List<String> results = new ArrayList<>();

        String pattern = "[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";
        Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(text);

        while (m.find()) {
            String contact = m.group();
            results.add(contact);
        }

        return results;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }


    public static JSONObject readJsonFromUrl(String url) throws IOException {
        InputStream is;
        JSONObject json = null;
        try {
            is = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            json = new JSONObject(jsonText);
        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Cannot access "+url);
        }
        return json;
    }

    public static JSONObject getJSON(String url_str){
        try {

            URL url = new URL(url_str);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            String json_response_str = content.toString();

            JSONObject json_response = new JSONObject(json_response_str);

            return json_response;
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
