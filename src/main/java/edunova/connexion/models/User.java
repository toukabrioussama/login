package edunova.connexion.models;

public class User {

    private int     id;
    private String  email;
    private String  password;
    private String  nom;
    private String  prenom;
    private String  telephone;
    private boolean actif;
    private int     roleId;
    private String  roleNom;

    public User() {}

    public User(int id, String email, String nom, String prenom,
                String telephone, boolean actif, int roleId, String roleNom) {
        this.id        = id;
        this.email     = email;
        this.nom       = nom;
        this.prenom    = prenom;
        this.telephone = telephone;
        this.actif     = actif;
        this.roleId    = roleId;
        this.roleNom   = roleNom;
    }

    public int     getId()        { return id; }
    public String  getEmail()     { return email; }
    public String  getPassword()  { return password; }
    public String  getNom()       { return nom; }
    public String  getPrenom()    { return prenom; }
    public String  getTelephone() { return telephone; }
    public boolean isActif()      { return actif; }
    public int     getRoleId()    { return roleId; }
    public String  getRoleNom()   { return roleNom; }

    public void setId(int id)              { this.id        = id; }
    public void setEmail(String email)     { this.email     = email; }
    public void setPassword(String p)      { this.password  = p; }
    public void setNom(String nom)         { this.nom       = nom; }
    public void setPrenom(String prenom)   { this.prenom    = prenom; }
    public void setTelephone(String t)     { this.telephone = t; }
    public void setActif(boolean actif)    { this.actif     = actif; }
    public void setRoleId(int roleId)      { this.roleId    = roleId; }
    public void setRoleNom(String roleNom) { this.roleNom   = roleNom; }
}