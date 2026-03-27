import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        
        // Test different passwords
        String[] passwords = { "tuan", "tuan1234", "Tuan@1234", "password" };
        
        System.out.println("=== BCrypt Hash Generator (10 rounds) ===\n");
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            boolean matches = encoder.matches(password, hash);
            
            System.out.println("Password: " + password);
            System.out.println("Hash:     " + hash);
            System.out.println("Verify:   " + matches);
            System.out.println("SQL:      INSERT INTO public.tai_khoan_password (id, password_hash, salt) VALUES ('account-1', '" + hash + "', NULL);");
            System.out.println();
        }
    }
}
