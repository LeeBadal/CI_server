import java.util.Random;
//class to generate random string of length 50, used for testing HTTP post where SHA is needed.
public class Utils {
    String asciiUpperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String asciiLowerCase = asciiUpperCase.toLowerCase();
    String digits = "1234567890";
    String seedChars = asciiUpperCase + asciiLowerCase + digits;
    int length = 50;
    public String generateRandomString() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        Random rand = new Random();
        while (i < length) {
            sb.append(seedChars.charAt(rand.nextInt(seedChars.length())));
            i++;
        }
        return sb.toString();
    }
}
