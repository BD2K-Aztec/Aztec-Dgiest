package attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToolName {

    private String m_name;
    private List<String> potential_names;

    public ToolName(String title, List<String> urls) {
        this.potential_names = this.potentialNames(title, urls);

//        System.out.println(this.potential_names);
        if(this.potential_names.isEmpty()){
            this.m_name = "";
        }else {
            this.m_name = this.potential_names.get(0);
        }
    }

    public String getName(){
        return this.m_name;
    }

    private List<String> potentialNames(String title, List<String> urls){
        List<String> names = new ArrayList<>();
        names.addAll(this.extractNameFromRepo(urls));

        String title_name = this.extractFromTitle(title);
        if(!title_name.isEmpty()){
            if(title.contains(":")) {
                names.add(0, title_name);
            }else{
                names.add(title_name);
            }
        }


        return names;
    }


    private String extractFromTitle(String title){
        int period_idx = title.lastIndexOf('.');
        if (period_idx > 0){
            title = title.substring(0, period_idx);
        }

        String tool_name = "";

        String[] word_tokens = title.split(" ");

        if(word_tokens.length < 5){
            return title;
        }

        int colon_idx = title.lastIndexOf(":");
        if(colon_idx > 0){
            return title.substring(0, colon_idx);
        }

        String clean = title.replaceAll("\\P{Print}", "");

        int oneDash_idx = clean.indexOf(" - ");
        if(oneDash_idx > 0){
            return clean.substring(0, oneDash_idx);
        }

        int longDash_idx = title.indexOf("–");
        if(longDash_idx > 0){
            return title.substring(0, longDash_idx);
        }

        int medDash_idx = title.indexOf("—");
        if(medDash_idx > 0){
            return title.substring(0, medDash_idx);
        }

        int doubleDash_idx = title.indexOf("--");
        if(doubleDash_idx > 0){
            return title.substring(0, doubleDash_idx);
        }

        int paren_idx = title.indexOf("(");
        if(paren_idx > 0){
            int end_paren_idx = title.indexOf(")");
            if(end_paren_idx > 0){
                return title.substring(paren_idx+1, end_paren_idx);
            }
        }

        int with_idx = title.lastIndexOf("with");
        if(with_idx > 0){
            String with_name = title.substring(with_idx+5).trim();
            if(with_name.split(" ").length < 3){
                return with_name;
            }
        }

        int using_idx = title.lastIndexOf("using");
        if(using_idx > 0){
            String using_name = title.substring(using_idx+6).trim();
            if(using_name.split(" ").length < 3){
                return using_name;
            }
        }

        String first_word = word_tokens[0];
        if(first_word.equals("The") || first_word.equals("A")){
            first_word = word_tokens[1];
        }

        if(first_word.equals(first_word.toUpperCase())){
            return first_word;
        }else{
            int num_upper = 0;
            int num_changes = 0;

            boolean prev_state = false;
            for (int i = 0; i < first_word.length(); i++) {
                char c =first_word.charAt(i);
                boolean is_upper = 'A'>=c && c<='Z';

                if(prev_state!=is_upper){
                    num_changes++;
                }

                if(is_upper){
                    num_upper++;
                }
            }

            if(num_changes > 2 || num_upper > 2){
                return first_word;
            }
        }

        return tool_name;

    }

    private List<String> extractNameFromRepo(List<String> links){
        List<String> results = new ArrayList<>();

        for(String link: links){
            String repo = "";
            if(link.contains("github")){
                repo = "github";
            }else if(link.contains("sourceforge")){
                repo = "sourceforge";
            }else if(link.contains("bitbucket")){
                repo = "bitbucket";
            }

            if(repo.isEmpty()){
                continue;
            }

            String pattern = "(www\\.)?"+repo+".(com|net|org|io)\\/[\\S]+?\\/[\\w.-]+";
            Pattern regex = Pattern.compile(pattern);

            Matcher match = regex.matcher(link);

            String found_name = "";
            if(match.find()){
                String found_link = match.group(0);
                found_name = found_link.substring(found_link.lastIndexOf("/")+1);
            }else{
                pattern = "[\\w-]+\\."+repo+".(com|net|org|io)?(\\/[\\w-]+)*";
                regex = Pattern.compile(pattern);

                match = regex.matcher(link);
                if(match.find()){
                    String found_link = match.group(0);
                    found_name = found_link.substring(0, found_link.indexOf("."));
                }
            }

            if(found_name.isEmpty()){
                pattern = "(www\\.)?"+repo+".(com|org|net|io)\\/[\\w\\d-]+";
                regex = Pattern.compile(pattern);

                match = regex.matcher(link);
                if(match.find()){
                    String found_link = match.group(0);
                    found_name = found_link.substring(found_link.lastIndexOf("/")+1);
                }
            }

            if(found_name.endsWith(".git")){
                found_name = found_name.substring(0, found_name.length()-4);
            }

            if(!found_name.isEmpty()){
                results.add(found_name);
            }

        }

        return results;
    }
}
