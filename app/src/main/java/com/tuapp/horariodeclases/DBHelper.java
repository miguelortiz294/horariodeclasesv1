package com.tuapp.horariodeclases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "HORARIO.db";
    public static final String TABLE = "RAMOS";
    private static final int DB_VERSION = 3; // <-- Versión incrementada

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS RAMOS (" +
                "CODIGO INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "NOMBRE TEXT, " +
                "PROFESOR TEXT, " +
                "SALA TEXT, " +
                "DIA TEXT, " +
                "HORA_INICIO TEXT, " + // Nueva columna
                "HORA_FIN TEXT)");      // Nueva columna
    }

    @Override

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public boolean insertar(String nombre, String profesor, String sala, String dia, String horaInicio, String horaFin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("NOMBRE", nombre);
        cv.put("PROFESOR", profesor);
        cv.put("SALA", sala);
        cv.put("DIA", dia);
        cv.put("HORA_INICIO", horaInicio);
        cv.put("HORA_FIN", horaFin);

        return db.insert(TABLE, null, cv) != -1;
    }

    public Cursor listar() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Ordenar por HORA_INICIO para la lógica de recesos
        return db.rawQuery("SELECT CODIGO, NOMBRE, PROFESOR, SALA, DIA, HORA_INICIO, HORA_FIN FROM RAMOS ORDER BY HORA_INICIO ASC", null);
    }

    public Cursor ver(int codigo) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM RAMOS WHERE CODIGO = " + codigo, null);
    }

    public boolean editar(int codigo, String nombre, String profesor, String sala, String dia, String horaInicio, String horaFin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("NOMBRE", nombre);
        cv.put("PROFESOR", profesor);
        cv.put("SALA", sala);
        cv.put("DIA", dia);
        cv.put("HORA_INICIO", horaInicio);
        cv.put("HORA_FIN", horaFin);

        return db.update(TABLE, cv, "CODIGO=?", new String[]{String.valueOf(codigo)}) > 0;
    }

    public boolean eliminar(int codigo) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE, "CODIGO=?", new String[]{String.valueOf(codigo)}) > 0;
    }
}
