package de.ocarthon.ssg.math;

public class VectorPair {
    public Vector v1;
    public Vector v2;
    public Facet f;

    public VectorPair(Vector v1, Vector v2) {
        this.v1 = v1;
        this.v2 = v2;

        order();
    }

    public void order() {
        if (v1.length2() > v2.length2()) {
            Vector vt = v1;
            this.v1 = this.v2;
            this.v2 = vt;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VectorPair that = (VectorPair) o;

        if (!v1.equals(that.v1)) return false;
        return v2.equals(that.v2);
    }

    @Override
    public int hashCode() {
        int result = v1 != null ? v1.hashCode() : 0;
        result = 31 * result + (v2 != null ? v2.hashCode() : 0);
        return result;
    }
}
