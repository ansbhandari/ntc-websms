package scvishnu7.blogspot.com.meetsms.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import scvishnu7.blogspot.com.meetsms.R;
import scvishnu7.blogspot.com.meetsms.models.Message;

/**
 * Created by vishnu on 12/24/15.
 */
public class HistoryListAdaptor extends ArrayAdapter<Message> {
    private  static class ViewHolder {
        TextView to;
        TextView msg;
        TextView time;
    }

    public HistoryListAdaptor(Context context, ArrayList<Message> msgs){
        super(context, R.layout.history_row_layout,msgs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message msg = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            convertView = layoutInflater.inflate(R.layout.history_row_layout,parent,false);
            viewHolder.time = (TextView)convertView.findViewById(R.id.dateTextView);
            viewHolder.to = (TextView) convertView.findViewById(R.id.toTextView);
            viewHolder.msg = (TextView) convertView.findViewById(R.id.msgTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String dateString = new SimpleDateFormat("yy/MM/dd HH:mm").format(new Date(Long.parseLong(msg.date)));
        viewHolder.time.setText(dateString);
        viewHolder.to.setText(msg.to);
        viewHolder.msg.setText(msg.msg);

        return convertView;
    }

}
