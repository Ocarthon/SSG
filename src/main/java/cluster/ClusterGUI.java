package cluster;

import de.ocarthon.ssg.math.Vector;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClusterGUI {

    public static void main(String[] args) {
        List<Vector> vectors = new ArrayList<>();

        for (int i = 0; i < 399; i++) {
            for (int j = 0; j < 399; j++) {
                vectors.add(new Vector(i, j, 0));
            }
        }

        Random rand = new Random();
        List<Cluster> cluster = new ArrayList<>();
        List<Vector> means = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            cluster.add(new Cluster());
            means.add(new Vector(rand.nextInt(400), rand.nextInt(400), 0));
        }

        cluster = Cluster.clusteringChebychev(cluster, means, vectors);


        JFrame frame = new JFrame("Cluster");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        List<Cluster> finalCluster = cluster;
        frame.add(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                for (Cluster aCluster : finalCluster) {
                    g.setColor(Color.getHSBColor(rand.nextFloat(), 1f, 1f));
                    for (int j = 0; j < aCluster.vectors.size(); j++) {
                        Vector v = aCluster.vectors.get(j);
                        g.fillRect((int) v.x, (int) v.y, 1, 1);
                    }
                }
            }
        });

        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}
