import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.LineEvent.Type;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.musicg.wave.Wave;
import com.musicg.wave.extension.Spectrogram;

public class user_interface {


	final int bufSize = 16384;

	AudioInputStream audioInputStream;

	String errStr;


	double duration, seconds;

	Display display;
	Shell displayShell;
	Group shell;
	Menu menu, fileMenu, editMenu, viewMenu;
	Label label;
	Label wavName;
	Button playButton, recordButton;

	File savedWav;
	Text wavFileName;
	Canvas imgCanvas;
	Image jpgImage;


	Clip clip;
	Playback playback = new Playback();
	Capture capture = new Capture();
	Timer timer;
	final static int interval = 100;
	double timerSec = 3;
	Label timerLabel;


	public user_interface() {

		//Count down timer
		timer = new Timer(interval, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if ((Math.round(timerSec*100.0)/100.0) == 0.00){
					//Toolkit.getDefaultToolkit().beep();
					capture.stop();
					timer.stop();
					Display.getDefault().syncExec(new Runnable(){
						public void run(){
							timerLabel.setText("Finished recording");
							playButton.setEnabled(true);
							recordButton.setText("Record");
						}
					});
					OpenSaveDialog();
				} else {
					timerSec = timerSec - .1;
					Display.getDefault().syncExec(new Runnable(){
						public void run(){
							timerLabel.setText((Math.round(timerSec*100.0)/100.0)+" sec");
						}
					});
				}

			}
		});

		display = new Display();

		displayShell = new Shell(display);
		displayShell.setLayout(new GridLayout(1,true));


		shell = new Group(displayShell, SWT.NONE);
		shell.setText("Record your voice and generate a 3D printable STL file");

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		shell.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		shell.setLayout(gridLayout);
		GridData gridData = new GridData();
	

		//Record button
		recordButton = new Button(shell, SWT.PUSH);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 1;
		gridData.heightHint = 50;
		setIcon("record");
		recordButton.setText("Record");
		
		recordButton.setLayoutData(gridData);



		//Play button
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 1;
		gridData.heightHint = 50;
		playButton = new Button(shell, SWT.PUSH);
		setIcon("play");
		playButton.setText("            Play            ");
		playButton.setLayoutData(gridData);
		playButton.setEnabled(false);

		timerLabel = new Label(shell, SWT.NONE);
		timerLabel.setText(timerSec + " sec");
		gridData.heightHint = 50;
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 1;
		timerLabel.setLayoutData(gridData);		


		wavName = new Label(shell, SWT.NONE);
		wavName.setText("example.wav");
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 3;
		wavName.setLayoutData(gridData);		

		Label divider = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);

		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 6;
		divider.setLayoutData(gridData);
	
		//first group
		Group step1 = new Group(shell, SWT.NONE);
		step1.setText("Step 1");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		step1.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 1;
		step1.setLayoutData(gridData);

		Button convertJPEG = new Button(step1, SWT.PUSH);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 1;
		gridData.heightHint = 50;
		convertJPEG.setText("Convert to JPEG");
		convertJPEG.setLayoutData(gridData);

		//image
		imgCanvas = new Canvas(shell, SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.widthHint = 200;
		gridData.heightHint = 100;
		gridData.horizontalSpan = 5;
		gridData.verticalSpan = 2;
		imgCanvas.setLayoutData(gridData);
		imgCanvas.addPaintListener(new PaintListener() {
			public void paintControl(final PaintEvent event) {
				if (jpgImage != null) {
				    final int width = imgCanvas.getBounds().width;
				    final int height = imgCanvas.getBounds().height;
				    jpgImage = new Image(display, jpgImage.getImageData().scaledTo((int)(width*1),(int)(height*1)));
					event.gc.drawImage(jpgImage, 0, 0);
				}
			}
		});

		//second group
		Group step2 = new Group(shell, SWT.NONE);
		step2.setText("Step 2");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		step2.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 1;
		step2.setLayoutData(gridData);

		Button convertSTL = new Button(step2, SWT.PUSH);
		gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 1;
		gridData.heightHint = 50;
		convertSTL.setText("Convert to STL");
		convertSTL.setLayoutData(gridData);

	

		displayShell.setText("3D Audio");
		displayShell.setSize(700, 350);


		label = new Label(shell, SWT.CENTER);
		label.setBounds(shell.getClientArea());

		menu = new Menu(displayShell, SWT.BAR);
		MenuItem fileItem = new MenuItem(menu, SWT.CASCADE);
		fileItem.setText("File");
		MenuItem helpItem = new MenuItem(menu, SWT.CASCADE);
		helpItem.setText("Help");

		//Main drop down 
		Menu fileMenu = new Menu(menu);
		fileItem.setMenu(fileMenu);
//		MenuItem newItem = new MenuItem(fileMenu, SWT.NONE);
//		newItem.setText("New");
//		MenuItem openItem = new MenuItem(fileMenu, SWT.NONE);
//		openItem.setText("Open...");
//		MenuItem saveItem = new MenuItem(fileMenu, SWT.NONE);
//		saveItem.setText("Save");
//		MenuItem saveAsItem = new MenuItem(fileMenu, SWT.NONE);
//		saveAsItem.setText("Save As...");
//		new MenuItem(fileMenu, SWT.SEPARATOR);
		MenuItem exitItem = new MenuItem(fileMenu, SWT.NONE);
		exitItem.setText("Exit");

		//Help drop down 
		Menu HelpMenu = new Menu(menu);
		helpItem.setMenu(HelpMenu);
		MenuItem aboutItem = new MenuItem(HelpMenu, SWT.NONE);
		aboutItem.setText("About");

		playButton.addSelectionListener(new PlayButtonListener());
		recordButton.addSelectionListener(new RecordButtonListener());
		convertJPEG.addSelectionListener(new ConvertJPEGButtonListener());
		convertSTL.addSelectionListener(new ConvertSTLButtonListener());
		exitItem.addSelectionListener(new MenuItemListener());
		aboutItem.addSelectionListener(new MenuHelpListener());

		displayShell.setMenuBar(menu);
		displayShell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	private void setIcon(String type){
		
		if(type=="play"){
			Image playImage = new Image(display, "pictures/play.png");
		    final int width = playImage.getBounds().width;
		    final int height = playImage.getBounds().height;
		    playImage = new Image(display, playImage.getImageData().scaledTo((int)(width*0.15),(int)(height*0.15)));
		    playButton.setImage(playImage);
		}else if (type=="stop"){
			Image stopImage = new Image(display, "pictures/stop.png");
		    final int width = stopImage.getBounds().width;
		    final int height = stopImage.getBounds().height;
		    stopImage = new Image(display, stopImage.getImageData().scaledTo((int)(width*0.15),(int)(height*0.15)));
		    playButton.setImage(stopImage);
		}else if (type=="record"){
			Image recordImage = new Image(display, "pictures/record.png");
		    final int width = recordImage.getBounds().width;
		    final int height = recordImage.getBounds().height;
		    recordImage = new Image(display, recordImage.getImageData().scaledTo((int)(width*0.15),(int)(height*0.15)));
		    recordButton.setImage(recordImage);
		}

	}
	

	private void OpenSaveDialog(){
		//popup save box
		JFrame parentFrame = new JFrame();

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Specify a file to save");

		fileChooser.setFileFilter(new FileFilter()
		{
			@Override
			public boolean accept(File file)
			{
				return file.getName().toUpperCase().equals(".wav");

			}

			@Override 
			public String getDescription()
			{
				return ".wav files";
			}


		});

		int userSelection = fileChooser.showSaveDialog(parentFrame);

		if(userSelection == JFileChooser.APPROVE_OPTION){
			File wavFile = fileChooser.getSelectedFile();
			File tempWav = new File("tempWav.wav");
			String filePath = wavFile.getAbsolutePath();
			if(!filePath.endsWith(".wav")) {
				wavFile = new File(filePath + ".wav");
			}
			try {
				copyFile(tempWav, wavFile);
				savedWav = wavFile;
				final File finalWav = wavFile;
				Display.getDefault().asyncExec(new Runnable(){
					@Override
					public void run() {
						wavName.setText(finalWav.getName());
					}	
				});
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if(!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new RandomAccessFile(sourceFile,"rw").getChannel();
			destination = new RandomAccessFile(destFile,"rw").getChannel();

			long position = 0;
			long count    = source.size();

			source.transferTo(position, count, destination);
		}
		finally {
			if(source != null) {
				source.close();
			}
			if(destination != null) {
				destination.close();
			}
		}
	}

	private void playWav(){
		try {
			AudioInputStream stream;
			AudioFormat format;
			DataLine.Info info;
			File tempWav = new File("tempWav.wav");
			stream = AudioSystem.getAudioInputStream(tempWav);
			format = stream.getFormat();
			info = new DataLine.Info(Clip.class, format);
			clip = (Clip) AudioSystem.getLine(info);
			clip.addLineListener(new playbackListener());
			clip.open(stream);
			clip.start();

		}catch (Exception e) {
			//whatevers
		}
	}

	private void stopWav(){
		clip.stop();
		clip.flush();
		clip.close();
	}

	//Listeners
	class MenuItemListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			if (((MenuItem) event.widget).getText().equals("Exit")) {
				displayShell.close();
			}
		}
	}

	class MenuHelpListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event) {
			if(((MenuItem) event.widget).getText().equals("About")) {
				label.setText("A prototype to record user's voice and output 3D printable (.stl) files.");
			}
		}
	}


	class playbackListener implements LineListener{

		@Override
		public void update(LineEvent event) {
			if(event.getType() == LineEvent.Type.STOP){
				//				playButton.setEnabled(true);
				Display.getDefault().syncExec(new Runnable(){
					public void run(){
						playButton.setText("            Play            ");
						timerLabel.setText("Standby");
						setIcon("play");
						clip.close();
					}
				});
			}
		}

	}
	//Recording Listeners

	class PlayButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event){
			if(((Button)event.widget).getText().equals("            Play            ")){
				Button item = (Button) event.widget;
				item.setText("            Stop            ");
				setIcon("stop");	
				//item.setImage(display, "pictures/stop.png");
				//			item.setEnabled(false);
				//			playback.start();
				playWav();

				timerLabel.setText("Playing");
			}else if(((Button)event.widget).getText().equals("            Stop            ")){
				playButton.setText("            Play            ");
				setIcon("play");
				stopWav();
				timerLabel.setText("Standby");
				//Stop Playback
			}
		}
	}

	class RecordButtonListener extends SelectionAdapter {

		public void widgetSelected(SelectionEvent event){

			if(((Button)event.widget).getText().equals("Record")){
				Button item = (Button) event.widget;
				item.setText("Stop");
				//Start Recording
				timerSec = 3.0;
				timerLabel.setText(timerSec + " sec");
				capture.start();
				//start timer
				timer.start();

			}else if(((Button)event.widget).getText().equals("Stop")){
				Button item = (Button) event.widget;
				item.setText("Record");
				//Stop Recording
				capture.stop();
				timer.stop();
				timerLabel.setText("Finished recording");
				if(playButton != null){
					playButton.setEnabled(true);
				}
				OpenSaveDialog();
			}
		}
	}

	class ConvertJPEGButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event){
			//Convert WAV to JPEG
			File outputJPG = createJPEG(savedWav);
			jpgImage = new Image(display, outputJPG.getAbsolutePath());
			imgCanvas.redraw();
		}
	}

	class ConvertSTLButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent event){
			//Convert JPEG to STL
			String name = savedWav.getName().substring(0, savedWav.getName().lastIndexOf('.'));
			createSTL(name+".jpg");
		}
	}

	class Capture implements Runnable {

		TargetDataLine line;

		Thread thread;

		private Object errStr;

		public void start() {
			errStr = null;
			thread = new Thread(this);
			thread.setName("Capture");
			thread.start();
		}

		public void stop() {
			thread = null;
		}

		private void shutDown(String message) {
			if ((errStr = message) != null && thread != null) {
				thread = null;
				//				playB.setEnabled(true);
				//				captB.setText("Record");
				System.err.println(errStr);
			}
		}


		public void run() {

			duration = 0;

			// define the required attributes for our line,
			// and make sure a compatible line is supported.

			AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
			float rate = 44100.0f;
			int channels = 2;
			int frameSize = 4;
			int sampleSize = 16;
			boolean bigEndian = true;

			AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8)
					* channels, rate, bigEndian);

			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

			if (!AudioSystem.isLineSupported(info)) {
				shutDown("Line matching " + info + " not supported.");
				return;
			}

			// get and open the target data line for capture.

			try {
				line = (TargetDataLine) AudioSystem.getLine(info);
				line.open(format, line.getBufferSize());
			} catch (LineUnavailableException ex) {
				shutDown("Unable to open the line: " + ex);
				return;
			} catch (SecurityException ex) {
				shutDown(ex.toString());
				//JavaSound.showInfoDialog();
				return;
			} catch (Exception ex) {
				shutDown(ex.toString());
				return;
			}

			// play back the captured audio data
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int frameSizeInBytes = format.getFrameSize();
			int bufferLengthInFrames = line.getBufferSize() / 8;
			int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
			byte[] data = new byte[bufferLengthInBytes];
			int numBytesRead;

			line.start();

			while (thread != null) {
				if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
					break;
				}
				out.write(data, 0, numBytesRead);
			}

			// we reached the end of the stream.
			// stop and close the line.
			line.stop();
			line.close();
			line = null;

			// stop and close the output stream
			try {
				out.flush();
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			// load bytes into the audio input stream for playback

			byte audioBytes[] = out.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
			audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);
			File tempWav = new File("tempWav.wav");

			try {
				AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, tempWav);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / format
					.getFrameRate());
			duration = milliseconds / 1000.0;

			try {
				audioInputStream.reset();
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}

		}


	} // End class Capture

	/**
	 * Write data to the OutputChannel.
	 */
	public class Playback implements Runnable {

		SourceDataLine line;

		Thread thread;

		private Object errStr;


		private int bufSize;

		public void start() {
			errStr = null;
			thread = new Thread(this);
			thread.setName("Playback");
			thread.start();
		}

		public void stop() {
			thread = null;
		}

		private void shutDown(String message) {
			if ((errStr = message) != null) {
				System.err.println(errStr);
			}
			if (thread != null) {
				thread = null;
				//captB.setEnabled(true);
				//playB.setText("Play");
			}
		}

		public void run() {

			System.out.println("playback.start run()");
			// make sure we have something to play
			if (audioInputStream == null) {
				shutDown("No loaded audio to play back");
				return;
			}
			// reset to the beginnning of the stream
			try {
				audioInputStream.reset();
			} catch (Exception e) {
				shutDown("Unable to reset the stream\n" + e);
				return;
			}

			// get an AudioInputStream of the desired format for playback

			AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
			float rate = 44100.0f;
			int channels = 2;
			int frameSize = 4;
			int sampleSize = 16;
			boolean bigEndian = true;

			AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8)
					* channels, rate, bigEndian);

			AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(format,
					audioInputStream);


			System.out.println("playbackInputStream");

			if (playbackInputStream == null) {
				shutDown("Unable to convert stream of format " + audioInputStream + " to format " + format);
				return;
			}

			// define the required attributes for our line,
			// and make sure a compatible line is supported.

			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			if (!AudioSystem.isLineSupported(info)) {
				shutDown("Line matching " + info + " not supported.");
				return;
			}

			// get and open the source data line for playback.

			try {
				line = (SourceDataLine) AudioSystem.getLine(info);
				line.open(format, bufSize);
			} catch (LineUnavailableException ex) {
				shutDown("Unable to open the line: " + ex);
				return;
			}

			// play back the captured audio data

			int frameSizeInBytes = format.getFrameSize();
			int bufferLengthInFrames = line.getBufferSize() / 8;
			int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
			byte[] data = new byte[bufferLengthInBytes];
			int numBytesRead = 0;

			// start the source data line
			line.start();

			System.out.println("thread null test");
			while (thread != null) {
				try {
					if ((numBytesRead = playbackInputStream.read(data)) == -1) {
						break;
					}
					int numBytesRemaining = numBytesRead;
					while (numBytesRemaining > 0) {
						numBytesRemaining -= line.write(data, 0, numBytesRemaining);
					}
				} catch (Exception e) {
					shutDown("Error during playback: " + e);
					break;
				}
			}
			// we reached the end of the stream.
			// let the data play out, then
			// stop and close the line.
			if (thread != null) {
				line.drain();
			}
			System.out.println("playback.start closing");
			line.stop();
			line.close();
			line = null;
			shutDown(null);
		}
	} // End class Playback


	public static void main(String[] args) {
		user_interface example = new user_interface();
	}
	
	public File createJPEG(File file){

		String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
		String path = file.getPath();
		String outFolder = "out/";
		String outputName = name+".jpg";

		System.out.println("Converting "+name+" to a printable STL file");

		/*
		 * Creating the Spectrogram
		 */
		Wave wave = new Wave(path);
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
				if(spec1Data.length > 3){
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
					}
				}
			}			
		}

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				Color newColor = new Color((int)(spec2Data[j][i]),(int)(spec2Data[j][i]),(int)(spec2Data[j][i]));
				image.setRGB(j, height-i-1, newColor.getRGB());
			}
		}
		File output = new File(outFolder+outputName);
		try {
			ImageIO.write(image, "png", output);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return output;
	}

	public void createSTL(String outputName){
		
		String scaleHeight = "20";
		String baseHeight = "5";
		
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "../libs/heightmap2stl.jar",outputName,scaleHeight,baseHeight);
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
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}