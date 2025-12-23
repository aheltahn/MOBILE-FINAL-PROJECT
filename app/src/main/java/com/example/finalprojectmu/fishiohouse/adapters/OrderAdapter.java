package com.example.finalprojectmu.fishiohouse.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.models.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

// SỬA TÊN LẠI CHO ĐÚNG: OrderAdapter
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final ArrayList<Order> orderList;

    // ================================================================
    // ==== BƯỚC 1: TẠO INTERFACE ĐỂ GIAO TIẾP ====
    // ================================================================
    public interface OnOrderClickListener {
        void onOrderClick(String orderId);
    }

    private final OnOrderClickListener clickListener;
    // ================================================================

    // SỬA HÀM CONSTRUCTOR: Thêm listener vào
    public OrderAdapter(Context context, ArrayList<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.clickListener = listener; // Gán listener
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sửa lại layout cho đúng (tôi đoán là item_order_history hoặc tên tương tự)
        // Dựa vào code cũ của bạn thì nó là R.layout.item_order
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        if (order == null) return;

        // Giữ nguyên logic hiển thị dữ liệu của bạn
        holder.orderId.setText("Mã đơn hàng: #" + order.getOrderId());
        holder.status.setText("Trạng thái: " + order.getStatus());

        if (order.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            holder.date.setText("Ngày đặt: " + sdf.format(order.getCreatedAt()));
        }

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.total.setText("Tổng tiền: " + currencyFormatter.format(order.getTotalPrice()));

        // ================================================================
        // ==== BƯỚC 2: BẮT SỰ KIỆN CLICK VÀO TOÀN BỘ ITEM ====
        // ================================================================
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                // Khi click, gọi hàm trong interface và truyền ID của đơn hàng ra ngoài
                clickListener.onOrderClick(order.getOrderId());
            }
        });
        // ================================================================
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // ViewHolder của bạn giữ nguyên, không cần sửa
    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, date, status, total;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.textViewOrderId);
            date = itemView.findViewById(R.id.textViewOrderDate);
            status = itemView.findViewById(R.id.textViewOrderStatus);
            total = itemView.findViewById(R.id.textViewOrderTotal);
        }
    }
}
