package pojos;

import java.io.Serializable;

//to STOP  the server
public class User implements Serializable {

    private static final long serialVersionUID = 2L;
    private int id;
    public String username;
    String password;



    public User(String username, String password) {
        this.username=username;
        this.password=password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }


    @Override
    public String toString() {
        return "User{" + "username=" + username + ", password=" + password + ", userId=" + id + '}';
    }
}
