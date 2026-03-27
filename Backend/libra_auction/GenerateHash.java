import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String password = "Tuan@1234";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hash (rounds=10): " + hash);
        
        // Verify it matches
        boolean matches = encoder.matches(password, hash);
        System.out.println("Verification: " + matches);
    }
}
