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
import java.util.Arrays;

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
		double[][] spec2Data = spectrogram.getNormalizedSpectrogramData();
		

		int width = spec1Data.length;
		int height = spec1Data[0].length;

		/*
		 * Process the data here.
		 */

		//Thresholding?
		for(int i=0; i<spec1Data.length; i++){
			for(int j=0; j<spec1Data[i].length; j++){
				double[] medianFilter = new double[3]; 
				if(spec1Data.length > 3){
//					if(j == 0){
//						medianFilter[0] = spec1Data[i][j];
//						medianFilter[1] = spec1Data[i][j+1];
//						medianFilter[2] = spec1Data[i][j+2];
//					}else if(j == spec1Data[i].length-1){
//						medianFilter[0] = spec1Data[i][j-2];
//						medianFilter[1] = spec1Data[i][j-1];
//						medianFilter[2] = spec1Data[i][j];
//					}else{
//						medianFilter[0] = spec1Data[i][j-1];
//						medianFilter[1] = spec1Data[i][j];
//						medianFilter[2] = spec1Data[i][j+1];
//					}
//					Arrays.sort(medianFilter);
//					spec2Data[i][j] = medianFilter[1];
					if(spec1Data[i][j] < 0.6){
						spec2Data[i][j] = 0;
					}
				}
			}			
		}

	
		spec1Data = spec2Data;

		//3x3 Mean Filter
		for(int i=1; i<spec1Data.length; i=i+3){
			for(int j=1; j<spec1Data[i].length; j=j+3){
				double mean = 0;
				if(spec1Data.length > 3){
					if(i == 0 || j == 0 || i == spec1Data.length-1 || j == spec1Data[i].length-1){
						//ignore it
					}else{
						mean += spec1Data[i-1][j];
						mean += spec1Data[i][j];
						mean += spec1Data[i+1][j];

						mean += spec1Data[i-1][j+1];
						mean += spec1Data[i][j+1];
						mean += spec1Data[i+1][j+1];

						mean += spec1Data[i-1][j-1];
						mean += spec1Data[i][j-1];
						mean += spec1Data[i+1][j-1];

						mean = mean/9;
						
						int colorValue = (int)(mean*255);
						
						spec2Data[i-1][j] = colorValue;
						spec2Data[i][j] = colorValue;
						spec2Data[i+1][j] = colorValue;
						spec2Data[i-1][j+1] = colorValue;
						spec2Data[i][j+1] = colorValue;
						spec2Data[i+1][j+1] = colorValue;
						spec2Data[i-1][j-1] = colorValue;
						spec2Data[i][j-1] = colorValue;
						spec2Data[i+1][j-1] = colorValue;
						//						if(spec1Data[i][j] < 0.6){
						//							spec1Data[i][j] = 0;
						//						}
					}
				}
			}			
		}

//		//Invert
//		for(int i=0; i<spec2Data.length; i++){
//			for(int j=0; j<spec2Data[i].length; j++){
//				//spec2Data[i][j] = 1-spec2Data[i][j];
//			}			
//		}


		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				Color newColor = new Color((int)(spec2Data[j][i]),(int)(spec2Data[j][i]),(int)(spec2Data[j][i]));
				image.setRGB(j, height-i-1, newColor.getRGB());
			}
		}
		File output = new File(outFolder+"grayscale"+outputName);
		try {
			ImageIO.write(image, "png", output);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		
//		GraphicRender render = new GraphicRender();
//		render.renderSpectrogramData(spec1Data, outFolder+outputName);
		System.out.println("Spectrogram Created");


		/*
		 * Image Processing
		 * Currently only converting the image to grey scale, need to figure out a process to make a printable image
		 */
		//GrayScale(outputName, outFolder);

//
//		float brightenFactor = 1.2f;
//
//
//		File fileInput = new File(outFolder+outputName);
//		BufferedImage image = null;
//		try {
//			image = ImageIO.read(fileInput);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		RescaleOp op = new RescaleOp(1.2f, 10, null);
//		image = op.filter(image, image);
//
//		File output = new File(outFolder+"grayscale"+outputName);
//		try {
//			ImageIO.write(image, "jpg", output);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		System.out.println("Image Processed");


		/*
		 * Converting Spectrogram to STL file.
		 */
				ProcessBuilder pb = new ProcessBuilder("java", "-jar", "../libs/heightmap2stl.jar","grayscale"+outputName,scaleHeight,baseHeight);
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
			File output = new File(outFolder+"grayscale"+Filename);
			ImageIO.write(image, "jpg", output);
		} catch (Exception e) {
		}
	}

}




/*else if(i == spec1Data.length && j == spec1Data[i].length){

}


if(j == 0){
	meanFilter[0] = spec1Data[i][j];
	meanFilter[1] = spec1Data[i][j+1];
	meanFilter[2] = spec1Data[i][j+2];
}else if(j == spec1Data[i].length-1){
	meanFilter[0] = spec1Data[i][j-2];
	meanFilter[1] = spec1Data[i][j-1];
	meanFilter[2] = spec1Data[i][j];
}*/