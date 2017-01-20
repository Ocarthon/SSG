package de.ocarthon.ssg;

import de.ocarthon.ssg.formats.ObjectReader;
import de.ocarthon.ssg.math.*;
import de.ocarthon.ssg.math.Vector;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;

@SuppressWarnings("Duplicates")
public class Test {
    private static Object3D object;
    private static float scale = 10;
    private static Vector zAxis = new Vector(0, 0, 1);
    private static ArrayList<FacetGroup> facetGroups = new ArrayList<>(2);
    private static ArrayList<Vector> points = new ArrayList<>();
    private static JSlider angleSlider;

    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            System.out.println("No file specified");
            return;
        }

        File file = new File(args[0]);
        object = ObjectReader.readObject(file);
        object.centerObject();


        System.out.println(object.facets.size());

        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        JSlider zSlider = new JSlider(-180, 180, 0);
        pane.add(zSlider, BorderLayout.SOUTH);

        JSlider ySlider = new JSlider(SwingConstants.VERTICAL, -180, 180, 0);
        pane.add(ySlider, BorderLayout.EAST);

        JSlider xSlider = new JSlider(-180, 180, 0);
        pane.add(xSlider, BorderLayout.NORTH);

        angleSlider = new JSlider(SwingConstants.VERTICAL, 0, 90, 45);
        pane.add(angleSlider, BorderLayout.WEST);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.addMouseWheelListener(e -> {
            scale -= e.getWheelRotation() / 10f;
            frame.repaint();
        });


        refreshFacets();

        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.setColor(Color.GRAY);

                Matrix rotation = Matrix.rotationMatrix(Math.toRadians(xSlider.getValue()),
                        Math.toRadians(ySlider.getValue()),
                        Math.toRadians(zSlider.getValue()))
                        .multiply(Matrix.scaleMatrix(scale));

                g2.setColor(Color.BLACK);
                for (Facet f : object.facets) {
                    f = rotation.transform(f);
                    drawFacet(g2, f);
                }


                for (FacetGroup fg : facetGroups) {
                    g2.setColor(fg.color);
                    for (Facet f : fg.getFacets()) {
                        drawFacet(g2, rotation.transform(f));
                    }

                    for (Vector v : fg.corners) {
                        drawCircle(g2, rotation.transform(v), 5, Color.RED);
                    }

                    Vector b = fg.findCenterOfCorners();

                    drawCircle(g2, rotation.transform(b), 5, Color.ORANGE);

                    b.z = 0;
                    drawCircle(g2, rotation.transform(b), 5, Color.GREEN);
                }

                for (Vector v : points) {
                    v = rotation.transform(v);
                    drawCircle(g2, v, 5, Color.YELLOW);
                }

            }
        };

        pane.add(renderPanel, BorderLayout.CENTER);

        zSlider.addChangeListener(e -> renderPanel.repaint());
        ySlider.addChangeListener(e -> renderPanel.repaint());
        xSlider.addChangeListener(e -> renderPanel.repaint());
        angleSlider.addChangeListener(e -> {
            refreshFacets();
            renderPanel.repaint();
        });

        frame.setSize(800, 800);
        frame.setVisible(true);
    }

    private static void drawCircle(Graphics2D g2, Vector v, double radius, Color color) {
        g2.setColor(color);
        g2.fillOval((int)(v.y - radius), -(int) (v.z + radius), (int)(radius * 2), (int)(radius * 2));
    }

    private static void drawFacet(Graphics2D g2, Facet f) {
        g2.drawLine((int)f.p1.y, -(int)f.p1.z, (int)f.p2.y, -(int)f.p2.z);
        g2.drawLine((int)f.p1.y, -(int)f.p1.z, (int)f.p3.y, -(int)f.p3.z);
        g2.drawLine((int)f.p2.y, -(int)f.p2.z, (int)f.p3.y, -(int)f.p3.z);
    }

    public static void refreshFacets() {
        Random random = new Random(42);

        facetGroups.clear();


        object.facets.stream().filter(f -> Vector.angle(f.n, zAxis) >= Math.toRadians(90 + angleSlider.getValue()) && !(MathUtil.findLowestPoint(f) == 0)).forEach(f -> {
            boolean a = false;
            for (FacetGroup fg : facetGroups) {
                if (fg.isPart(f)) {
                    fg.add(f);
                    a = true;
                }
            }

            if (!a) {
                facetGroups.add(new FacetGroup(f, Color.getHSBColor(random.nextFloat(), 1f, 1f)));
            }
        });

        int oldLength = facetGroups.size();
        int newLength = oldLength+1;

        while (oldLength != newLength) {
            ArrayList<FacetGroup> newGroup = new ArrayList<>();
            newGroup.add(facetGroups.get(0));
            l:
            for (int i = 1; i < facetGroups.size(); i++) {
                for (Facet f : facetGroups.get(i).getFacets()) {
                    for (FacetGroup fg : newGroup) {
                        if (fg.isPart(f)) {
                            for (Facet f1 : facetGroups.get(i).getFacets()) {
                                fg.add(f1);
                            }
                            continue l;
                        }
                    }
                }

                newGroup.add(facetGroups.get(i));
            }

            oldLength = facetGroups.size();
            newLength = newGroup.size();

            facetGroups = newGroup;
        }

        for (FacetGroup fg : facetGroups) {
            fg.calculateCorners();
            Vector m = fg.findCenterOfCorners();
            fg.corners.sort(new FacetGroup.PolarVecComp(m));
            double maxCornerAngle = Math.toRadians(40);
            ArrayList<Vector> newCorners = new ArrayList<>();
            for (int i = 0; i < fg.corners.size(); i++) {
                newCorners.add(fg.corners.get(i));
                Vector a = fg.corners.get(i);
                Vector b = fg.corners.get((i+1) % fg.corners.size());

                double angle = Vector.angle(a.copy().sub(m), b.copy().sub(m));
                if (angle > maxCornerAngle) {
                    int parts = (int) Math.ceil(angle / maxCornerAngle);
                    Vector ab = b.copy().sub(a).scale(1D/(parts));
                    for (int j = 1; j <= parts-1; j++) {
                        newCorners.add(a.copy().add(ab.copy().scale(j)));
                    }
                }
            }

            fg.corners = newCorners;

            for (int i = 0; i < fg.corners.size(); i++) {
                Vector corner = fg.corners.get(i);
                Vector mc = new Vector(corner.x - m.x, corner.y - m.y, 0).norm();
                points.add(m.copy().add(mc.copy().scale(5)));
            }
        }


        System.out.println(facetGroups.size() + " " + points.size());
    }
}