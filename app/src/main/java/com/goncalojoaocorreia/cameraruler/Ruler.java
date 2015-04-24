package com.goncalojoaocorreia.cameraruler;

import android.graphics.Point;

import java.util.List;

/**
 * Created by Gonçalo on 13/02/2015.
 */
public class Ruler {

    private Ruler(){}

    public static double compute(List<Point> points, double scale, int inputUnitIndex, int outputUnitIndex){
        if(points.size() < 4) return -1;

        //Get reference points
        Point ref1 = points.get(0);
        Point ref2 = points.get(1);
        //Get the measurement points
        Point m1 = points.get(2);
        Point m2 = points.get(3);

        double reference = getDistance(ref1, ref2);
        double measurement = getDistance(m1, m2);

        measurement = (measurement * scale) / reference; //Get the actual distance
        //Convert to the right unit
        measurement = convertUnits(inputUnitIndex, reference, outputUnitIndex, measurement);

        return measurement;
    }

    /**
     * Get the distance between 2 points
     * @param p1 First point
     * @param p2 Second point
     * @return Distance between the 2 points
     */
    private static double getDistance(Point p1, Point p2){
        double x = Math.pow(p2.x - p1.x, 2);
        double y = Math.pow(p2.y - p1.y, 2);
        return Math.sqrt(x+y);
    }

    private static double convertUnits(int refUnit, double reference, int meaUnit, double measurement){
        if(refUnit == meaUnit)
            return measurement;

        measurement = toMeters(measurement, refUnit);
        switch (meaUnit){
            case 0:
                return measurement;
            case 1:
                return Utils.metersToCentimeters(measurement);
            case 2:
                return Utils.metersToMillimeters(measurement);
            case 3:
                return Utils.metersToInch(measurement);
            case 4:
                return Utils.metersToFeet(measurement);
            case 5:
                return Utils.metersToYards(measurement);
            default:
                return -1;
        }
    }

    private static double toMeters(double measurement, int refUnit){
        switch (refUnit){
            case 0:
                return measurement;
            case 1:
                return Utils.centimetersToMeters(measurement);
            case 2:
                return Utils.millimetersToMeters(measurement);
            case 3:
                return Utils.inchesToMeters(measurement);
            case 4:
                return Utils.yardsToMeters(measurement);
            default:
                return -1;
        }
    }
}
