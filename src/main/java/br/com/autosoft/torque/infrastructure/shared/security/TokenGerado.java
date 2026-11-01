package br.com.autosoft.torque.infrastructure.shared.security;

import java.time.Instant;

record TokenGerado(String valor, Instant expiraEm) {

}
