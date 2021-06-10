
package com.globalnest.detector;

/**
 * <p>
 * Encapsulates information about finder patterns in an image, including the location of the three
 * finder patterns, and their estimated module size.
 * </p>
 * 
 * 
 */
public final class FinderPatternInfo {

    private final FinderPattern bottomLeft;
    private final FinderPattern topLeft;
    private final FinderPattern topRight;

    public FinderPatternInfo(FinderPattern[] patternCenters) {
        this.bottomLeft = patternCenters[0];
        this.topLeft = patternCenters[1];
        this.topRight = patternCenters[2];
    }

    public FinderPattern getBottomLeft() {
        return bottomLeft;
    }

    public FinderPattern getTopLeft() {
        return topLeft;
    }

    public FinderPattern getTopRight() {
        return topRight;
    }

}
