package de.ocarthon.ssg.math;

import java.awt.*;

public class Facet {
    public Vector p1;
    public Vector p2;
    public Vector p3;
    public Vector n;
    public Color color;

    public Facet(Vector p1, Vector p2, Vector p3) {
        this(p1, p2, p3, null);
    }

    public Facet(Vector p1, Vector p2, Vector p3, Vector n) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;

        if (n == null) {
            this.n = Vector.cross(p2.copy().sub(p1), p3.copy().sub(p1));
        } else {
            this.n = n;
        }
    }

    public double findLowestPoint() {
        double l = p1.z;
        if (p2.z < l) l = p2.z;
        if (p3.z < l) l = p3.z;

        return l;
    }

    public boolean contains(Vector v) {
        return p1.equals(v) || p2.equals(v) || p3.equals(v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Facet facet = (Facet) o;

        return p1 != null ? p1.equals(facet.p1) : facet.p1 == null &&
                (p2 != null ? p2.equals(facet.p2) : facet.p2 == null &&
                        (p3 != null ? p3.equals(facet.p3) : facet.p3 == null));

    }

    public boolean equals(Object o, double eps) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Facet facet = (Facet) o;

        return p1 != null ? p1.equals(facet.p1, eps) : facet.p1 == null &&
                (p2 != null ? p2.equals(facet.p2, eps) : facet.p2 == null &&
                        (p3 != null ? p3.equals(facet.p3, eps) : facet.p3 == null));

    }

    @Override
    public int hashCode() {
        int result = p1 != null ? p1.hashCode() : 0;
        result = 31 * result + (p2 != null ? p2.hashCode() : 0);
        result = 31 * result + (p3 != null ? p3.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Facet{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                ", p3=" + p3 +
                ", n=" + n +
                '}';
    }
}
