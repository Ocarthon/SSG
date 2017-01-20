package de.ocarthon.ssg.math;

public class LGS {

    public static Vector solveLGS(double a11, double a12, double b1, double a21, double a22, double b2) {
        try {
            double y = (a21*b1-a11*b2)/(a12*a21-a11*a22);
            double x = (b1-y*a12)/(a11);

            return new Vector(x, y, 0);
        } catch (Exception e) {
            return null;
        }
    }
}
