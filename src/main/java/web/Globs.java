package web;

public class Globs {

    private static final String basic_java = "/Users/pinglab_dev1/IDEAProjects/Aztec-Digest/src/main/java/extraction/";
    private static final String agency_path = "/Users/pinglab_dev1/IDEAProjects/Aztec-Digest/lib/agency_names.txt";
    private static final String serializedClassifier = "/Users/pinglab_dev1/IDEAProjects/Aztec-Digest/lib/stanford-ner-2017-06-09/classifiers/english.conll.4class.distsim.crf.ser.gz";

    private static final String en_path = basic_java + "name/en.txt";
    private static final String stop_path = basic_java + "name/stop.txt";
    private static final String mesh_path = basic_java + "name/mesh_terms.txt";
    private static final String abs_summ_dir = basic_java + "summary/abs_to_summ/";
    private static final String svm_lib_path = basic_java + "summary/abstract_to_summary.py";

    private static final String EMAIL_ADDRESS = "aztec.digest@gmail.com";
    private static final String EMAIL_PASSWORD = "ucla2015";
    private static final String smtp_properties = "/Users/pinglab_dev1/IDEAProjects/Aztec-Digest/src/main/resources/smtp.properties";
    private static final String json_response_path = "/Users/pinglab_dev1/IDEAProjects/Aztec-Digest/response.json";
    private static final String CACHED_TREE_MAP = "/Users/pinglab_dev1/IDEAProjects/Aztec-Digest/src/main/java/extraction/funding/cached_tree_map.json";
    private static final String EMAIL_HTML_PATH = "/Users/pinglab_dev1/IDEAProjects/Aztec-Digest/src/main/resources/templates/email.html";
    private static final String imap_protocol = "imap";

    //////////////////////////////

//    private static final String basic_java = "AZtools/src/main/java/extraction/";
//    private static final String agency_path = "AZtools/lib/agency_names.txt";
//    private static final String serializedClassifier = "AZtools/lib/stanford-ner-2017-06-09/classifiers/english.conll.4class.distsim.crf.ser.gz";
//
//    private static final String en_path = basic_java + "name/en.txt";
//    private static final String stop_path = basic_java + "name/stop.txt";
//    private static final String mesh_path = basic_java + "name/mesh_terms.txt";
//    private static final String abs_summ_dir = basic_java + "summary/abs_to_summ/";
//    private static final String svm_lib_path = basic_java + "summary/abstract_to_summary.py";
//
//    private static final String EMAIL_ADDRESS = "email@gmail.com";
//    private static final String EMAIL_PASSWORD = "password";
//    private static final String smtp_properties = "AZtools/src/main/resources/smtp.properties";
//    private static final String json_response_path = "AZtools/response.json";
//    private static final String CACHED_TREE_MAP = "AZtools/src/main/java/extraction/funding/cached_tree_map.json";
//    private static final String EMAIL_HTML_PATH = "AZtools/src/main/resources/templates/email.html";
//    private static final String imap_protocol = "imaps";

    public static String get_en_path() {
        return en_path;
    }

    public static String get_stop_path() {
        return stop_path;
    }

    public static String get_mesh_path() {
        return mesh_path;
    }

    public static String get_abs_summ_dir() {
        return abs_summ_dir;
    }

    public static String get_svm_lib_path() {
        return svm_lib_path;
    }

    public static String getAgencyNamesFileName() {
        return agency_path;
    }

    public static String getSerializedClassifier() {
        return serializedClassifier;
    }

    public static String get_smtp_properties() {
        return smtp_properties;
    }

    public static String get_json_response_path() {
        return json_response_path;
    }

    public static String get_email_addr() {
        return EMAIL_ADDRESS;
    }

    public static String get_email_pass() {
        return EMAIL_PASSWORD;
    }

    public static String get_cached_tree_map() {
        return CACHED_TREE_MAP;
    }

    public static String getEmailHTMLPath() {
        return EMAIL_HTML_PATH;
    }

    public static String getImap_protocol() {
        return imap_protocol;
    }
}
