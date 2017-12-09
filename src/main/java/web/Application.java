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

        AppThread app = new AppThread();
        SpringApplication.run(Application.class, args);
//        app.run();
    }
}
