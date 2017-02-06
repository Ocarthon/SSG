import de.ocarthon.ssg.math.MathUtil;
import de.ocarthon.ssg.math.Vector;

public class Test {

    public static void main(String[] args) {
        Vector p1 = new Vector(15, -10, 11.7543);
        Vector p2 = new Vector(0.4904, 10, 3.37713);
        Vector p3 = new Vector(0.4904, -10, 3.37713);
        Vector p = new Vector(0, 0, 0);
        /*double radius = 5;
        double dist2 = MathUtil.dst2PointTriangle(p, p1, p2, p3);
        System.out.println(dist2);
        System.out.println(dist2 <= Math.pow(radius, 2));
        System.out.println(dist2 <= Math.pow(radius + 1, 2));
        */

        System.out.println(MathUtil.dst2PointLine(p, p2, p3));
    }
}
