package com.example.finalprojectmu.fishiohouse.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.models.Voucher;

import java.util.ArrayList;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder> {

    private final ArrayList<Voucher> voucherList;
    private int selectedPosition = -1;
    private final OnVoucherSelectedListener listener;

    public interface OnVoucherSelectedListener {
        void onVoucherSelected(Voucher voucher);
    }

    public VoucherAdapter(ArrayList<Voucher> voucherList, OnVoucherSelectedListener listener) {
        this.voucherList = voucherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VoucherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher, parent, false);
        return new VoucherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.txtCode.setText(voucher.getCode());
        holder.txtDescription.setText(voucher.getDescription());

        holder.radioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
            if (listener != null) {
                listener.onVoucherSelected(voucherList.get(selectedPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    static class VoucherViewHolder extends RecyclerView.ViewHolder {
        TextView txtCode, txtDescription;
        RadioButton radioButton;
        ImageView icon;

        public VoucherViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCode = itemView.findViewById(R.id.txt_voucher_code);
            txtDescription = itemView.findViewById(R.id.txt_voucher_description);
            radioButton = itemView.findViewById(R.id.radio_button_voucher);
            icon = itemView.findViewById(R.id.img_voucher_icon);
        }
    }
}
