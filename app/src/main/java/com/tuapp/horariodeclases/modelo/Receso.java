package com.tuapp.horariodeclases.modelo;

public class Receso {
    private String horaInicio;
    private String horaFin;
    private long duracionMinutos;

    public Receso(String horaInicio, String horaFin, long duracionMinutos) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.duracionMinutos = duracionMinutos;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public long getDuracionMinutos() {
        return duracionMinutos;
    }
}
