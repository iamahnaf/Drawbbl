package GameAuth;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L; // For serialization
    private final String username;
    private final String passwordHash;

    public User(String username, String password) {
        this.username = username;
        // In a real application, you would use a strong hashing algorithm like BCrypt.
        // For simplicity, we are using a basic hash.
        this.passwordHash = String.valueOf(password.hashCode());
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String password) {
        return passwordHash.equals(String.valueOf(password.hashCode()));
    }
}