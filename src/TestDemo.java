import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDemo {
    public static void main(String[] args) {
        Pattern p = Pattern.compile("(.*)(x.*)");
        Matcher matcher = p.matcher("-9x^3");
        if (matcher.matches())
            System.out.println(matcher.group(1));
    }
}
