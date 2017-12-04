package web;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

        List<NameUrl> name_list = new ArrayList<>();
        for(Object obj: full_json.keySet()){
            String key_str = (String) obj;
            JSONObject json_obj = full_json.getJSONObject(key_str);

            NameUrl tmp = new NameUrl();
            tmp.name = json_obj.getString("toolName");
            tmp.url = AZTEC_URL+Integer.toString(json_obj.getInt("id"));
            name_list.add(tmp);
        }

        for(File f: fnm_files){
            f.delete();
        }

        model.addAttribute("name_list", name_list);

        System.out.println("Finished processing PDFs");

        return "success";
    }
//    @PostMapping("/")
//    public ResponseEntity<String> post_pdf_to_process(@RequestParam("file") List<MultipartFile> files,
//                                          RedirectAttributes redirectAttributes, Model model) throws Exception {
//
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.add("Content-Type", "application/json; charset=UTF-8");
//
//        ArrayList<MultipartFile> f_files = new ArrayList();
//        ArrayList<File> fnm_files = new ArrayList();
//        ArrayList<String> f_filenames = new ArrayList();
//        for (MultipartFile file : files) {
//            f_files.add(file);
//            f_filenames.add(file.getOriginalFilename());
//
//            File convFile = new File(UPLOAD_PATH+file.getOriginalFilename());
//
//            convFile.createNewFile();
//            FileOutputStream fos = new FileOutputStream(convFile);
//            fos.write(file.getBytes());
//            fos.close();
//            fnm_files.add(convFile);
//        }
//        String result = (new DigestPDF(fnm_files, f_filenames)).getDataString();
//
//        for(File f: fnm_files){
//            f.delete();
//        }
//
//
//        System.out.println("Finished processing PDFs");
//
//        return new ResponseEntity(result, responseHeaders, HttpStatus.OK);
//    }

}
