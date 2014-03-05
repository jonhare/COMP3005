package uk.ac.soton.ecs.comp3005.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

/**
 * Very crude orthographic wireframe renderer
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class Simple3D {
	public static interface Primative {
		public void renderOrtho(Matrix transform, MBFImage image);
	}

	public static class Point3D implements Primative {
		Matrix pt;
		private Float[] colour;
		private int size;

		public Point3D(double x, double y, double z, Float[] colour, int size) {
			pt = new Matrix(3, 1);
			pt.set(0, 0, x);
			pt.set(1, 0, y);
			pt.set(2, 0, z);
			this.colour = colour;
			this.size = size;
		}

		@Override
		public void renderOrtho(Matrix transform, MBFImage image) {
			image.drawPoint(projectOrtho(transform.times(pt)), colour, size);
		}
	}

	public static class Line3D implements Primative {
		Matrix pt1;
		Matrix pt2;
		private Float[] colour;
		private int thickness;

		public Line3D(double x1, double y1, double z1, double x2, double y2, double z2, Float[] colour, int size) {
			pt1 = new Matrix(3, 1);
			pt1.set(0, 0, x1);
			pt1.set(1, 0, y1);
			pt1.set(2, 0, z1);
			pt2 = new Matrix(3, 1);
			pt2.set(0, 0, x2);
			pt2.set(1, 0, y2);
			pt2.set(2, 0, z2);
			this.colour = colour;
			this.thickness = size;
		}

		@Override
		public void renderOrtho(Matrix transform, MBFImage image) {
			final Point2dImpl p1 = projectOrtho(transform.times(pt1));
			final Point2dImpl p2 = projectOrtho(transform.times(pt2));

			image.drawLine(p1, p2, thickness, colour);
		}
	}

	public static class Scene {
		List<Primative> primatives = new ArrayList<Primative>();

		public Scene() {
		}

		public Scene(List<Primative> primatives) {
			this.primatives.addAll(primatives);
		}

		public Scene(Primative... primatives) {
			this.primatives.addAll(Arrays.asList(primatives));
		}

		public Scene addPrimative(Primative p) {
			primatives.add(p);
			return this;
		}

		public void renderOrtho(Matrix transform, MBFImage image) {
			for (final Primative p : primatives)
				p.renderOrtho(transform, image);
		}
	}

	public static Point2dImpl projectOrtho(Matrix pt) {
		final Point2dImpl po = new Point2dImpl();

		po.x = (float) pt.get(0, 0);
		po.y = (float) pt.get(1, 0);

		return po;
	}

	public static Matrix euler2Rot(final double pitch, final double yaw, final double roll)
	{
		Matrix R;
		R = new Matrix(3, 3);

		final double sina = Math.sin(pitch), sinb = Math.sin(yaw), sinc = Math
				.sin(roll);
		final double cosa = Math.cos(pitch), cosb = Math.cos(yaw), cosc = Math
				.cos(roll);
		R.set(0, 0, cosb * cosc);
		R.set(0, 1, -cosb * sinc);
		R.set(0, 2, sinb);
		R.set(1, 0, cosa * sinc + sina * sinb * cosc);
		R.set(1, 1, cosa * cosc - sina * sinb * sinc);
		R.set(1, 2, -sina * cosb);
		R.set(2, 0, R.get(0, 1) * R.get(1, 2) - R.get(0, 2) * R.get(1, 1));
		R.set(2, 1, R.get(0, 2) * R.get(1, 0) - R.get(0, 0) * R.get(1, 2));
		R.set(2, 2, R.get(0, 0) * R.get(1, 1) - R.get(0, 1) * R.get(1, 0));

		return R;
	}

	public static void main(String[] args) {
		final MBFImage img = new MBFImage(800, 800, ColourSpace.RGB);
		new Scene(
				new Point3D(400, 400, 400, RGBColour.RED, 14),
				new Line3D(0, 400, 400, 800, 400, 400, RGBColour.GREEN, 3),
				new Line3D(400, 0, 400, 400, 800, 400, RGBColour.BLUE, 3),
				new Line3D(400, 400, 0, 400, 400, 800, RGBColour.MAGENTA, 3)).renderOrtho(
				euler2Rot(Math.PI / 4, Math.PI / 4, Math.PI / 4),
				img);
		DisplayUtilities.display(img);
	}
}
