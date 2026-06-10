// package net.zalduaxa.backend.model.requestUser;

// import com.fasterxml.jackson.annotation.JsonProperty;

// import jakarta.validation.constraints.Email;
// import jakarta.validation.constraints.NotBlank;
// import jakarta.validation.constraints.Size;

// public class RequestUser {

//     public interface Signup {}
//     public interface Login {}

//     @NotBlank(message = "Username is required", groups = { Signup.class, Login.class })
//     @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters", groups = { Signup.class })
//     private String username;

//     @Size(max = 255, message = "Full name must be at most 255 characters", groups = { Signup.class })
//     private String fullName;

//     @NotBlank(message = "Email is required", groups = { Signup.class })
//     @Email(message = "Email must be valid", groups = { Signup.class })
//     @Size(max = 255, message = "Email must be at most 255 characters", groups = { Signup.class })
//     private String email;

//     @NotBlank(message = "Password is required", groups = { Signup.class, Login.class })
//     @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters", groups = { Signup.class })
//     private String password;

//     @JsonProperty("repeated_password")
//     @NotBlank(message = "Repeated password is required", groups = { Signup.class })
//     private String repeatedPassword;

//     public RequestUser() {}

//     public String getUsername() {
//         return username;
//     }

//     public void setUsername(String username) {
//         this.username = username;
//     }

//     public String getFullName() {
//         return fullName;
//     }

//     public void setFullName(String fullName) {
//         this.fullName = fullName;
//     }

//     public String getEmail() {
//         return email;
//     }

//     public void setEmail(String email) {
//         this.email = email;
//     }

//     public String getPassword() {
//         return password;
//     }

//     public void setPassword(String password) {
//         this.password = password;
//     }

//     public String getRepeatedPassword() {
//         return repeatedPassword;
//     }

//     public void setRepeatedPassword(String repeatedPassword) {
//         this.repeatedPassword = repeatedPassword;
//     }

//     public String getRepeated_password() {
//         return repeatedPassword;
//     }

//     public void setRepeated_password(String repeatedPassword) {
//         this.repeatedPassword = repeatedPassword;
//     }
// }