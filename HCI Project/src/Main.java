import com.musicg.graphic.GraphicRender;
import com.musicg.wave.Wave;
import com.musicg.wave.extension.Spectrogram;

import java.awt.*;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.imageio.ImageIO;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/*
		 * Variables
		 */
		String name = "Hello";
		String scaleHeight = "20";
		String baseHeight = "5";
		
		String outFolder = "out/";
		String filename = name+".wav";
		String outputName = name+".jpg";
		

		System.out.println("Converting "+filename+" to a printable STL file");
		
		/*
		 * Creating the Spectrogram
		 */
		Wave wave = new Wave(filename);
		Spectrogram spectrogram = new Spectrogram(wave);

		int fftSampleSize = 512;
		int overlapFactor = 2;
		spectrogram = new Spectrogram(wave, fftSampleSize, overlapFactor);
		
		double[][] spec1Data = spectrogram.getNormalizedSpectrogramData();
		GraphicRender render = new GraphicRender();
		render.renderSpectrogramData(spec1Data, outFolder+outputName);
		System.out.println("Spectrogram Created");
		
		
		/*
		 * Image Processing
		 * Currently only converting the image to grey scale, need to figure out a process to make a printable image
		 */
		GrayScale(outputName, outFolder);
		System.out.println("Image Processed");
		
		
		/*
		 * Converting Spectrogram to STL file.
		 */
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "../libs/heightmap2stl.jar",""+outputName,scaleHeight,baseHeight);
		pb.directory(new File("./out"));
		try {
			System.out.println("Starting Conversion Process");
			String line;
			Process p = pb.start();
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			int status = p.waitFor();
			System.out.println(status);
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finished Process");
	}

	
/*
 * GrayScale function, converts an image to grayscale
 */
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
