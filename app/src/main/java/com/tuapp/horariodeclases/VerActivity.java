package com.tuapp.horariodeclases;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class VerActivity extends AppCompatActivity {

    private TextView verProfesor, verSala, verDia, verHoraInicio, verHoraFin;
    private DBHelper db;
    private int codigo;
    private String nombreRamo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver);

        Toolbar toolbar = findViewById(R.id.toolbar_ver);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DBHelper(this);
        codigo = getIntent().getIntExtra("codigo", -1);

        verProfesor = findViewById(R.id.ver_profesor);
        verSala = findViewById(R.id.ver_sala);
        verDia = findViewById(R.id.ver_dia);
        verHoraInicio = findViewById(R.id.ver_hora_inicio);
        verHoraFin = findViewById(R.id.ver_hora_fin);
    }

    private void cargarDatos() {
        if (codigo == -1) return;

        Cursor c = db.ver(codigo);
        if (c.moveToFirst()) {
            nombreRamo = c.getString(c.getColumnIndexOrThrow("NOMBRE"));
            String profesor = c.getString(c.getColumnIndexOrThrow("PROFESOR"));
            String sala = c.getString(c.getColumnIndexOrThrow("SALA"));
            String dia = c.getString(c.getColumnIndexOrThrow("DIA"));
            String horaInicio = c.getString(c.getColumnIndexOrThrow("HORA_INICIO"));
            String horaFin = c.getString(c.getColumnIndexOrThrow("HORA_FIN"));

            getSupportActionBar().setTitle(nombreRamo);
            verProfesor.setText("Profesor: " + profesor);
            verSala.setText("Sala: " + sala);
            verDia.setText("Día: " + dia);
            verHoraInicio.setText("Hora Inicio: " + horaInicio);
            verHoraFin.setText("Hora Fin: " + horaFin);

        } else {
            Toast.makeText(this, "El ramo ya no existe", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ver_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.menu_editar) {
            Intent i = new Intent(this, EditarActivity.class);
            i.putExtra("codigo", codigo);
            startActivity(i);
            return true;
        } else if (id == R.id.menu_eliminar) {
            mostrarDialogoDeConfirmacion();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoDeConfirmacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Ramo")
                .setMessage("¿Estás seguro de que quieres eliminar '" + nombreRamo + "'?")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    boolean ok = db.eliminar(codigo);
                    if (ok) {
                        Toast.makeText(this, "Ramo eliminado", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos();
    }
}
