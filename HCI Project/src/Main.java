import com.musicg.graphic.GraphicRender;
import com.musicg.wave.Wave;
import com.musicg.wave.extension.Spectrogram;

import java.awt.*;
import java.awt.image.*;
import java.io.File;

import javax.imageio.ImageIO;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		String name = "1";
		String outFolder = "out/";
		String filename = name+".wav";
		String outputName = name+".jpg";
		

		// create a wave object
		Wave wave = new Wave(filename);
		Spectrogram spectrogram = new Spectrogram(wave);

		// change the spectrogram representation
		int fftSampleSize = 512;
		int overlapFactor = 2;
		spectrogram = new Spectrogram(wave, fftSampleSize, overlapFactor);

//		double[][] spec1Data = spectrogram.getAbsoluteSpectrogramData();
		double[][] spec1Data = spectrogram.getNormalizedSpectrogramData();
		GraphicRender render = new GraphicRender();
		render.renderSpectrogramData(spec1Data, outFolder+outputName);

		// Graphic render
		// GraphicRender render = new GraphicRender();
		// render.setHorizontalMarker(1);
		// render.setVerticalMarker(1);
		// render.renderSpectrogram(spec1Data, outFolder + "/spectrogram.jpg");

		// render.renderSpectrogram(spectrogram, outFolder +
		// "/spectrogram2.jpg");

		GrayScale(outputName, outFolder);
		
	}

	private static void GrayScale(String Filename, String outFolder) {

		BufferedImage image;
		int width;
		int height;
		try {
			File input = new File(Filename);
			image = ImageIO.read(input);
			width = image.getWidth();
			height = image.getHeight();
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					Color c = new Color(image.getRGB(j, i));
					int red = (int) (c.getRed() * 0.299);
					int green = (int) (c.getGreen() * 0.587);
					int blue = (int) (c.getBlue() * 0.114);
					Color newColor = new Color(red + green + blue, red + green
							+ blue, red + green + blue);
					image.setRGB(j, i, newColor.getRGB());
				}
			}
			File ouptut = new File(outFolder+"grayscale"+Filename);
			ImageIO.write(image, "jpg", ouptut);
		} catch (Exception e) {
		}
	}

}
