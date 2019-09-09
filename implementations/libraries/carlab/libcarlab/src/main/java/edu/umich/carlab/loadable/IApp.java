package edu.umich.carlab.loadable;

import android.app.Activity;
import android.util.Pair;
import android.view.View;
import edu.umich.carlab.DataMarshal;

import java.util.List;


public interface IApp {

    /**
     * New data passed into the app. This data is meant for app internal functions. The app may
     * decide to process secondary sources based on this data locally and maybe use them for
     * visualization later (when CarLab calls {@link #initializeVisualization(Activity)}).
     * @param dObject Data object
     */
    public void newData(DataMarshal.DataObject dObject);


    /**
     * This reads from a local variable called "name". All apps must define this variable.
     * If they don't define it, the base class which implements IApp will throw an exception.
     * @return
     */
    public String getName();

    /**
     * Returns the list of sensors which this app needs. Eventually we may also add an option to
     * say if each sensor is mandatory or optional. That way, CarLab can automatically hide or show
     * the app. This might also be a good point to integrate permission models.
     * @return List of sensors -- pairs <device, sensor>
     */
    public List<Pair<String, String>> getSensors();


    /**
     * Creates a View. The View can be anything including an GLSurfaceView. See
     * https://developer.android.com/reference/android/view/View.html for more info
     * @return
     */
    public View initializeVisualization(Activity parentActivity);

    /**
     * Destroys visualization
     */
    public void destroyVisualization();

    public void shutdown();
}
