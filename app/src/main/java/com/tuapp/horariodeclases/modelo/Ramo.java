package com.tuapp.horariodeclases.modelo;

public class Ramo {

    private String id; // Agregado: id del documento Firestore
    private int codigo;
    private String nombre;
    private String profesor;
    private String sala;
    private String dia;
    private String horaInicio;
    private String horaFin;

    public Ramo() {}

    public Ramo(int codigo, String nombre, String profesor, String sala, String dia, String horaInicio, String horaFin) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.profesor = profesor;
        this.sala = sala;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    // Agregado: getter/setter para id de Firestore
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // GETTERS & SETTERS

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getProfesor() {
        return profesor;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }
}
