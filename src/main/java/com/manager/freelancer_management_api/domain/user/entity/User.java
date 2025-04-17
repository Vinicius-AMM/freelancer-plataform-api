package com.manager.freelancer_management_api.domain.user.entity;

import com.manager.freelancer_management_api.domain.user.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "id")
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name ="full_name", nullable = false)
    @NotBlank
    private String fullName;

    @Column(name= "document", unique = true, nullable = false)
    private String document;

    @Column(name= "email", unique = true, nullable = false)
    @Email(message = "Invalid E-mail")
    private String email;

    @Column(name ="password", nullable = false)
    private String password;

    @Column(name ="main_role")
    @Enumerated(EnumType.STRING)
    @NotNull
    private UserRole mainUserRole;

    @Column(name ="role_current")
    @Enumerated(EnumType.STRING)
    private UserRole currentUserRole;

    public User(String fullName, String document, String email, String password, UserRole mainUserRole, UserRole currentUserRole) {
        this.fullName = fullName;
        this.document = document;
        this.email = email;
        this.password = password;
        this.mainUserRole = mainUserRole;
        this.currentUserRole = currentUserRole != null ? currentUserRole : mainUserRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(currentUserRole == UserRole.CLIENT) return List.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
        else return List.of(new SimpleGrantedAuthority("ROLE_FREELANCER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
