package com.tuapp.horariodeclases;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot; // Agregado: validacion de solapamientos
import java.text.ParseException; // Agregado: parseo de horas sin java.time
import java.text.SimpleDateFormat;
import java.util.ArrayList; // Agregado: coleccion auxiliar
import java.util.Calendar;
import java.util.HashMap;
import java.util.List; // Agregado: lista auxiliar para validaciones
import java.util.Locale;
import java.util.Map;

public class AgregarActivity extends AppCompatActivity {

    private TextInputEditText txtNombre, txtProfesor, txtSala, txtHoraInicio, txtHoraFin;
    private Spinner spinnerDia; // Agregado: spinner de dias
    private Button btnGuardar;
    private FirebaseFirestore db; // Cambiado: Firestore en lugar de DBHelper
    private final String[] dias = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes"}; // Agregado: dias disponibles

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar);

        db = FirebaseFirestore.getInstance(); // Cambiado: uso de Firestore

        txtNombre = findViewById(R.id.txtNombre);
        txtProfesor = findViewById(R.id.txtProfesor);
        txtSala = findViewById(R.id.txtSala);
        txtHoraInicio = findViewById(R.id.txtHoraInicio);
        txtHoraFin = findViewById(R.id.txtHoraFin);
        spinnerDia = findViewById(R.id.spinnerDia); // Agregado
        btnGuardar = findViewById(R.id.btnGuardar);

        setupSpinner(); // Agregado: inicializacion del spinner

        txtHoraInicio.setOnClickListener(v -> showTimePickerDialog(txtHoraInicio)); // Agregado: selector de hora inicio
        txtHoraFin.setOnClickListener(v -> showTimePickerDialog(txtHoraFin)); // Agregado: selector de hora fin

        btnGuardar.setOnClickListener(v -> {
            String nombre = txtNombre.getText().toString().trim();
            String profesor = txtProfesor.getText().toString().trim();
            String sala = txtSala.getText().toString().trim();
            String horaInicio = txtHoraInicio.getText().toString().trim();
            String horaFin = txtHoraFin.getText().toString().trim();
            String dia = spinnerDia.getSelectedItem() != null ? spinnerDia.getSelectedItem().toString() : "";

            if (nombre.isEmpty() || profesor.isEmpty() || sala.isEmpty() || horaInicio.isEmpty() || horaFin.isEmpty() || dia.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Integer inicioMin = parseHoraEnMinutos(horaInicio);
            Integer finMin = parseHoraEnMinutos(horaFin);
            if (inicioMin == null || finMin == null) { // Agregado: validacion de formato
                Toast.makeText(this, "Formato de hora invalido", Toast.LENGTH_SHORT).show();
                return;
            }
            if (inicioMin >= finMin) { // Agregado: validacion de rango horario
                Toast.makeText(this, "La hora de inicio debe ser menor a la hora de fin", Toast.LENGTH_SHORT).show();
                return;
            }

            verificarConflictosYGuardar(nombre, profesor, sala, dia, horaInicio, horaFin, inicioMin, finMin); // Agregado: validacion de solapamientos
        });
    }

    private void setupSpinner() {
        // Agregado: carga de dias en el spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dias);
        spinnerDia.setAdapter(adapter);
    }

    private void showTimePickerDialog(final TextInputEditText timeEditText) {
        // Agregado: selector de hora en formato AM/PM
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

    private void verificarConflictosYGuardar(String nombre, String profesor, String sala, String dia, String horaInicio, String horaFin, int inicioMin, int finMin) {
        // Agregado: consulta previa para evitar choques de horario en el mismo dia
        db.collection("ramos")
                .whereEqualTo("dia", dia)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Map<String, Object>> existentes = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        existentes.add(doc.getData());
                    }

                    for (Map<String, Object> data : existentes) {
                        String hIni = data.get("horaInicio") != null ? data.get("horaInicio").toString() : "";
                        String hFin = data.get("horaFin") != null ? data.get("horaFin").toString() : "";
                        Integer ini = parseHoraEnMinutos(hIni);
                        Integer fin = parseHoraEnMinutos(hFin);
                        if (ini == null || fin == null) continue;
                        if (haySolapamiento(inicioMin, finMin, ini, fin)) {
                            Toast.makeText(this, "Ya existe un ramo en ese horario", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    guardarRamo(nombre, profesor, sala, dia, horaInicio, horaFin); // Agregado: guardar solo si no hay conflicto
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al validar horario", Toast.LENGTH_SHORT).show());
    }

    private void guardarRamo(String nombre, String profesor, String sala, String dia, String horaInicio, String horaFin) {
        Map<String, Object> ramo = new HashMap<>();
        ramo.put("nombre", nombre);
        ramo.put("profesor", profesor);
        ramo.put("sala", sala);
        ramo.put("dia", dia);
        ramo.put("horaInicio", horaInicio);
        ramo.put("horaFin", horaFin);

        db.collection("ramos")
                .add(ramo)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Ramo guardado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar el ramo", Toast.LENGTH_SHORT).show());
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
        // Se solapa si hay interseccion en el intervalo
        return inicioNuevo < finExistente && finNuevo > inicioExistente;
    }
}
