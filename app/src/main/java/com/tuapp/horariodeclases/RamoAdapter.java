package com.tuapp.horariodeclases;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tuapp.horariodeclases.modelo.Ramo;
import com.tuapp.horariodeclases.modelo.RamoListItem; // Agregado: item mixto ramo/receso
import com.tuapp.horariodeclases.modelo.Receso;
import java.util.ArrayList;
import java.util.List;

public class RamoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_RAMO = 0;
    private static final int TYPE_RECESO = 1;

    private final Context context;
    private List<RamoListItem> items; // Cambiado: lista de ramos y recesos

    public RamoAdapter(Context context, List<RamoListItem> items) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();
    }

    // Agregado: permite reemplazar la lista sin recrear el adapter
    public void setItems(List<RamoListItem> nuevosItems) {
        this.items = nuevosItems != null ? nuevosItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        RamoListItem item = items.get(position);
        return item.getTipo() == RamoListItem.Tipo.RECESO ? TYPE_RECESO : TYPE_RAMO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_RECESO) {
            View view = inflater.inflate(R.layout.list_item_receso, parent, false);
            return new RecesoViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.list_item_ramo, parent, false);
            return new RamoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RamoListItem item = items.get(position);
        if (holder instanceof RamoViewHolder) {
            Ramo ramo = item.getRamo();
            if (ramo == null) return;
            RamoViewHolder rh = (RamoViewHolder) holder;
            rh.nombreRamo.setText(ramo.getNombre());
            String inicio = ramo.getHoraInicio() != null ? ramo.getHoraInicio() : "";
            String fin = ramo.getHoraFin() != null ? ramo.getHoraFin() : "";
            String horario = inicio.isEmpty() && fin.isEmpty() ? "" : (inicio + (fin.isEmpty() ? "" : " - " + fin));
            rh.horaRamo.setText(horario.trim());
            rh.profesorRamo.setText(ramo.getProfesor());
            rh.salaRamo.setText(ramo.getSala());

            rh.itemView.setOnClickListener(v -> {
                // Cambiado: abre detalle usando el id del documento Firestore
                Intent intent = new Intent(context, VerActivity.class);
                intent.putExtra("codigo", ramo.getId());
                context.startActivity(intent);
            });
        } else if (holder instanceof RecesoViewHolder) {
            Receso receso = item.getReceso();
            if (receso == null) return;
            RecesoViewHolder rvh = (RecesoViewHolder) holder;
            rvh.recesoDuracion.setText("Receso de " + formatearDuracion(receso.getDuracionMinutos())); // Cambiado: formato horas/minutos
            rvh.recesoHora.setText(receso.getHoraInicio() + " - " + receso.getHoraFin());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RamoViewHolder extends RecyclerView.ViewHolder {
        TextView nombreRamo, horaRamo, profesorRamo, salaRamo;

        public RamoViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreRamo = itemView.findViewById(R.id.nombreRamo);
            horaRamo = itemView.findViewById(R.id.horaRamo);
            profesorRamo = itemView.findViewById(R.id.profesorRamo);
            salaRamo = itemView.findViewById(R.id.salaRamo);
        }
    }

    static class RecesoViewHolder extends RecyclerView.ViewHolder {
        TextView recesoDuracion, recesoHora;

        public RecesoViewHolder(@NonNull View itemView) {
            super(itemView);
            recesoDuracion = itemView.findViewById(R.id.receso_duracion);
            recesoHora = itemView.findViewById(R.id.receso_hora);
        }
    }

    // Agregado: formato legible de la duración del receso
    private String formatearDuracion(long minutos) {
        if (minutos <= 0) return "0 minutos";
        long horas = minutos / 60;
        long mins = minutos % 60;
        StringBuilder sb = new StringBuilder();
        if (horas > 0) {
            sb.append(horas).append(horas == 1 ? " hora" : " horas");
            if (mins > 0) sb.append(" ");
        }
        if (mins > 0) {
            sb.append(mins).append(mins == 1 ? " minuto" : " minutos");
        }
        return sb.toString();
    }
}
