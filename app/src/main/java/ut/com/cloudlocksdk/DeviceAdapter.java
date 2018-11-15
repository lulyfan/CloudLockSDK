package ut.com.cloudlocksdk;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ut.unilink.cloudLock.UTBleDevice;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder>{

    private final Context mContext;
    private List<UTBleDevice> UTBleDevices;

    public DeviceAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(mContext).inflate(R.layout.item_device, parent, false);
        ViewHolder viewHolder = new ViewHolder(root);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UTBleDevice clocdLock = UTBleDevices.get(position);
        holder.address.setText(clocdLock.getAddress());

        if (itemClickListener != null) {
            holder.itemView.setOnClickListener(itemClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return UTBleDevices == null ? 0 : UTBleDevices.size();
    }

    public void setUTBleDevices(List<UTBleDevice> UTBleDevices) {
        this.UTBleDevices = UTBleDevices;
        notifyDataSetChanged();
    }

    private View.OnClickListener itemClickListener;

    public void setItemClickListener(View.OnClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public List<UTBleDevice> getUTBleDevices() {
        return UTBleDevices;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView address;

        public ViewHolder(View itemView) {
            super(itemView);

            address = itemView.findViewById(R.id.deviceName);
        }
    }
}
