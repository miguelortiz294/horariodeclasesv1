package com.tuapp.horariodeclases;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity; // Agregado: centrado del texto de las pestañas
import android.view.ViewGroup; // Agregado: layout params para tabs
import android.widget.TextView; // Agregado: custom text para tabs
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Agregado: color blanco para tabs
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout; // Agregado: TabLayout
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query; // Agregado: Query para filtros
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tuapp.horariodeclases.modelo.Ramo;
import com.tuapp.horariodeclases.modelo.RamoListItem; // Agregado: lista mixta
import com.tuapp.horariodeclases.modelo.Receso; // Agregado: recesos
import java.text.ParseException; // Agregado: parseo sin java.time
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Agregado: locale para parseo de horas

public class ListarActivity extends AppCompatActivity {

    private static final String TAG = "ListarActivity";
    private RecyclerView recyclerView;
    private RamoAdapter ramoAdapter;
    private List<Ramo> listaRamos;
    private FirebaseFirestore db; // Cambiado: Firestore
    private ListenerRegistration listener; // Agregado: listener en vivo
    private TabLayout tabLayout; // Agregado: tabs de dias
    private String diaSeleccionado = null; // Cambiado: dia seleccionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar);

        db = FirebaseFirestore.getInstance(); // Cambiado: Firestore en lugar de DBHelper

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaRamos = new ArrayList<>();
        ramoAdapter = new RamoAdapter(this, new ArrayList<>()); // Cambiado: adapter acepta lista mixta
        recyclerView.setAdapter(ramoAdapter);

        tabLayout = findViewById(R.id.tab_layout); // Agregado: referencia al TabLayout
        setupTabs(); // Agregado: carga de pestañas L-V

        FloatingActionButton fab = findViewById(R.id.btnAgregar); // Cambiado: id según layout
        fab.setOnClickListener(view -> startActivity(new Intent(ListarActivity.this, AgregarActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarRamos(); // Cambiado: suscripción a Firestore con filtro
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Agregado: liberar listener para evitar fugas
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    private void setupTabs() {
        // Cambiado: solo lunes a viernes, centrado, texto blanco y tamaño mayor
        String[] etiquetas = {"LUN", "MAR", "MIE", "JUE", "VIE"}; // Agregado: abreviaturas mostradas
        String[] dias = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes"}; // Agregado: valores para el filtro
        tabLayout.removeAllTabs();
        tabLayout.setTabMode(TabLayout.MODE_FIXED); // Agregado: tabs fijas
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL); // Cambiado: llenar ancho para evitar recortes

        float textSizeSp = 16f; // Agregado: tamaño de texto un poco mayor
        int padding = (int) (12 * getResources().getDisplayMetrics().density); // Cambiado: padding ajustado para evitar cortes

        for (int i = 0; i < dias.length; i++) {
            String etiqueta = etiquetas[i];
            String dia = dias[i];
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setTag(dia); // Agregado: guardamos el dia completo para el filtro

            TextView textView = new TextView(this); // Agregado: custom view para tamaño y centrado
            textView.setText(etiqueta);
            textView.setTextSize(textSizeSp);
            textView.setSingleLine(true);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)); // Cambiado: ancho/alto llenan el tab para no cortar
            textView.setPadding(padding, padding / 2, padding, padding / 2);
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.white)); // Agregado: texto blanco
            textView.setMinEms(4); // Cambiado: mas espacio para evitar recortes ("MAR")
            tab.setCustomView(textView);

            tabLayout.addTab(tab);
        }
        // Seleccionar el primer dia (Lunes) al inicio
        if (tabLayout.getTabCount() > 0) {
            tabLayout.getTabAt(0).select();
            diaSeleccionado = dias[0];
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String texto = tab.getTag() != null ? tab.getTag().toString() : "";
                diaSeleccionado = texto;
                cargarRamos(); // refrescar con filtro
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab); // recargar si se vuelve a tocar
            }
        });
    }

    private void cargarRamos() {
        if (listener != null) {
            listener.remove();
        }
        Query query = db.collection("ramos");
        if (diaSeleccionado != null && !diaSeleccionado.isEmpty()) {
            query = query.whereEqualTo("dia", diaSeleccionado); // Agregado: filtro por dia
        }

        listener = query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Error al cargar los documentos.", error);
                return;
            }
            if (value == null) return;

            listaRamos.clear();
            for (QueryDocumentSnapshot document : value) {
                Ramo ramo = document.toObject(Ramo.class);
                ramo.setId(document.getId()); // Agregado: guardamos id del documento
                listaRamos.add(ramo);
            }
            ordenarPorHora(listaRamos); // Agregado: orden por hora de inicio
            List<RamoListItem> items = construirItemsConRecesos(listaRamos); // Agregado: inserta recesos
            ramoAdapter.setItems(items); // Agregado: actualiza lista combinada
            Log.d(TAG, "Datos cargados correctamente: " + listaRamos.size() + " ramos (incluye recesos calculados).");
        });
    }

    // Agregado: ordena ramos por horaInicio ascendente
    private void ordenarPorHora(List<Ramo> ramos) {
        ramos.sort((r1, r2) -> {
            Integer m1 = parseHoraEnMinutos(r1.getHoraInicio());
            Integer m2 = parseHoraEnMinutos(r2.getHoraInicio());
            if (m1 == null && m2 == null) return 0;
            if (m1 == null) return 1;
            if (m2 == null) return -1;
            return Integer.compare(m1, m2);
        });
    }

    // Agregado: construye lista mixta con recesos entre ramos consecutivos
    private List<RamoListItem> construirItemsConRecesos(List<Ramo> ramos) {
        List<RamoListItem> items = new ArrayList<>();
        for (int i = 0; i < ramos.size(); i++) {
            Ramo actual = ramos.get(i);
            if (i > 0) {
                Ramo anterior = ramos.get(i - 1);
                Integer finAnterior = parseHoraEnMinutos(anterior.getHoraFin());
                Integer inicioActual = parseHoraEnMinutos(actual.getHoraInicio());
                if (finAnterior != null && inicioActual != null && inicioActual > finAnterior) {
                    long duracion = inicioActual - finAnterior;
                    Receso receso = new Receso(anterior.getHoraFin(), actual.getHoraInicio(), duracion);
                    items.add(RamoListItem.desdeReceso(receso)); // Agregado: inserta receso calculado
                }
            }
            items.add(RamoListItem.desdeRamo(actual));
        }
        return items;
    }

    // Agregado: parsea hora en minutos desde "hh:mm a"
    private Integer parseHoraEnMinutos(String hora) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            sdf.setLenient(false);
            return (int) (sdf.parse(hora).getTime() / (60 * 1000));
        } catch (ParseException | NullPointerException e) {
            return null;
        }
    }
}
