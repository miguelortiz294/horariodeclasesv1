package com.tuapp.horariodeclases;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.tuapp.horariodeclases.modelo.Ramo;
import com.tuapp.horariodeclases.modelo.Receso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ListarActivity extends AppCompatActivity {

    private DBHelper db;
    private RecyclerView recyclerView;
    private HorarioAdapter adapter;
    private TabLayout tabLayout;
    private ArrayList<Ramo> todosLosRamos = new ArrayList<>();
    private ArrayList<Object> itemsDelDia = new ArrayList<>(); // Contendrá Ramos y Recesos
    private String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(""); // Esto asegura que no haya título por defecto

        db = new DBHelper(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tabLayout = findViewById(R.id.tab_layout);

        // Configurar pestañas
        for (String dia : dias) {
            tabLayout.addTab(tabLayout.newTab().setText(dia.substring(0, 3).toUpperCase()));
        }

        setupTabLayout();

        findViewById(R.id.btnAgregar).setOnClickListener(v -> {
            startActivity(new Intent(ListarActivity.this, AgregarActivity.class));
        });
    }

    private void cargarTodosLosRamos() {
        todosLosRamos.clear();
        Cursor c = db.listar(); // La consulta ya ordena por hora de inicio
        if (c != null) {
            while (c.moveToNext()) {
                todosLosRamos.add(new Ramo(
                        c.getInt(c.getColumnIndexOrThrow("CODIGO")),
                        c.getString(c.getColumnIndexOrThrow("NOMBRE")),
                        c.getString(c.getColumnIndexOrThrow("PROFESOR")),
                        c.getString(c.getColumnIndexOrThrow("SALA")),
                        c.getString(c.getColumnIndexOrThrow("DIA")),
                        c.getString(c.getColumnIndexOrThrow("HORA_INICIO")),
                        c.getString(c.getColumnIndexOrThrow("HORA_FIN"))
                ));
            }
            c.close();
        }
        // Filtrar por el día seleccionado actualmente
        int selectedTabPosition = tabLayout.getSelectedTabPosition();
        if (selectedTabPosition != -1) {
            actualizarListaParaDia(dias[selectedTabPosition]);
        }
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                actualizarListaParaDia(dias[tab.getPosition()]);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void actualizarListaParaDia(String dia) {
        itemsDelDia.clear();
        ArrayList<Ramo> ramosDelDia = new ArrayList<>();
        for (Ramo ramo : todosLosRamos) {
            if (ramo.getDia().equalsIgnoreCase(dia)) {
                ramosDelDia.add(ramo);
            }
        }

        // --- LÓGICA PARA CALCULAR RECESOS ---
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Ramo ramoAnterior = null;

        for (Ramo ramoActual : ramosDelDia) {
            if (ramoAnterior != null) {
                try {
                    Date finAnterior = sdf.parse(ramoAnterior.getHoraFin());
                    Date inicioActual = sdf.parse(ramoActual.getHoraInicio());

                    long diffInMillis = inicioActual.getTime() - finAnterior.getTime();
                    long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);

                    if (diffInMinutes > 1) {
                        itemsDelDia.add(new Receso(ramoAnterior.getHoraFin(), ramoActual.getHoraInicio(), diffInMinutes));
                    }
                } catch (ParseException e) {
                    e.printStackTrace(); // Manejar error de parseo
                }
            }
            itemsDelDia.add(ramoActual);
            ramoAnterior = ramoActual;
        }

        adapter = new HorarioAdapter(this, itemsDelDia);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarTodosLosRamos(); // Recarga los datos cuando volvemos a la actividad
    }

    // --- Adaptador Personalizado para RecyclerView (maneja Ramos y Recesos) ---
    private class HorarioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_RAMO = 0;
        private static final int VIEW_TYPE_RECESO = 1;

        private Context context;
        private List<Object> items;

        public HorarioAdapter(Context context, List<Object> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getItemViewType(int position) {
            if (items.get(position) instanceof Ramo) {
                return VIEW_TYPE_RAMO;
            } else {
                return VIEW_TYPE_RECESO;
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_RAMO) {
                View view = LayoutInflater.from(context).inflate(R.layout.list_item_ramo, parent, false);
                return new RamoViewHolder(view);
            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.list_item_receso, parent, false);
                return new RecesoViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == VIEW_TYPE_RAMO) {
                Ramo ramo = (Ramo) items.get(position);
                RamoViewHolder ramoHolder = (RamoViewHolder) holder;

                ramoHolder.nombreRamo.setText(ramo.getNombre());
                ramoHolder.profesorRamo.setText(ramo.getProfesor());
                ramoHolder.salaRamo.setText("Sala " + ramo.getSala());
                ramoHolder.horaRamo.setText(ramo.getHoraInicio() + " - " + ramo.getHoraFin());

                ramoHolder.itemView.setOnClickListener(v -> {
                    Intent i = new Intent(context, VerActivity.class);
                    i.putExtra("codigo", ramo.getCodigo());
                    context.startActivity(i);
                });

            } else {
                Receso receso = (Receso) items.get(position);
                RecesoViewHolder recesoHolder = (RecesoViewHolder) holder;

                recesoHolder.recesoDuracion.setText("Receso de " + receso.getDuracionMinutos() + " minutos");
                recesoHolder.recesoHora.setText(receso.getHoraInicio() + " - " + receso.getHoraFin());
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        // ViewHolder para Ramo
        class RamoViewHolder extends RecyclerView.ViewHolder {
            TextView nombreRamo, profesorRamo, salaRamo, horaRamo;
            public RamoViewHolder(View itemView) {
                super(itemView);
                nombreRamo = itemView.findViewById(R.id.nombreRamo);
                profesorRamo = itemView.findViewById(R.id.profesorRamo);
                salaRamo = itemView.findViewById(R.id.salaRamo);
                horaRamo = itemView.findViewById(R.id.horaRamo);
            }
        }

        // ViewHolder para Receso
        class RecesoViewHolder extends RecyclerView.ViewHolder {
            TextView recesoDuracion, recesoHora;
            public RecesoViewHolder(View itemView) {
                super(itemView);
                recesoDuracion = itemView.findViewById(R.id.receso_duracion);
                recesoHora = itemView.findViewById(R.id.receso_hora);
            }
        }
    }
}
