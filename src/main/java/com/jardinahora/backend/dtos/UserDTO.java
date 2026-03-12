package com.jardinahora.backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail deve ter um formato válido")
    private String username;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    private String password;

    @NotBlank(message = "Papel do usuário é obrigatório")
    private String role;

    public String checkProperties() throws IllegalAccessException {
        for(Field f : getClass().getDeclaredFields()){
            if(f.get(this) == null){
                String[] arr = f.toString().split("\\.");
                String t = arr[arr.length - 1];
                if(t.equalsIgnoreCase("username")
                        || t.equalsIgnoreCase("password")
                        || t.equalsIgnoreCase("role")
                ){
                    return t;
                }
            }
        }
        return null;
    }

}