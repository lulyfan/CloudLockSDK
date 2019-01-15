package ut.com.cloudlocksdk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ut.unilink.cloudLock.ScanDevice;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder>{

    private final Context mContext;
    private List<ScanDevice> ScanDevices;

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
        ScanDevice clocdLock = ScanDevices.get(position);
        holder.address.setText(clocdLock.getAddress() + " " + (clocdLock.getName() == null ? "UNKNOW" : clocdLock.getName()));

        if (itemClickListener != null) {
            holder.itemView.setOnClickListener(itemClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return ScanDevices == null ? 0 : ScanDevices.size();
    }

    public void setScanDevices(List<ScanDevice> scanDevices) {
        this.ScanDevices = scanDevices;
        notifyDataSetChanged();
    }

    private View.OnClickListener itemClickListener;

    public void setItemClickListener(View.OnClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public List<ScanDevice> getScanDevices() {
        return ScanDevices;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView address;

        public ViewHolder(View itemView) {
            super(itemView);

            address = itemView.findViewById(R.id.deviceName);
        }
    }
}
