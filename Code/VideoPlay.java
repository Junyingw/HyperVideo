import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
//import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class VideoPlay extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	private PlaySound playSound;
    private Button btnPlay;
    private Button btnPause;
    private Button btnStop;
    private int playStatus = 3; //1 for play, 2 for pause, 3 for stop
    private Thread playingThread;
    private Thread audioThread;
    static final int WIDTH = 352;
    static final int HEIGHT = 288;
    static final int CONVERSION = 1470;
    
    private static final Color FINAL_DRAWING_COLOR = Color.red;
    
    private BufferedImage currentImg;
    
    private JLabel imageLabel;
    
    private Timer timer;
    private String videoFrameDir;
    private int startFrame = 2;
    private String metaDataDir;
    
    private boolean newVideo;
    private boolean newVideoMetaData = true;
//    private MouseListener listener;
    
    List<MouseListener> listenerList = new ArrayList<>();
    
    //frameMap contains hyperlink map
    private Map<Integer, Map<Integer, HyperLink>> frameMap;
    
	public VideoPlay() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 50, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel videoPanel = new JPanel();
		imageLabel = new JLabel();
		videoPanel.add(imageLabel);
		GridBagConstraints gbc_videoPanel = new GridBagConstraints();
		gbc_videoPanel.gridwidth = 13;
		gbc_videoPanel.insets = new Insets(0, 0, 0, 5);
		gbc_videoPanel.gridx = 1;
		gbc_videoPanel.gridy = 0;
		getContentPane().add(videoPanel, gbc_videoPanel);
		
		JPanel buttonPanel = new JPanel();
		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.anchor = GridBagConstraints.EAST;
		gbc_buttonPanel.gridwidth = 3;
		gbc_buttonPanel.insets = new Insets(0, 0, 0, 5);
		gbc_buttonPanel.fill = GridBagConstraints.VERTICAL;
		gbc_buttonPanel.gridx = 14;
		gbc_buttonPanel.gridy = 0;
		getContentPane().add(buttonPanel, gbc_buttonPanel);
		GridBagLayout gbl_buttonPanel = new GridBagLayout();
		gbl_buttonPanel.columnWidths = new int[]{75, 0};
		gbl_buttonPanel.rowHeights = new int[]{29, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_buttonPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_buttonPanel.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		buttonPanel.setLayout(gbl_buttonPanel);
		
		btnPlay = new Button("Play");
		GridBagConstraints gbc_btnPlay = new GridBagConstraints();
		gbc_btnPlay.insets = new Insets(0, 0, 5, 0);
		gbc_btnPlay.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnPlay.gridx = 0;
		gbc_btnPlay.gridy = 1;
		buttonPanel.add(btnPlay, gbc_btnPlay);
		btnPlay.addActionListener(this);
		
		btnPause = new Button("Pause");
		GridBagConstraints gbc_btnPause = new GridBagConstraints();
		gbc_btnPause.anchor = GridBagConstraints.NORTH;
		gbc_btnPause.insets = new Insets(0, 0, 5, 0);
		gbc_btnPause.gridx = 0;
		gbc_btnPause.gridy = 4;
		buttonPanel.add(btnPause, gbc_btnPause);
		btnPause.addActionListener(this);
		
		btnStop = new Button("Stop");
		GridBagConstraints gbc_btnStop = new GridBagConstraints();
		gbc_btnStop.gridx = 0;
		gbc_btnStop.gridy = 7;
		buttonPanel.add(btnStop, gbc_btnStop);
		btnStop.addActionListener(this);
	}
	
	//Play video and audio simultaneously
	private void PlayVideo() {
		playingThread = new Thread() {
            public void run() {
	            System.out.println("Video Start!!!!");
	            updateGUI();
	            System.out.println("Video End!!!!!");
	        }
	    };
	    audioThread = new Thread() {
            public void run() {
            	System.out.println("Audio Start!!!!");
            	loadAudio(startFrame * CONVERSION);
            	 try {
         	        playSound.play();
         	    } catch (PlayWaveException e) {
         	        e.printStackTrace();
         	        return;
         	    }
	        }
	    };
	    audioThread.start();
	    playingThread.start();
	}
	
	private void updateGUI() {
		timer = new Timer(31, new ActionListener() { // 30 fps -> 33.333
	        @Override
	        public void actionPerformed(ActionEvent e) {
	        	if (startFrame <= 9000) {
	        		 loadNewFrame(startFrame);
	        		 imageLabel.setIcon(new ImageIcon(currentImg));
	        		 startFrame++;
	        	}
	        }
	    });
		timer.start();
	}
	
	private void loadAudio(int audioStartFrame) {
		String filename = videoFrameDir + ".wav";
	/*	FileInputStream inputStream;
		try {
		    inputStream = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		    return;
		}*/
	
		// initializes the playSound Object
		 playSound = new PlaySound(filename, audioStartFrame);
	
	/*	// plays the sound
		try {
		    playSound.play();
		} catch (PlayWaveException e) {
		    e.printStackTrace();
		    return;
		}*/
	}
	
	private void convertMetaDataFile() {
		Gson gson = new Gson();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(metaDataDir));
			newVideoMetaData = true;
		} catch (FileNotFoundException e) {
			newVideoMetaData = false;
			return;
		} 
		// load all the link and boundingbox information
		frameMap = gson.fromJson(br, new TypeToken<Map<Integer, Map<Integer, HyperLink>>>(){}.getType());
	}
	
	private void loadNewFrame(int count) {
		try {
			// remove the listener from previous frame
			for (MouseListener listener : listenerList) {
				imageLabel.removeMouseListener(listener);
			}
			listenerList = new ArrayList<MouseListener>();
			// if the target video has bounding boxes, draw them out too.
			if (newVideo) {
				convertMetaDataFile();
				newVideo = false;
			}
			
			String formatted = "";
  	        if (count < 10) {
  	        	formatted = "000" + count;
  	        } else if (count < 100) {
  	            formatted = "00" + count;
  	        } else if (count < 1000){
  	    	    formatted = "0" + count;
  	        } else {
  	    	    formatted += count;
  	        }
  	        String fullName = videoFrameDir + formatted + ".rgb";
	    	File file = new File(fullName);
	    	InputStream is = new FileInputStream(file);

	   	    long len = file.length();
		    byte[] bytes = new byte[(int)len];
		    int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        	offset += numRead;
	        }
	        System.out.println("Start loading frame: " + fullName);
	    	int index = 0;
	        currentImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	        for (int y = 0; y < HEIGHT; y++) {
	        	for (int x = 0; x < WIDTH; x++) {
	   				byte r = bytes[index];
	   				byte g = bytes[index+HEIGHT*WIDTH];
	   				byte b = bytes[index+HEIGHT*WIDTH*2]; 
	   				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	    			currentImg.setRGB(x,y,pix);
	    			index++;
	    		}
	    	}
	        if (newVideoMetaData) {
	        	drawLinkBoxes(count);
	            clickLinkBoxes(count);
	        }
	        is.close(); 
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }  
	}

	private void drawLinkBoxes(int frameNum) {
		
		Map<Integer, HyperLink> linkMap = frameMap.get(frameNum);
		if (linkMap == null) {
			return;
		}
		for (Map.Entry<Integer, HyperLink> links : linkMap.entrySet()) {
			HyperLink link = links.getValue();
			BoundingBox box = link.getBoxOnCurrentFrame();
			// get the points from the box and draw it one by one onto the frame
			Graphics g = currentImg.getGraphics();
		    g.setColor(FINAL_DRAWING_COLOR);
		    g.drawRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
		    g.dispose();
		    repaint();
		}
	}
	
	private void clickLinkBoxes(int frameNum) {
		Map<Integer, HyperLink> linkMap = frameMap.get(frameNum);
		if (linkMap == null) {
			return;
		}
		for (Map.Entry<Integer, HyperLink> links : linkMap.entrySet()) {
			HyperLink link = links.getValue();
			BoundingBox box = link.getBoxOnCurrentFrame();
			int minX = box.getX();
			int minY = box.getY();
			int maxX = box.getX()+ box.getWidth();
			int maxY = box.getY()+ box.getHeight();
			MouseListener listener = new MouseListener() {
			    @Override
			    public void mouseClicked(MouseEvent e) {
				    int mouseX = e.getX();
				    int mouseY = e.getY();
				    if ((mouseX >= minX && mouseX <= maxX)&&(mouseY >= minY && mouseY <= maxY)){
						timer.stop();
				    	videoFrameDir = link.getTargetVideoDir();
				    	int count2 = link.getTargetFrameNum();
				    	startFrame = count2;
				    	String[] segs = videoFrameDir.split("/");
				    	String videoName = segs[segs.length - 1]; // video name is the same as the json file name
				    	// TODO: relative path depends on the local dir
				    	metaDataDir = "../" + videoName + ".json";
				    	newVideo = true;
				    	// play from the new position in the new video
				    	
//				    	playSound.stop();
//				    	audioThread.interrupt();
				    	if (!audioThread.isAlive()) {
				    		audioThread = new Thread() {
					            public void run() { 
					            	// TODO: video chaining sound stop
					            	playSound.stop();
					            	System.out.println("Next Audio Start!!!!");
					            	loadAudio(startFrame * CONVERSION);
					            	 try {
					         	        playSound.play();
					         	    } catch (PlayWaveException e) {
					         	        e.printStackTrace();
					         	    }
						        }
						    };
						    audioThread.start();
				    	}
				    	timer.start();
				    }
				}
			    
				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
			};
		    imageLabel.addMouseListener(listener);
		    listenerList.add(listener);
		}
	}
	
	private void PauseVideo() {
		if(playingThread != null) {
			playingThread.interrupt();
			audioThread.interrupt();
			playSound.pause();
			playingThread = null;
			audioThread = null;
		}
		timer.stop();	
	}
	
	
	private void StopVideo() {
		if(playingThread != null) {
			playingThread.interrupt();
			audioThread.interrupt();
			playSound.stop();
			playingThread = null;
//			audioThread = null;
		} 
		timer.stop();
		startFrame = 1; // reset to the first frame
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.btnPlay) {
			System.out.println("play button clicked");
			if(this.playStatus > 1) {
				this.playStatus = 1;
				this.PlayVideo();
			}
		} else if(e.getSource() == this.btnPause) {
			System.out.println("pause button clicked");
			if(this.playStatus == 1) {
				this.playStatus = 2;
				this.PauseVideo();
			}
		} else if(e.getSource() == this.btnStop) {
			System.out.println("stop button clicked");
			if(this.playStatus < 3) {
				this.playStatus = 3;
				this.StopVideo();
			}
		}
	}
	
	public void showUI() {
	    pack();
	    setVisible(true);
	}
	
/************************* Change window size **************************/
	
	private static final int PREF_W = 653;
	private static final int PREF_H = 448;

	public void setUp() {
	   // TODO finish
	}

	@Override
	public Dimension getPreferredSize() {
	    return new Dimension(PREF_W, PREF_H);
	}
	
/****************************** main *****************************************/
	
	public static void main(String[] args) {	
		VideoPlay ui = new VideoPlay();
		ui.showUI();
		ui.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		// hard coded path for now
//	    ui.videoFrameDir = "AIFilm/AIFilmOne/AIFilmOne";
//	    ui.metaDataDir = "AIFilmOne.json";
		
		// TODO: relative path depends on the local dir
		ui.metaDataDir = "../" + args[1];
		System.out.println(ui.metaDataDir);
		String videoName = args[1].split("\\.")[0];
		ui.videoFrameDir = "../" + args[0] + "/" + videoName + "/" + videoName;
		System.out.println(ui.videoFrameDir);
	    
	    // pre-process the meta data
	    ui.convertMetaDataFile();
	    
	    // display the first frame on the screen when initializing
	    ui.loadNewFrame(1);
	    ui.loadAudio(0);
		ui.imageLabel.setIcon(new ImageIcon(ui.currentImg));
	}
		
}
	