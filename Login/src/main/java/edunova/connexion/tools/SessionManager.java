package edunova.connexion.tools;

public class SessionManager {

    private static SessionManager instance;

    private String email;
    private String role;
    private int    userId;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public String getEmail()  { return email;  }
    public String getRole()   { return role;   }
    public int    getUserId() { return userId; }

    public void setEmail(String email)  { this.email  = email;  }
    public void setRole(String role)    { this.role   = role;   }
    public void setUserId(int userId)   { this.userId = userId; }

    public boolean isLoggedIn() {
        return email != null && !email.isEmpty();
    }

    public void clear() {
        email  = null;
        role   = null;
        userId = 0;
    }
}