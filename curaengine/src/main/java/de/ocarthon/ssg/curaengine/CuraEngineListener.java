package de.ocarthon.ssg.curaengine;

import de.ocarthon.libArcus.Error;

public interface CuraEngineListener {

    void onSliceStart(SliceProgress p);

    void onProgressUpdate(SliceProgress p);

    void onError(SliceProgress p, Error e);

    void onSliceFinished(SliceProgress p);
}
