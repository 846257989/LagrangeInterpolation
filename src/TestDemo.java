import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDemo {
    public static void main(String[] args) {
        Matcher matcher = Demo.TEST.matcher("-114509/24x^4");
        if (matcher.matches())
        {
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(3));
        }
    }
}
