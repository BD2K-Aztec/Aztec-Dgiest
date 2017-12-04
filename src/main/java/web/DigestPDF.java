package web;


import attributes.Attributes;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.json.JSONObject;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.tools.timeout.TimeoutException;

import java.io.*;
import java.util.*;

/**
 * Created by pinglab_dev1 on 9/20/17.
 */
public class DigestPDF {

    private long cermine_time;
    private long aztools_time;
    private long total_time;

    private JSONObject data;
    private JSONObject metadata;
    private JSONObject final_json_object;

    private String data_string;
    private String metadata_string;
    private String final_json_string;

    private String last_filename;

    private Map<String, Attributes> metadata_info = new HashMap<>();

    public DigestPDF(ArrayList<File> files, ArrayList<String> filenames) {
        data = new JSONObject();
        metadata = new JSONObject();
        final_json_object = new JSONObject();

        cermine_time = 0;
        aztools_time = 0;
        total_time = 0;

        Calendar cermine_start, cermine_end;
        Calendar digest_start, digest_end;
        Calendar clock_start = Calendar.getInstance(), clock_end;

        int spacesToIndentEachLevel = 4;

        SolrInterface si = new SolrInterface();

        try {
            for (int i = 0; i < files.size(); i++) {
                try {
                    cermine_start = Calendar.getInstance();
                    ContentExtractor extractor = new ContentExtractor();
                    InputStream inputStream = new FileInputStream(files.get(i));
                    extractor.setPDF(inputStream);

                    Element nlmMetadata = extractor.getMetadataAsNLM();
                    Element nlmFullText = extractor.getBodyAsNLM(null);
                    Element nlmContent = new Element("article");
                    for (Object ns : nlmFullText.getAdditionalNamespaces()) {
                        if (ns instanceof Namespace) {
                            nlmContent.addNamespaceDeclaration((Namespace) ns);
                        }
                    }

                    Element meta = (Element) nlmMetadata.getChild("front").clone();
                    nlmContent.addContent(meta);
                    nlmContent.addContent(nlmFullText);
                    String nlm = new XMLOutputter().outputString(nlmContent);
                    cermine_end = Calendar.getInstance();
                    cermine_time += cermine_end.getTimeInMillis() - cermine_start.getTimeInMillis();

                    digest_start = Calendar.getInstance();
                    Attributes attr = new Attributes(nlm, filenames.get(i), false);
                    attr.setID(si.newID());
                    metadata_info.put(filenames.get(i), attr);
                    digest_end = Calendar.getInstance();
                    aztools_time += digest_end.getTimeInMillis() - digest_start.getTimeInMillis();
                    data.put(filenames.get(i), attr.getFinalJSONObject());

                    String pubDOI = attr.getDOI();
                    List<String> pubDOIs = new ArrayList<>();
                    if (!pubDOI.equals("")) {
                        pubDOIs.add(pubDOI);
                    }

                    if(si.submit(attr)){
                        System.out.println("Successfully inserted into Solr");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            clock_end = Calendar.getInstance();
            total_time = clock_end.getTimeInMillis() - clock_start.getTimeInMillis();

            metadata.put("num_pdfs", files.size());
            metadata.put("total_time", total_time);
            metadata.put("cermine_time", total_time);
            metadata.put("aztools_time", aztools_time);
            metadata_string = metadata.toString(spacesToIndentEachLevel);

            data_string = data.toString(spacesToIndentEachLevel);

            final_json_object.put("metadata", metadata);
            final_json_object.put("data", data);
            final_json_string = final_json_object.toString(spacesToIndentEachLevel).replace("\\\"", "\"");
        }
        catch (TimeoutException e) {
            e.printStackTrace();
            cermine_time = 0;
            aztools_time = 0;
            total_time = 0;

            metadata.put("num_pdfs", files.size());
            metadata.put("status", "failure");
            metadata_string = metadata.toString(spacesToIndentEachLevel);
            data_string = "";

            final_json_object.put("metadata", metadata);
            final_json_object.put("data", data);
            final_json_string = final_json_object.toString(spacesToIndentEachLevel);
            final_json_string = final_json_string.replace("\\\"", "\"");
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    public String getFinalString() {
        return final_json_string;
    }

    public String getDataString() {
        return data_string;
    }

    public String getMetadataString() {
        return metadata_string;
    }

    ///////////////////////////////////////////////////////////////////////////

    public JSONObject getData() {
        return data;
    }

    public JSONObject getMetaData() {
        return metadata;
    }

    public JSONObject getFinalJsonObject() {
        return final_json_object;
    }

    ///////////////////////////////////////////////////////////////////////////

    public long getCermineTime() {
        return cermine_time;
    }

    public long getRefineTime() {
        return aztools_time;
    }

    public long getTotalTime() {
        return total_time;
    }

    public Map<String, Attributes> getMetadata_info() {
        return metadata_info;
    }

    public String getFileName() {
        return last_filename;
    }

}
