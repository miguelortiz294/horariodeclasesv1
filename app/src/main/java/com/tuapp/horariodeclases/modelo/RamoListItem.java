package com.tuapp.horariodeclases.modelo;

// Agregado: item mixto para mostrar ramos y recesos en el mismo RecyclerView
public class RamoListItem {
    public enum Tipo { RAMO, RECESO }

    private final Tipo tipo;
    private final Ramo ramo;
    private final Receso receso;

    private RamoListItem(Tipo tipo, Ramo ramo, Receso receso) {
        this.tipo = tipo;
        this.ramo = ramo;
        this.receso = receso;
    }

    public static RamoListItem desdeRamo(Ramo ramo) {
        return new RamoListItem(Tipo.RAMO, ramo, null);
    }

    public static RamoListItem desdeReceso(Receso receso) {
        return new RamoListItem(Tipo.RECESO, null, receso);
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Ramo getRamo() {
        return ramo;
    }

    public Receso getReceso() {
        return receso;
    }
}
