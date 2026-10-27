package br.com.autosoft.torque.infrastructure.cadastro.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ClienteRequest(

        @NotBlank String documento,
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Email @Size(max = 254) String email,
        @Size(max = 20) String telefone) {

}
