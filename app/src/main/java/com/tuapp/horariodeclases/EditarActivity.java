package com.tuapp.horariodeclases;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot; // Agregado: validacion de solapamientos
import java.text.ParseException; // Agregado: parseo de horas sin java.time
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditarActivity extends AppCompatActivity {

    private TextInputEditText txtNombre, txtProfesor, txtSala, txtHoraInicio, txtHoraFin;
    private Spinner spinnerDia;
    private Button btnActualizar;
    private FirebaseFirestore db; // Cambiado: Firestore en lugar de DBHelper
    private String codigo; // Cambiado: id de documento Firestore
    private final String[] dias = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        Toolbar toolbar = findViewById(R.id.toolbar_editar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance(); // Agregado
        codigo = getIntent().getStringExtra("codigo"); // Cambiado: id Firestore

        txtNombre = findViewById(R.id.txtNombre);
        txtProfesor = findViewById(R.id.txtProfesor);
        txtSala = findViewById(R.id.txtSala);
        txtHoraInicio = findViewById(R.id.txtHoraInicio);
        txtHoraFin = findViewById(R.id.txtHoraFin);
        spinnerDia = findViewById(R.id.spinnerDia);
        btnActualizar = findViewById(R.id.btnActualizar);

        setupSpinner();
        cargarDatos(); // Cambiado: carga desde Firestore

        txtHoraInicio.setOnClickListener(v -> showTimePickerDialog(txtHoraInicio));
        txtHoraFin.setOnClickListener(v -> showTimePickerDialog(txtHoraFin));

        btnActualizar.setOnClickListener(v -> actualizarRamo());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dias);
        spinnerDia.setAdapter(adapter);
    }

    private void cargarDatos() {
        if (codigo == null || codigo.isEmpty()) return;

        db.collection("ramos")
                .document(codigo)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) return;

                    txtNombre.setText(document.getString("nombre"));
                    txtProfesor.setText(document.getString("profesor"));
                    txtSala.setText(document.getString("sala"));
                    txtHoraInicio.setText(document.getString("horaInicio"));
                    txtHoraFin.setText(document.getString("horaFin"));

                    String diaDB = document.getString("dia");
                    if (diaDB != null) {
                        for (int i = 0; i < dias.length; i++) {
                            if (dias[i].equalsIgnoreCase(diaDB)) {
                                spinnerDia.setSelection(i);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show());
    }

    private void showTimePickerDialog(final TextInputEditText timeEditText) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minuteOfHour);

                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    timeEditText.setText(sdf.format(selectedTime.getTime()));
                }, hour, minute, false);

        timePickerDialog.show();
    }

    private void actualizarRamo() {
        if (codigo == null || codigo.isEmpty()) {
            Toast.makeText(this, "Id invalido", Toast.LENGTH_SHORT).show();
            return;
        }

        String nombre = txtNombre.getText().toString();
        String profesor = txtProfesor.getText().toString();
        String sala = txtSala.getText().toString();
        String horaInicio = txtHoraInicio.getText().toString();
        String horaFin = txtHoraFin.getText().toString();
        String dia = spinnerDia.getSelectedItem() != null ? spinnerDia.getSelectedItem().toString() : "";

        if (nombre.isEmpty() || profesor.isEmpty() || sala.isEmpty() || horaInicio.isEmpty() || horaFin.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer inicioMin = parseHoraEnMinutos(horaInicio);
        Integer finMin = parseHoraEnMinutos(horaFin);
        if (inicioMin == null || finMin == null) { // Agregado: validacion de formato
            Toast.makeText(this, "Formato de hora invalido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (inicioMin >= finMin) { // Agregado: validacion de rango
            Toast.makeText(this, "La hora de inicio debe ser menor a la hora de fin", Toast.LENGTH_SHORT).show();
            return;
        }

        verificarConflictosYActualizar(nombre, profesor, sala, dia, horaInicio, horaFin, inicioMin, finMin); // Agregado: validar solapamientos
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void verificarConflictosYActualizar(String nombre, String profesor, String sala, String dia, String horaInicio, String horaFin, int inicioMin, int finMin) {
        // Agregado: consulta previa para evitar choques de horario en el mismo dia (excluyendo el propio)
        db.collection("ramos")
                .whereEqualTo("dia", dia)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        if (doc.getId().equals(codigo)) continue; // saltar el actual
                        String hIni = doc.getString("horaInicio");
                        String hFin = doc.getString("horaFin");
                        Integer ini = parseHoraEnMinutos(hIni != null ? hIni : "");
                        Integer fin = parseHoraEnMinutos(hFin != null ? hFin : "");
                        if (ini == null || fin == null) continue;
                        if (haySolapamiento(inicioMin, finMin, ini, fin)) {
                            Toast.makeText(this, "Ya existe un ramo en ese horario", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("nombre", nombre);
                    updates.put("profesor", profesor);
                    updates.put("sala", sala);
                    updates.put("dia", dia);
                    updates.put("horaInicio", horaInicio);
                    updates.put("horaFin", horaFin);

                    db.collection("ramos")
                            .document(codigo)
                            .update(updates)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Ramo actualizado con éxito", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al validar horario", Toast.LENGTH_SHORT).show());
    }

    // Agregado: utilidades de hora
    private Integer parseHoraEnMinutos(String hora) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            sdf.setLenient(false);
            return (int) (sdf.parse(hora).getTime() / (60 * 1000));
        } catch (ParseException | NullPointerException e) {
            return null;
        }
    }

    private boolean haySolapamiento(int inicioNuevo, int finNuevo, int inicioExistente, int finExistente) {
        return inicioNuevo < finExistente && finNuevo > inicioExistente;
    }
}
