package de.ocarthon.ssg.curaengine;

import java.util.LinkedList;
import java.util.List;

public class SliceProgress {
    LinkedList<Cura.GCodeLayer> layers = new LinkedList<>();
    float progress = 0F;

    float timeEstimate = 0F;
    List<Cura.MaterialEstimates> materialEstimates;

    public LinkedList<Cura.GCodeLayer> getLayers() {
        return layers;
    }

    public float getProgress() {
        return progress;
    }

    public float getTimeEstimate() {
        return timeEstimate;
    }

    public List<Cura.MaterialEstimates> getMaterialEstimates() {
        return materialEstimates;
    }
}
