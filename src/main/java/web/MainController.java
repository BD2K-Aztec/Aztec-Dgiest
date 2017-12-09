package web;

import attributes.Attributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pinglab_dev1 on 9/20/17.
 */

@Controller
public class MainController {
    public static String UPLOAD_PATH = ".//pdfs//";
    public final static String AZTEC_URL = "https://dev.aztec.io/AZ";
    @GetMapping("/")
    public String get_upload_page(Model model) throws IOException {
        return "upload";
    }

    class NameUrl {
        public String name;
        public String url;
    }

    @PostMapping("/")
    public String post_pdf_to_process(@RequestParam("file") List<MultipartFile> files,
                                                      RedirectAttributes redirectAttributes, Model model) throws Exception {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");

        ArrayList<MultipartFile> f_files = new ArrayList();
        ArrayList<File> fnm_files = new ArrayList();
        ArrayList<String> f_filenames = new ArrayList();
        for (MultipartFile file : files) {
            f_files.add(file);
            f_filenames.add(file.getOriginalFilename());

            File convFile = new File(file.getOriginalFilename());
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();
            fnm_files.add(convFile);
        }
        DigestPDF result = new DigestPDF(fnm_files, f_filenames);

        JSONObject full_json = result.getData();

        List<NameUrl> name_list = this.convert2list(full_json);

        for(File f: fnm_files){
            f.delete();
        }

        model.addAttribute("name_list", name_list);

        System.out.println("Finished processing PDFs");

        return "success";
    }

@PostMapping(value = "/pmc_id")
public String post_pmcid(@RequestParam("PMC_ID") String pmc_id,
                                               Model model) throws Exception {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("Content-Type", "application/json; charset=UTF-8");
    String[] tokens = pmc_id.split(",");

    JSONObject results = new JSONObject();
    for(String token: tokens){
        String trimmed = token.trim();
        if(trimmed!="") {
            results.put(trimmed, this.fetchFromPMC(token));
        }
    }

    List<NameUrl> name_list = this.convert2list(results);

    model.addAttribute("name_list", name_list);

    return "success";

//    return new ResponseEntity(results.toString(4), responseHeaders, HttpStatus.OK);
}

private List<NameUrl> convert2list(JSONObject full_json){
    List<NameUrl> name_list = new ArrayList<>();
    for(Object obj: full_json.keySet()){
        String key_str = (String) obj;
        JSONObject json_obj = full_json.getJSONObject(key_str);

        NameUrl tmp = new NameUrl();
        if(json_obj.has("toolName")) {
            tmp.name = json_obj.getString("toolName");
            tmp.url = AZTEC_URL + Integer.toString(json_obj.getInt("id"));
            name_list.add(tmp);
        }
    }

    return name_list;
}
private JSONObject fetchFromPMC(String pmc_id){
    //get xml
    String url_link = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pmc&format=xml&id=PMC" + pmc_id;
    String html = getHTML(url_link);

    //check if it's open access
    String pattern = "(?s)<body>.*</body>";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(html);
    boolean flag = false;
    while (m.find( )) {
        flag = true;
    }
    if(!flag){
        JSONObject obj = new JSONObject();
        obj.put("status", "No PMC full text available for "+pmc_id);
        return obj;
    }

    //get rid of reference section
    String html_withoutref = html;
    if(html.contains("<ref-list>")){
        html_withoutref = html.split("<ref-list>")[0];
        html_withoutref += html.split("</ref-list>")[1];
    }


    //get rid of outmost tag
    html_withoutref = html_withoutref.split("<pmc-articleset>")[1];
    html_withoutref = html_withoutref.split("</pmc-articleset>")[0];

    //remove <bold> and <italics> tag
    html_withoutref = html_withoutref.replaceAll("<\\/?bold>","");
    html_withoutref = html_withoutref.replaceAll("<\\/?italic>","");

    Attributes attr = null;
    try {
        attr = new Attributes(html_withoutref, "", true);
        SolrInterface si = new SolrInterface();
        attr.setID(si.newID());
        si.submit(attr);
    }catch(Exception ex){
        ex.printStackTrace();
    }


    return attr.getFinalJSONObject();
}

    // helper function: get HTML content
    private static String getHTML(String urlToRead) {
        try{
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1500);
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            return result.toString();
        }
        catch (Exception e){
            return "";
        }
    }

}
