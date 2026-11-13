package com.example.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;
    private String tipo;
    private BigDecimal precio;
    private String estado;

    @ManyToOne
    private Evento evento;

    @ManyToOne
    private Sector sector;

    protected Entrada() {
    }

    public Entrada(String codigo, String tipo, BigDecimal precio, String estado, Evento evento, Sector sector) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.precio = precio;
        this.estado = estado;
        this.evento = evento;
        this.sector = sector;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }
}