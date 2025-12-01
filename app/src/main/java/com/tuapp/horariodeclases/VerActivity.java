package com.tuapp.horariodeclases;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class VerActivity extends AppCompatActivity {

    private TextView verProfesor, verSala, verDia, verHoraInicio, verHoraFin;
    private FirebaseFirestore db; // Cambiado: Firestore en lugar de DBHelper
    private String codigo; // Cambiado: id del documento Firestore
    private String nombreRamo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver);

        Toolbar toolbar = findViewById(R.id.toolbar_ver);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance(); // Agregado
        codigo = getIntent().getStringExtra("codigo"); // Cambiado: id Firestore

        verProfesor = findViewById(R.id.ver_profesor);
        verSala = findViewById(R.id.ver_sala);
        verDia = findViewById(R.id.ver_dia);
        verHoraInicio = findViewById(R.id.ver_hora_inicio);
        verHoraFin = findViewById(R.id.ver_hora_fin);
    }

    private void cargarDatos() {
        if (codigo == null || codigo.isEmpty()) {
            Toast.makeText(this, "No se encontró el identificador del ramo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("ramos")
                .document(codigo)
                .get()
                .addOnSuccessListener(this::mostrarDatos)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar el ramo", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void mostrarDatos(DocumentSnapshot document) {
        if (!document.exists()) {
            Toast.makeText(this, "El ramo ya no existe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nombreRamo = document.getString("nombre");
        String profesor = document.getString("profesor");
        String sala = document.getString("sala");
        String dia = document.getString("dia");
        String horaInicio = document.getString("horaInicio");
        String horaFin = document.getString("horaFin");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(nombreRamo);
        }
        verProfesor.setText("Profesor: " + (profesor != null ? profesor : ""));
        verSala.setText("Sala: " + (sala != null ? sala : ""));
        verDia.setText("Día: " + (dia != null ? dia : ""));
        verHoraInicio.setText("Hora Inicio: " + (horaInicio != null ? horaInicio : ""));
        verHoraFin.setText("Hora Fin: " + (horaFin != null ? horaFin : ""));
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
            i.putExtra("codigo", codigo); // Cambiado: pasamos id Firestore
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
                .setPositiveButton("Sí, eliminar", (dialog, which) -> eliminarRamo()) // Cambiado: elimina en Firestore
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarRamo() {
        if (codigo == null || codigo.isEmpty()) {
            Toast.makeText(this, "Id inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("ramos")
                .document(codigo)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Ramo eliminado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos(); // Cambiado: lectura desde Firestore
    }
}
