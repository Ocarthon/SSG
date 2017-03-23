package cluster;

import de.ocarthon.ssg.math.Centroid;
import de.ocarthon.ssg.math.Vector;

import java.util.ArrayList;
import java.util.List;


public class Cluster {
    private static final double TOL = 1e-2;
    private static final int MAX_DEPTH = 100;

    public ArrayList<Vector> vectors;

    public Cluster(){
        vectors = new ArrayList<>();
    }

    public static List<Cluster> clusteringChebychev(List<Cluster> C, List<Vector> means, List<Vector> V){
        //Creation of new Cluster with old Mean Vector
        List<Cluster> newC = new ArrayList<>();
        List<Cluster> oldC = new ArrayList<>();
        List<Vector> newM = new ArrayList<>();
        List<Vector> oldM = new ArrayList<>();

        int depth = 0;

        while (depth < MAX_DEPTH) {
            //System.out.println(depth);
            if (depth == 0) {
                for (int i = 0; i < C.size(); i++) {
                    oldC.add(C.get(i));
                    newC.add(new Cluster());
                    oldM.add(means.get(i));
                }
            } else {
                oldC.clear();
                oldM.clear();
                for (int i = 0; i < newC.size(); i++){
                    oldC.add(newC.get(i));
                    oldM.add(newM.get(i));
                }
            }

            for (Cluster c: newC){
                c.vectors.clear();
            }
            //every Vector searches nearest Cluster and is added to it
            for (Vector v : V) {
                double d = Double.MAX_VALUE;
                Cluster cn = null;
                for (int i = 0; i < newC.size(); i++) {
                    double dl = Vector.dst2(v, oldM.get(i));
                    if (dl < d) {
                        d = dl;
                        cn = newC.get(i);
                    }
                }
                if (cn != null) {
                    cn.vectors.add(v);
                }
            }

            newM.clear();
            for (int i = 0; i < newC.size(); i++) {
                newM.add(Centroid.chebychevCenter(newC.get(i).vectors));
            }

            boolean complete = true;
            for (int i = 0; i < oldM.size(); i++) {
                double d = Vector.dst2(oldM.get(i), newM.get(i));
                if (d > TOL) {
                    //C.get(i).vectors.clear();
                    complete = false;
                    break;
                }
            }
            depth++;
            if (complete){
                return newC;
            }
        }
        return newC;
    }
}
