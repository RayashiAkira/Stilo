package com.example.stilo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class EstabelecimentoAdapter extends RecyclerView.Adapter<EstabelecimentoAdapter.ViewHolder> implements Filterable {

    private final Context context;
    private List<Estabelecimento> estabelecimentoList;
    private List<Estabelecimento> estabelecimentoListFull;
    private final OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnItemClickListener {
        void onItemClick(Estabelecimento estabelecimento);
    }

    public EstabelecimentoAdapter(Context context, List<Estabelecimento> estabelecimentoList, OnItemClickListener listener) {
        this.context = context;
        this.estabelecimentoList = estabelecimentoList;
        this.estabelecimentoListFull = new ArrayList<>(estabelecimentoList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_estabelecimento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Estabelecimento estabelecimento = estabelecimentoList.get(position);
        holder.bind(estabelecimento, listener, position, selectedPosition);
    }

    @Override
    public int getItemCount() {
        return estabelecimentoList.size();
    }

    @Override
    public Filter getFilter() {
        return estabelecimentoFilter;
    }

    private final Filter estabelecimentoFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Estabelecimento> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(estabelecimentoListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Estabelecimento item : estabelecimentoListFull) {
                    if (item.getNome().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, @NonNull FilterResults results) {
            if (results.values != null) {
                estabelecimentoList.clear();
                estabelecimentoList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView imageView, checkmark;
        TextView nome, tipo, ratingValue;
        RatingBar ratingBar;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            imageView = itemView.findViewById(R.id.image_estabelecimento);
            nome = itemView.findViewById(R.id.text_nome_estabelecimento);
            tipo = itemView.findViewById(R.id.text_descricao_estabelecimento);
            ratingBar = itemView.findViewById(R.id.rating_bar_estabelecimento);
            ratingValue = itemView.findViewById(R.id.text_rating);
            checkmark = itemView.findViewById(R.id.checkbox_selecionado);
        }

        void bind(final Estabelecimento estabelecimento, final OnItemClickListener listener, final int position, final int selectedPosition) {
            nome.setText(estabelecimento.getNome());
            tipo.setText(estabelecimento.getTipo());
            ratingBar.setRating(estabelecimento.getRating());
            ratingValue.setText(String.valueOf(estabelecimento.getRating()));

            if (estabelecimento.getImageUrl() != null && !estabelecimento.getImageUrl().isEmpty()) {
                Glide.with(context).load(estabelecimento.getImageUrl()).circleCrop().into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder_barbearia);
            }

            itemView.setOnClickListener(v -> {
                if(getAdapterPosition() == RecyclerView.NO_POSITION) return;
                
                int previousSelectedPosition = EstabelecimentoAdapter.this.selectedPosition;
                EstabelecimentoAdapter.this.selectedPosition = getAdapterPosition();

                if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousSelectedPosition);
                }
                notifyItemChanged(EstabelecimentoAdapter.this.selectedPosition);

                listener.onItemClick(estabelecimento);
            });

            // Atualiza a UI baseada na seleção
            if (position == selectedPosition) {
                cardView.setStrokeWidth(6); // Borda mais grossa para indicar seleção
                checkmark.setVisibility(View.VISIBLE);
            } else {
                cardView.setStrokeWidth(2); // Borda normal
                checkmark.setVisibility(View.GONE);
            }
        }
    }

    public void updateList(List<Estabelecimento> newList) {
        estabelecimentoList = newList;
        estabelecimentoListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }
}
