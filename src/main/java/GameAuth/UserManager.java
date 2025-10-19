package GameAuth;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserManager {
    private static final String USER_FILE = "users.dat";
    private List<User> users;

    public UserManager() {
        this.users = loadUsers();
    }

    @SuppressWarnings("unchecked")
    private List<User> loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_FILE))) {
            return (List<User>) ois.readObject();
        } catch (FileNotFoundException e) {
            return new ArrayList<>(); // File doesn't exist yet, return empty list
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<User> findUser(String username) {
        return users.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public boolean registerUser(String username, String password) {
        if (findUser(username).isPresent()) {
            return false; // User already exists
        }
        users.add(new User(username, password));
        saveUsers();
        return true;
    }

    public boolean authenticate(String username, String password) {
        return findUser(username)
                .map(user -> user.checkPassword(password))
                .orElse(false); // User not found or password incorrect
    }
}