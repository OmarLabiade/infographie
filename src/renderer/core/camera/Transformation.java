package renderer.core.camera;

import renderer.algebra.Matrix;
import renderer.algebra.SizeMismatchException;
import renderer.algebra.Vector;


/**
 * The Transformation class represents a transformation in 3D space.
 * author: cdehais
 */
public class Transformation {

    /**
     * The world to camera matrix.
     */
    private Matrix worldToCamera;
    /**
     * The 3x4 projection matrix.
     */
    private Matrix projection;
    /**
     * The 3x3 calibration matrix.
     */
    private Matrix calibration;

    /**
     * Creates a new Transformation object.
     */
    public Transformation() {
        final int w2cDim = 4;
        worldToCamera = Matrix.createIdentity("W2C", w2cDim);
        final int projRows = 3;
        final int projCols = 4;
        projection = new Matrix("P", projRows, projCols);
        final int calibDim = 3;
        calibration = Matrix.createIdentity("K", calibDim);
    }

    /**
     * Sets the lookAt transformation.
     * @param eye a 3D vector representing the eye position
     * @param lookAtPoint a 3D vector representing the point to look at
     * @param up a 3D vector representing the up direction
     */
    public void setLookAt(final Vector eye, final Vector lookAtPoint, final Vector up) {
        try {
            // compute rotation
            Vector z_cam = lookAtPoint.subtract(eye).normalize();
            Vector x_cam = up.cross(z_cam).normalize();
            Vector y_cam = z_cam.cross(x_cam).normalize();
            Matrix R = Matrix.createIdentity("R", 3);

            R.setRow(0, x_cam);
            R.setRow(1, y_cam);
            R.setRow(2, z_cam);

            // compute translation
            Matrix R_opp = R.scale(-1);
            Vector T = R_opp.multiply(eye);

            //  world to camera matrix
            Vector W2C_row1 = new Vector(R.getRow(0).get(0), R.getRow(0).get(1), R.getRow(0).get(2), T.get(0));
            Vector W2C_row2 = new Vector(R.getRow(1).get(0), R.getRow(1).get(1), R.getRow(1).get(2), T.get(1));
            Vector W2C_row3 = new Vector(R.getRow(2).get(0), R.getRow(2).get(1), R.getRow(2).get(2), T.get(2));
            Vector W2C_row4 = new Vector(0, 0, 0, 1);

            this.worldToCamera.setRow(0, W2C_row1);
            this.worldToCamera.setRow(1, W2C_row2);
            this.worldToCamera.setRow(2, W2C_row3);
            this.worldToCamera.setRow(3, W2C_row4);

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Modelview matrix:\n" + worldToCamera);
    }

    /**
     * Sets the projection matrix.
     */
    public void setProjection() {


        this.projection.setRow(0, new Vector(1, 0, 0, 0));
        this.projection.setRow(1, new Vector(0, 1, 0, 0));
        this.projection.setRow(2, new Vector(0, 0, 1, 0));





        System.out.println("Projection matrix:\n" + projection);
    }

    /**
     * Sets the calibration matrix.
     * @param focal the focal length
     * @param width the width of the image
     * @param height the height of the image
     */
    public void setCalibration(double focal, double width, double height) {

        // TODO
        this.calibration.setRow(0, new Vector(focal, 0, width / 2));
        this.calibration.setRow(1, new Vector(0, focal, height / 2));
        this.calibration.setRow(2, new Vector(0, 0, 1));






        System.out.println("Calibration matrix:\n" + calibration);
    }

    /**
     * Projects the given 3 dimensional point onto the screen.
     * The resulting Vector as its (x,y) coordinates in pixel, and its z coordinate
     * is the depth of the point in the camera coordinate system.
     * @param p a 3d vector representing a point
     * @return the projected point as a 3d vector, with (x,y) the pixel
     * coordinates and z the depth
     * @throws SizeMismatchException if the size of the input vector is not 3
     */
    public Vector projectPoint(Vector p) throws SizeMismatchException {
        // TODO
        Vector ps = new Vector(3);

        Vector phomogene = new Vector(p.get(0), p.get(1), p.get(2), 1);

        ps = calibration.multiply(projection.multiply(worldToCamera.multiply(phomogene)));

        ps.set(0, ps.get(0) / ps.get(2));
        ps.set(1, ps.get(1) / ps.get(2));
        ps.set(2, ps.get(2));


        return ps;
    }

    /**
     * Transform a vector from world to camera coordinates.
     * @param v the vector to transform
     * @return the transformed vector
     * @throws SizeMismatchException if the size of the input vector is not 3
     */
    public Vector transformVector(final Vector v) {
        // Doing nothing special here because there is no scaling
        final Matrix m = worldToCamera.getSubMatrix(0, 0, 3, 3);
        return m.multiply(v);
    }

}
