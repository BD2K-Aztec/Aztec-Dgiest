package web;

/**
 * Created by pinglab_dev1 on 9/20/17.
 */
import attributes.CodeStats;
import attributes.ToolName;
import attributes.Utilities;
import attributes.url.Url;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.xmlbeans.impl.xb.ltgfmt.Code;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
//        CodeStats cs = new CodeStats("www.github.com/PapenfussLab", "Clove");
//        System.out.println("link:"+cs.getRepo_link());
//        System.out.println("name:"+cs.getRepo_name());

//        SolrInterface si = new SolrInterface();
//        System.out.println(si.checkDup("10.1021/acs.analchem.6b04604", "https://github.com/mgleeming/Xenophile"));
//        System.out.println(si.getID());
        DupDetector dd = new DupDetector();
        AppThread app = new AppThread();
        SpringApplication.run(Application.class, args);
        app.run();
    }
}
