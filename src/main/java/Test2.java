import de.ocarthon.ssg.math.Centroid;
import de.ocarthon.ssg.math.Vector;

import java.util.ArrayList;
import java.util.List;

public class Test2 {

    public static void main(String[] args) {
        List<Vector> vectorList = new ArrayList<>();
        vectorList.add(new Vector(-20, -10, 0));
        vectorList.add(new Vector(-17.5, -8.75, 0));
        vectorList.add(new Vector(-17.5, -10, 0));

        System.out.println(Centroid.chebychevCenter(vectorList));
    }
}
