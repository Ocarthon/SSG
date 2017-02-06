package de.ocarthon.ssg.curaengine;

import de.ocarthon.libArcus.Error;

public interface CuraEngineListener {

    default void onSliceStart(SliceProgress p) {
    }

    default void onProgressUpdate(SliceProgress p) {
    }

    default void onError(SliceProgress p, Error e) {
    }

    void onSliceFinished(SliceProgress p);
}
