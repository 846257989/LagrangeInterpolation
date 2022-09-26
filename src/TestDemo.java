import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDemo {
    public static void main(String[] args) {
        Pattern p = Pattern.compile("(.?(\\d+)?)(x\\^)(\\d+)");
        Matcher m1 = p.matcher("-x^1");
        if (m1.matches()){
            int end = m1.end();
            System.out.println(m1.group(1));
            System.out.println(m1.group(3));
            System.out.println(end);
        }
    }
}
