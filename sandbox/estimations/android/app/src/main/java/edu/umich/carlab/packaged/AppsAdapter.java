package edu.umich.carlab.packaged;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

import edu.umich.carlab.Registry;

// https://www.raywenderlich.com/127544/android-gridview-getting-started
public class AppsAdapter extends BaseAdapter {
    List<AppModel> appNames;
    Context mContext;
    final int ROW_HEIGHT = 350;

    static public class AppModel {
        public String name;
        public Set<Registry.Information> inputInformation;
        public AppState state;
        public AppModel(String name, Set<Registry.Information> ii, AppState state) {
            inputInformation = ii;
            this.name = name;
            this.state = state;
        }
    }


    public enum AppState {
        DATA,
        ACTIVE,
        ERROR,
        INACTIVE,
        PROCESSING
    };

    // 1
    public AppsAdapter(Context context, List<AppModel> appNames) {
        this.mContext = context;
        this.appNames = appNames;
    }

    // 2
    @Override
    public int getCount() {
        return appNames.size();
    }

    // 3
    @Override
    public long getItemId(int position) {
        return 0;
    }

    // 4
    @Override
    public Object getItem(int position) {
        return null;
    }

    // 5
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1
        final AppModel app = appNames.get(position);

        // 2
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.carlab_app_icon, null);
            convertView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, ROW_HEIGHT));
        }

        // 3
        final TextView appName = convertView.findViewById(R.id.appIconName);
        final LinearLayout backgroundLayout = convertView.findViewById(R.id.appIconBackground);

        // 4
        appName.setText(app.name);
        int drawableId = R.drawable.background_gray;

        // If CL Service is running, we show all colors. Otherwise we only show the active/inactive colors

        switch (app.state) {
            case ACTIVE:
                drawableId = R.drawable.background_white;
                break;
            case INACTIVE:
                drawableId = R.drawable.background_gray;
                break;
            case DATA:
                drawableId = R.drawable.background_green;
                break;
            case ERROR:
                drawableId = R.drawable.background_red;
                break;
            case PROCESSING:
                drawableId = R.drawable.background_yellow;
                break;
        }


        backgroundLayout.setBackground(
                convertView
                        .getResources()
                        .getDrawable(drawableId));

        return convertView;
    }
}
