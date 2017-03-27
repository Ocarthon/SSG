package de.ocarthon.ssg.curaengine;

import com.google.protobuf.Message;
import de.ocarthon.libArcus.ArcusSocket;
import de.ocarthon.libArcus.Error;
import de.ocarthon.libArcus.SocketListener;
import de.ocarthon.libArcus.SocketState;
import de.ocarthon.ssg.curaengine.config.CuraConfiguration;
import de.ocarthon.ssg.curaengine.config.Printer;
import de.ocarthon.ssg.math.Object3D;
import de.ocarthon.ssg.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CuraEngine {
    /**
     * List of filenames that are required to get CuraEngine running
     */
    private static final String[] curaEngineFiles = new String[]{"CuraEngine.exe",
            "libgcc_s_seh-1.dll", "libstdc++-6.dll", "libwinpthread-1.dll",
            "fdmextruder.def.json", "fdmprinter.def.json"};

    /**
     * Directory where the CuraEngine is extracted to
     */
    private static File curaEngineDir;

    /**
     * CuraEngine process if one exists
     */
    private Process curaEngineProc;

    /**
     * ArcusSocket to communicate with the CuraEngine
     */
    private ArcusSocket socket;

    /**
     * Registered listener
     */
    private List<CuraEngineListener> listeners = new ArrayList<>(2);

    /**
     * port that will be used to communicate
     */
    private int port;

    /**
     * Current slice configuration
      */
    private Cura.Slice sliceConfiguration;

    /**
     * Current slice progress
     */
    private SliceProgress progress;

    // Extract the Engine
    static {
        curaEngineDir = new File(System.getProperty("user.home"), "/.ssg/CuraEngine");

        if (!curaEngineDir.exists() && !curaEngineDir.mkdirs()) {
            throw new RuntimeException("Error creating CuraEngine directory");
        }

        try {
            for (String fName : curaEngineFiles) {
                FileUtil.saveResourceToFile(CuraEngine.class.getResourceAsStream("binary/" + fName), new File(curaEngineDir, fName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CuraEngine(int port) {
        this.port = port;
        setupArcusSocket();
    }

    public void slice(Printer printer, Object3D... objects) throws IOException {
        if (isProcessRunning()) {
            throw new IllegalStateException("Process already running");
        }

        Cura.Slice.Builder builder = Cura.Slice.newBuilder();
        CuraConfiguration.addObjects(builder, objects);
        CuraConfiguration.setConfiguration(builder, printer);
        sliceConfiguration = builder.build();

        progress = new SliceProgress();

        this.socket.addListener(new CuraEngineListenerInternal());

        for (CuraEngineListener l : listeners) {
            l.onSliceStart(progress);
        }

        start();
    }

    private void start() throws IOException {
        this.socket.listen(port);

        ProcessBuilder pb = new ProcessBuilder(curaEngineDir.getAbsolutePath() + "/CuraEngine",
                "connect", "127.0.0.1:" + socket.getPort(), "-v", "-j",
                curaEngineDir.getAbsolutePath() + "/fdmprinter.def.json");

        File log = new File(curaEngineDir, "engine.log");
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
        pb.redirectError(ProcessBuilder.Redirect.appendTo(log));

        this.curaEngineProc = pb.start();
    }

    public boolean isProcessRunning() {
        return curaEngineProc != null && curaEngineProc.isAlive();
    }

    public void addListener(CuraEngineListener listener) {
        this.listeners.add(listener);
    }

    public void reset() {
        socket.reset();
    }

    private void setupArcusSocket() {
        this.socket = new ArcusSocket();

        socket.registerMessageType(Cura.Slice.getDefaultInstance());
        socket.registerMessageType(Cura.Layer.getDefaultInstance());
        socket.registerMessageType(Cura.LayerOptimized.getDefaultInstance());
        socket.registerMessageType(Cura.Progress.getDefaultInstance());
        socket.registerMessageType(Cura.GCodeLayer.getDefaultInstance());
        socket.registerMessageType(Cura.PrintTimeMaterialEstimates.getDefaultInstance());
        socket.registerMessageType(Cura.SettingList.getDefaultInstance());
        socket.registerMessageType(Cura.GCodePrefix.getDefaultInstance());
        socket.registerMessageType(Cura.SlicingFinished.getDefaultInstance());
        socket.registerMessageType(Cura.SettingExtruder.getDefaultInstance());
    }

    private class CuraEngineListenerInternal implements SocketListener {

        @Override
        public void stateChanged(ArcusSocket socket, SocketState newState) {
            if (newState == SocketState.Connected) {
                socket.sendMessage(sliceConfiguration);
            }
        }

        @Override
        public void messageReceived(ArcusSocket socket) {
            Message m = socket.takeNextMessage();
            try {
                if (m instanceof Cura.GCodeLayer) {
                    progress.layers.add((Cura.GCodeLayer) m);
                } else if (m instanceof Cura.Progress) {
                    progress.progress = ((Cura.Progress) m).getAmount();

                    for (CuraEngineListener l : listeners) {
                        l.onProgressUpdate(progress);
                    }
                } else if (m instanceof Cura.PrintTimeMaterialEstimates) {
                    Cura.PrintTimeMaterialEstimates ptme = ((Cura.PrintTimeMaterialEstimates) m);
                    progress.timeEstimate = ptme.getTime();
                    progress.materialEstimates = ptme.getMaterialEstimatesList();
                } else if (m instanceof Cura.SlicingFinished) {
                    for (CuraEngineListener l : listeners) {
                        l.onSliceFinished(progress);
                    }
                }
            } catch (Exception e) {
                System.err.println("CuraEngine error: " + e.getMessage());
            }
        }

        @Override
        public void error(ArcusSocket socket, Error error) {
            for (CuraEngineListener l : listeners) {
                l.onError(progress, error);
            }
        }
    }
}
