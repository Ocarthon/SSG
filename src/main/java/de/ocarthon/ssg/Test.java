package de.ocarthon.ssg;

import de.ocarthon.ssg.formats.ObjectReader;
import de.ocarthon.ssg.generator.FacetClustering;
import de.ocarthon.ssg.math.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@SuppressWarnings("Duplicates")
public class Test {
    private static Object3D object;
    private static float scale = 10;
    private static Vector zAxis = new Vector(0, 0, 1);
    private static List<FacetGroup> facetGroups = new ArrayList<>(2);
    private static ArrayList<Vector> points = new ArrayList<>();
    private static List<Facet> supportFacets = new ArrayList<>();
    private static List<Facet> splitFacets = new ArrayList<>();

    private static JSlider angleSlider;

    private static boolean showRegions = true;
    private static boolean showHull = false;
    private static boolean showCorners = false;
    private static boolean showSupportFacets = false;
    private static boolean showSplitFacets = false;

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);

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

                if (showRegions ||showHull || showCorners) {
                    for (FacetGroup fg : facetGroups) {
                        if (showRegions) {
                            g2.setColor(fg.color);
                            for (Facet f : fg.getFacets()) {
                                drawFacet(g2, rotation.transform(f));
                            }

                        }

                        if (showHull) {
                            for (Vector v : fg.corners) {
                                drawCircle(g2, rotation.transform(v), 10, Color.RED);
                            }
                            Vector b = fg.center;

                            drawCircle(g2, rotation.transform(b), 10, Color.ORANGE);

                        }

                        if (showCorners) {
                            for (Vector v : points) {
                                v = rotation.transform(v);
                                drawCircle(g2, v, 10, Color.YELLOW);
                            }
                        }
                    }
                }

                if (showSupportFacets) {
                    g2.setColor(Color.GREEN);
                    for (Facet f : supportFacets) {
                        drawFacet(g2, rotation.transform(f));
                    }
                }

                if (showSplitFacets) {
                    g2.setColor(Color.PINK);
                    for (Facet f : splitFacets) {
                        drawFacet(g2, rotation.transform(f));
                    }
                }
            }
        };

        renderPanel.getInputMap().put(KeyStroke.getKeyStroke('1'), "1");
        renderPanel.getActionMap().put("1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("1");
            }
        });

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
        g2.fillOval((int) (v.y - radius), -(int) (v.z + radius), (int) (radius * 2), (int) (radius * 2));
    }

    private static void drawFacet(Graphics2D g2, Facet f) {
        g2.drawLine((int) f.p1.y, -(int) f.p1.z, (int) f.p2.y, -(int) f.p2.z);
        g2.drawLine((int) f.p1.y, -(int) f.p1.z, (int) f.p3.y, -(int) f.p3.z);
        g2.drawLine((int) f.p2.y, -(int) f.p2.z, (int) f.p3.y, -(int) f.p3.z);
    }

    public static void refreshFacets() {
        Random random = new Random(42);

        facetGroups.clear();

        object.centerObject();
        object.facets.stream().filter(f -> Vector.angle(f.n, zAxis) >= Math.toRadians(90 + angleSlider.getValue()) && !MathUtil.equals(f.findLowestZ(), 0, 1)).forEach(f -> {
            double b = f.findLowestZ();

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

        facetGroups = FacetGroup.unifyFacetGroups(facetGroups);

        double minArea = 1;

        for (int i = facetGroups.size() - 1; i >= 0; i--) {
            if (facetGroups.get(i).getArea() < minArea) {
                facetGroups.remove(i);
            }
        }

        facetGroups.forEach(facetGroup -> System.out.println(facetGroup.getArea()));

        double radius2 = 5 * 5;

        supportFacets.clear();

        List<Facet> flatFacets = new ArrayList<>();

        for (FacetGroup fg : facetGroups) {
            fg.removeDoubles();
            fg.calculateHull();
            System.out.println(facetGroups.get(0).corners);

            double lowZ = Double.MAX_VALUE;
            for (Facet f : fg.getFacets()) {
                double lowF = f.findLowestZ();
                if (lowF < lowZ) {
                    lowZ = lowF;
                }

                flatFacets.add(new Facet(f));
            }

            Vector m = fg.center;

            // Add sub corners
            Vector.sortByPolarAngle(fg.corners, m);

            for (int i = 0; i < fg.corners.size(); i++) {
                Vector a = fg.corners.get(i);
                Vector b = fg.corners.get((i + 1) % fg.corners.size());

                double angle = Vector.angle(Vector.sub(a, m), Vector.sub(b, m));
                double maxCornerAngle = Math.toRadians(33);
                if (angle > maxCornerAngle) {
                    int parts = (int) Math.ceil(angle / maxCornerAngle);
                    Vector ab = Vector.sub(b, a).mult(1d / parts);
                    for (int j = 1; j <= parts - 1; j++) {
                        points.add(Vector.add(a, ab.copy().mult(j)));
                    }
                }
            }


            List<Facet> facets = object.facets;
            for (Facet f : facets) {
                if (f.findLowestZ() < lowZ && Vector.angle(Vector.Z, f.n) <= Math.PI / 2 && MathUtil.dst2PointTriangle(m, f.p1, f.p2, f.p3) <= radius2) {
                    supportFacets.add(f);
                }
            }
        }

        splitFacets.clear();

        for (Facet f : flatFacets) {
            f.p1.z = 0;
            f.p2.z = 0;
            f.p3.z = 0;

            FacetClustering.splitFacet(f, splitFacets);
            //splitFacets.add(f);
        }

        System.out.println("Split facets: " + splitFacets.size());

        facetGroups = FacetClustering.clusterFacets(splitFacets, 2);
        for (FacetGroup fg : facetGroups) {
            fg.color = Color.getHSBColor(random.nextFloat(), 1f, 1f);
            System.out.println(fg.getFacets().size());
        }
    }
}