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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AgregarActivity extends AppCompatActivity {

    private TextInputEditText txtNombre, txtProfesor, txtSala, txtHoraInicio, txtHoraFin;
    private Spinner spinnerDia;
    private Button btnGuardar;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar);

        Toolbar toolbar = findViewById(R.id.toolbar_agregar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DBHelper(this);

        txtNombre = findViewById(R.id.txtNombre);
        txtProfesor = findViewById(R.id.txtProfesor);
        txtSala = findViewById(R.id.txtSala);
        txtHoraInicio = findViewById(R.id.txtHoraInicio);
        txtHoraFin = findViewById(R.id.txtHoraFin);
        spinnerDia = findViewById(R.id.spinnerDia);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Configurar Spinner
        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dias);
        spinnerDia.setAdapter(adapter);

        // Configurar TimePickers
        txtHoraInicio.setOnClickListener(v -> showTimePickerDialog(txtHoraInicio));
        txtHoraFin.setOnClickListener(v -> showTimePickerDialog(txtHoraFin));

        btnGuardar.setOnClickListener(v -> guardarRamo());
    }

    private void showTimePickerDialog(final TextInputEditText timeEditText) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // El último argumento es 'is24HourView'. Ponerlo en 'false' muestra el selector AM/PM.
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
            (view, hourOfDay, minuteOfHour) -> {
                // Usamos un objeto Calendar para formatear la hora correctamente con AM/PM
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minuteOfHour);

                // El formato "hh:mm a" producirá algo como "02:30 PM"
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                timeEditText.setText(sdf.format(selectedTime.getTime()));
            }, hour, minute, false); // <-- Cambiado a false

        timePickerDialog.show();
    }

    private void guardarRamo() {
        String nombre = txtNombre.getText().toString();
        String profesor = txtProfesor.getText().toString();
        String sala = txtSala.getText().toString();
        String horaInicio = txtHoraInicio.getText().toString();
        String horaFin = txtHoraFin.getText().toString();
        String dia = spinnerDia.getSelectedItem().toString();

        if (nombre.isEmpty() || profesor.isEmpty() || sala.isEmpty() || horaInicio.isEmpty() || horaFin.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean ok = db.insertar(nombre, profesor, sala, dia, horaInicio, horaFin);

        if (ok) {
            Toast.makeText(this, "Ramo guardado con éxito", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al guardar el ramo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
