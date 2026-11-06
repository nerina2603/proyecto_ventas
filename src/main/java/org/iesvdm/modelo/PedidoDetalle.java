package org.iesvdm.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoDetalle {
    private int id;
    private BigDecimal total;
    private LocalDate fecha;
    private int clienteId;
    private int comercialId;
    private String clienteNombre;
    private String comercialNombre;
}