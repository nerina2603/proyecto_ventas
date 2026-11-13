package org.iesvdm.modelo;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteComercial {
    private int clienteId;
    private int comercialId;
    private String comercialNombre;
    private LocalDate fecha;
    private Integer prioridad;
}