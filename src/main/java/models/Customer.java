package models;

public class Customer {
    private int id;
    private String fullName, email, phone, address, password;

    public Customer() {}

    public Customer(int id, String fullName, String email,
                    String phone, String address, String password) {
        this.id       = id;
        this.fullName = fullName;
        this.email    = email;
        this.phone    = phone;
        this.address  = address;
        this.password = password;
    }

    public int    getId()       { return id; }
    public String getFullName() { return fullName; }
    public String getEmail()    { return email; }
    public String getPhone()    { return phone; }
    public String getAddress()  { return address; }
    public String getPassword() { return password; }

    public void setId(int id)           { this.id = id; }
    public void setFullName(String n)   { this.fullName = n; }
    public void setEmail(String e)      { this.email = e; }
    public void setPhone(String p)      { this.phone = p; }
    public void setAddress(String a)    { this.address = a; }
    public void setPassword(String p)   { this.password = p; }

    @Override
    public String toString() { return fullName + " <" + email + ">"; }
}
