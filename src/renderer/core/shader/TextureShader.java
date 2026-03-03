package renderer.core.shader;

import java.awt.Color;

import renderer.algebra.MathUtils;
import renderer.controller.ImageWrapper;
import renderer.controller.Renderer;
import renderer.core.mesh.Texture;

/**
 * Simple shader that just copy the interpolated color to the screen,
 * taking the depth of the fragment into account.
 *
 * @author cdehais
 */
public class TextureShader extends Shader {

    /**
     * The start index of the texture attribute.
     */
    private static final int START_TEXTURE_ATTRIBUTE = 7;

    /**
     * The number of attribute about the texture.
     */
    private static final int NUMBER_TEXTURE_ATTRIBUTE = 2;

    /** The depth buffer. */
    private DepthBuffer depth;
    /** The texture to apply. */
    private Texture texture;
    /**
     * If we have to combine the texture color and
     * the original color of the fragment.
     */
    private boolean combineWithBaseColor;

    /**
     * Creates a PainterShader.
     */
    public TextureShader() {
        super();
        texture = null;
    }

    /**
     * Set the texture to use for shading.
     *
     * @param path the path to the texture image
     * @return whether the operation is a success
     */
    public boolean setTexture(String path) {
        try {
            texture = new Texture(path);
            return true;
        } catch (Exception e) {
            System.out.println("Could not load texture " + path);
            e.printStackTrace();
            texture = null;
            return false;
        }
    }

    /**
     * Set whether the texture should be combined with the base color.
     *
     * @param combineWithBaseColor true if the texture should be combined
     *                             with the base color
     */
    public void setCombineWithBaseColor(boolean combineWithBaseColor) {
        this.combineWithBaseColor = combineWithBaseColor;
    }

    /**
     * Shade the fragment, taking the depth of the fragment into account.
     *
     * @param fragment the fragment to shade
     */
    @Override
    public void shade(Fragment fragment) {
        if (!depth.testFragment(fragment)) {
            return;
        }
        // The Fragment may not have texture coordinates
        try {
            // Récupérer les coordonnées de texture (u, v) stockées aux indices 7 et 8
            double[] uv = fragment.getAttribute(START_TEXTURE_ATTRIBUTE, NUMBER_TEXTURE_ATTRIBUTE);
            double u = uv[0];
            double v = uv[1];

            // Échantillonner la texture aux coordonnées (u, v)
            Color texColor = texture.sample(u, v);

            if (combineWithBaseColor) {
                // Combiner la couleur de la texture avec la couleur de base du fragment
                double r = Fragment.colorToFloat(texColor.getRed())   * fragment.getAttribute(Fragment.COLOR_R);
                double g = Fragment.colorToFloat(texColor.getGreen()) * fragment.getAttribute(Fragment.COLOR_G);
                double b = Fragment.colorToFloat(texColor.getBlue())  * fragment.getAttribute(Fragment.COLOR_B);

                r = MathUtils.clamp(r, 0.0, 1.0);
                g = MathUtils.clamp(g, 0.0, 1.0);
                b = MathUtils.clamp(b, 0.0, 1.0);
                screen.setPixel(fragment.getX(), fragment.getY(),
                        new Color((int)(r * 255), (int)(g * 255), (int)(b * 255)));
            } else {
                // Utiliser uniquement la couleur de la texture
                screen.setPixel(fragment.getX(), fragment.getY(), texColor);
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            // Pas de coordonnées de texture : utiliser la couleur interpolée
            screen.setPixel(fragment.getX(), fragment.getY(), fragment.getColor());
        }
        depth.writeFragment(fragment);
    }
    /**
     * Reset the shader.
     */
    @Override
    public void reset() {
        depth.clear();
    }


    /**
     * Gets whether the color has to be combined with the base color.
     * @return whether the color has to be combined with the base color
     */
    public boolean getCombineWithBaseColor() {
        return combineWithBaseColor;
    }

    @Override
    public void init(final Renderer renderer, final ImageWrapper screen) {
        super.init(renderer, screen);
        if (depth == null) {
            depth = new DepthBuffer(screen.getWidth(), screen.getHeight());
        } else {
            depth.resize(screen.getWidth(), screen.getHeight());
        }
    }
}
