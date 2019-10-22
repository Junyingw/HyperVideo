import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class VideoGUI extends JFrame implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	// the dimension of the imported video
    static final int WIDTH = 352;
    static final int HEIGHT = 288;
    
    private JLabel imageLabel;
    private JLabel resultImageLabel;
    private Button loadPrimaryButton;
    private Button loadSecondaryButton;
    private JSlider frameSlider1;
    private JSlider frameSlider2;
    private JFileChooser chooser;
    private JTable linkTable;
    
    // hard-coded content for table
    // by default showing 20 empty cells
 	private String[][] data = { 
 		{ "" }, { "" }, { "" }, { "" }, { "" }, 
 		{ "" }, { "" }, { "" }, { "" }, { "" }, 
 		{ "" }, { "" }, { "" }, { "" }, { "" }, 
 		{ "" }, { "" }, { "" }, { "" }, { "" }, 
 		{ "" }, { "" }, { "" }, { "" }, { "" }, 
 		{ "" }, { "" }, { "" }, { "" }, { "" }, 
 	}; 
 	
 	private String[] columnNames = { "" }; 
    
    private BufferedImage primaryImg;
    private BufferedImage secondaryImg;
    
    // the path to the corresponding folders to be opened
    private String directPrimary;
    private String directSecondary;
    
    private String primaryVideoName;
    
    private static final Color DRAWING_COLOR = new Color(255, 100, 200);
    private static final Color FINAL_DRAWING_COLOR = Color.red;
    private static final Color HIGHLIGHT_COLOR = Color.green;
    
    private Point startPt = null;
    private Point endPt = null;
    private Point currentPt = null;
    
    Point p1 = new Point(0, 0);
    
    private boolean loadNewPrimary;
    
    private Map<Integer, Map<Integer, HyperLink>> frameMap;
    
    // TODO: experimenting area
    private int startFrame = -1;
    private int endFrame = -1;
    
    private BoundingBox startBoundingBox;
    private BoundingBox endBoundingBox;
    
	public VideoGUI() {
		
	   /*********************************** Primary Panel *********************************************/
		
		frameMap = new HashMap<Integer, Map<Integer, HyperLink>>();
		
	    Panel loadPanel = new Panel();
	    loadPrimaryButton = new Button("Load Primary Video");
	    loadPrimaryButton.addActionListener(this);
	    loadPanel.setLayout(new GridLayout(0, 1, 0, 0));
	    JLabel queryLabel = new JLabel("Action:");
	    loadPanel.add(queryLabel);
	    loadPanel.add(loadPrimaryButton);
	    loadSecondaryButton = new Button("Load Secondary Video");
	    loadPanel.add(loadSecondaryButton);
	    loadSecondaryButton.addActionListener(this);
	    Panel primaryPanel = new Panel();
	    primaryPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
	    primaryPanel.add(loadPanel);
	    
	    JButton newHyperlinkButton = new JButton("Create New Hyperlink");
	    loadPanel.add(newHyperlinkButton);
	    // TODO: experimenting area
	    newHyperlinkButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!loadNewPrimary) {
			    	// create a new link in the next available location
			    	int row = linkTable.getSelectedRow();
			    	linkTable.setValueAt("New Link", row + 1, 0);
			    	// interpolating between all the frames 
			    	interpolatingBoundingBox(row + 1);
			    } else {
			    	linkTable.setValueAt("New Link", 0, 0);
			    	interpolatingBoundingBox(0);
			    	loadNewPrimary = false;
			    }
			}
	    });
	    
	    getContentPane().add(primaryPanel, BorderLayout.WEST);
	      
	    /***************************************** Secondary Panel *****************************************/
	    
	    Panel secondaryPanel = new Panel();
	    secondaryPanel.setMinimumSize(new Dimension(5, 5));
	    getContentPane().add(secondaryPanel, BorderLayout.EAST);
	    secondaryPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 2, 2));
	    
	    JLabel selectLink = new JLabel("Select Link:");
	    secondaryPanel.add(selectLink);
	    
	    linkTable = new JTable(data, columnNames);
	    // adding it to JScrollPane to make it scrollable
	 	JScrollPane scrollPane = new JScrollPane(linkTable); 
	 	scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); 
	 	linkTable.setPreferredScrollableViewportSize(new Dimension(100,50));
	    secondaryPanel.add(scrollPane);
	    
	    // TODO: experimenting area
	    linkTable.addMouseListener(new MouseAdapter() {
	    	@Override
	        public void mouseClicked(MouseEvent evt) {
	            int row = linkTable.rowAtPoint(evt.getPoint());
	            int col = linkTable.columnAtPoint(evt.getPoint());
	            String targetLinkName = (String) linkTable.getModel().getValueAt(row, col);
	            System.out.println(row + " " + col);
	            if (row >= 0 && col >= 0) {
	                // look it up in the frame map
	            	for (Map.Entry<Integer, Map<Integer, HyperLink>> frames : frameMap.entrySet()) {
	            		Map<Integer, HyperLink> linkMap = frames.getValue();
	            		for (Map.Entry<Integer, HyperLink> links : linkMap.entrySet()) {
	            			HyperLink link = links.getValue();
	            			if (link.getName() != null && link.getName().equals(targetLinkName)) {
	            				int start = link.getStartFrameNum();
	            				System.out.println(link.getName());
	            				// jump to the start frame
	            				frameSlider1.setValue(start);
	    	            		// repaint and highlight that box
	    	            		repaintAndHighlight(start, link);
	            				return;
	            			}
	            		}
	            	}
	            }
	        }
	    });
	    
	    JButton connectVideoButton = new JButton("Connect Video");
	    secondaryPanel.add(connectVideoButton);
	    connectVideoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int linkRow = linkTable.getSelectedRow();
				String currentLinkName = linkTable.getValueAt(linkRow, 0).toString();
				for (int i = startFrame; i <= endFrame; i++) {
					Map<Integer, HyperLink> currentFrameLinks = frameMap.get(i);
					HyperLink currentLink = currentFrameLinks.get(linkRow);
					currentLink.setName(currentLinkName);
					currentLink.setStartFrameNum(startFrame);
					currentLink.setEndFrameNum(endFrame);
					currentLink.setTargetVideoDir(directSecondary);
					currentLink.setTargetFrameNum(frameSlider2.getValue());
				}
				// reset the start and end frame pair for one link
				startFrame = -1;
				endFrame = -1;
			}
	    });
	    
	    JButton saveFileButton = new JButton("Save File");
	    secondaryPanel.add(saveFileButton);
	    saveFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// create the .json file here with primary video's name as the file name
				try (Writer writer = new FileWriter(primaryVideoName + ".json")) {
				    Gson gson = new GsonBuilder().create();
				    gson.toJson(frameMap, writer);
				} catch (Exception error) {
					error.printStackTrace();
				}
			}
	    });
	    
	    Panel framePanel = new Panel();
	    framePanel.setLayout(new GridLayout(2, 2));
	    
	    //Drawing bounding box
	    this.imageLabel = new JLabel() {
			private static final long serialVersionUID = 1L;
			@Override
			public void paint(Graphics g) {
				super.paint(g);
			    if (primaryImg != null) {
			        this.setIcon(new ImageIcon(primaryImg));
			        g.drawImage(primaryImg, 0, 0, this);
			    }
			      
			    if (startPt != null && currentPt != null) {
			        g.setColor(DRAWING_COLOR);
			        int x = Math.min(startPt.x, currentPt.x);
			        int y = Math.min(startPt.y, currentPt.y);
			        int width = Math.abs(startPt.x - currentPt.x);
			        int height = Math.abs(startPt.y - currentPt.y);
			        g.drawRect(x, y, width, height);
			    }
			}
	    };
	    
	   class MyMouseAdapter extends MouseAdapter {
		     @Override
		     public void mouseDragged(MouseEvent mEvt) {
		        currentPt = mEvt.getPoint();
		        VideoGUI.this.repaint();
		     }

		     @Override
		     public void mouseReleased(MouseEvent mEvt) {
		        endPt = mEvt.getPoint();
		        currentPt = null;
		        drawToBackground();
		     }

		     @Override
		     public void mousePressed(MouseEvent mEvt) {
		        startPt = mEvt.getPoint();
		     }
		}

		MyMouseAdapter myMouseAdapter = new MyMouseAdapter();
		imageLabel.addMouseMotionListener(myMouseAdapter);
		imageLabel.addMouseListener(myMouseAdapter);
	    
	    this.resultImageLabel = new JLabel();
	    
	    Panel imagePanel = new Panel();
	    imagePanel.add(this.imageLabel);
	    Panel resultImagePanel = new Panel();
	    resultImagePanel.add(this.resultImageLabel);
	    framePanel.add(imagePanel);
	    framePanel.add(resultImagePanel);
	    
	    /*********************************************** Frame Panel ****************************************/
	    
	    Panel controlPanel = new Panel();
	    Panel resultControlPanel = new Panel();
	    framePanel.add(controlPanel);
	    framePanel.add(resultControlPanel);
	    controlPanel.setLayout(new BorderLayout(0, 0));
	    
	    frameSlider1 = new JSlider(1, 9000, 1);
	    frameSlider1.setToolTipText("Frame");
	    controlPanel.add(frameSlider1);
	    
	    frameSlider1.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int index = frameSlider1.getValue();
				loadPrimaryVideo(directPrimary, index);
				repaintExistingBoxes(index);
                imageLabel.setIcon(new ImageIcon(primaryImg));
				frameSlider1.setToolTipText("Frame " + index);
			}	
	    });
	    
	    resultControlPanel.setLayout(new BorderLayout(0, 0));
	    
	    frameSlider2 = new JSlider(1, 9000, 1);
	    resultControlPanel.add(frameSlider2);
	    frameSlider2.setToolTipText("Frame");
	    
	    frameSlider2.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int index = frameSlider2.getValue();
			    loadSecondaryVideo(directSecondary, index);
			    resultImageLabel.setIcon(new ImageIcon(secondaryImg));
			    frameSlider2.setToolTipText("Frame " + index);
			}	
	    });
	    
	    getContentPane().add(framePanel, BorderLayout.SOUTH);
	}
	
	public void drawToBackground() {
		Graphics g = primaryImg.getGraphics();
	    g.setColor(FINAL_DRAWING_COLOR);
	    int x = Math.min(startPt.x, endPt.x);
	    int y = Math.min(startPt.y, endPt.y);
	    int width = Math.abs(startPt.x - endPt.x);
	    int height = Math.abs(startPt.y - endPt.y);
	    g.drawRect(x, y, width, height);
	    g.dispose();
	    System.out.println(x+","+y+","+width+","+height);
	    // create the bounding box object
	    BoundingBox box = new BoundingBox(x, y, width, height);
	    
	    // tracking & interpolating each bounding box and associate them to a same link object
	    startPt = null;
	    VideoGUI.this.repaint();

	    // set the start and end position pair
	    if (startFrame == -1 && endFrame == -1) {
	    	startFrame = frameSlider1.getValue();
	    	startBoundingBox = box;
	    } else {
	    	endFrame = frameSlider1.getValue();
	    	endBoundingBox = box;
	    }
	}
	
	public void showUI() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(0,0,screenSize.width, screenSize.height);
	    pack();
	    setVisible(true);
	}
	
	private void repaintAndHighlight(int frameNum, HyperLink link) {
		Map<Integer, HyperLink> linkMap = frameMap.get(frameNum);
		for (Map.Entry<Integer, HyperLink> links : linkMap.entrySet()) {
			HyperLink temp = links.getValue();
			if (temp != null) {
				System.out.println(link.getName());
				Graphics g = primaryImg.getGraphics();
				if (link.getName().equals(temp.getName())) {
				    g.setColor(HIGHLIGHT_COLOR);
				} else {
					g.setColor(FINAL_DRAWING_COLOR);
				}
				BoundingBox box = temp.getBoxOnCurrentFrame();
			    g.drawRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
			    g.dispose();
			    VideoGUI.this.repaint();
			}
		}
	}
	
	private void repaintExistingBoxes(int frameNum) {
		Map<Integer, HyperLink> linkMap = frameMap.get(frameNum);
		if (linkMap != null) {
			for (Map.Entry<Integer, HyperLink> links : linkMap.entrySet()) {
				HyperLink temp = links.getValue();
				if (temp != null) {
					Graphics g = primaryImg.getGraphics();
					g.setColor(FINAL_DRAWING_COLOR);
					BoundingBox box = temp.getBoxOnCurrentFrame();
				    g.drawRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
				    g.dispose();
				    VideoGUI.this.repaint();
				}
			}
		}
	}
	
	private void interpolatingBoundingBox(int row) {
		// adding box and link relationship to all frames in between
		int startX = startBoundingBox.getX();
		int startY = startBoundingBox.getY();
		int startWidth = startBoundingBox.getWidth();
		int startHeight = startBoundingBox.getHeight();
		
		int endX = endBoundingBox.getX();
		int endY = endBoundingBox.getY();
		int endWidth = endBoundingBox.getWidth();
		int endHeight = endBoundingBox.getHeight();
		
		int deltaFrameNum = endFrame - startFrame;
		double deltaX = ((double) (endX - startX)) / deltaFrameNum;
		double deltaY = ((double) (endY - startY)) / deltaFrameNum;
		double deltaWidth = ((double) (endWidth - startWidth)) / deltaFrameNum;
		double deltaHeight = ((double) (endHeight - startHeight)) / deltaFrameNum;
		
		int count = 0;
		for (int i = startFrame; i <= endFrame; i++) {
			Map<Integer, HyperLink> links = frameMap.getOrDefault(i, new HashMap<Integer, HyperLink>());
	    	HyperLink newLink = new HyperLink();
	    	// save the bounding box to the map with the corresponding frame number
	    	int tempX = (int) (startX + count * deltaX);
	    	int tempY = (int) (startY + count * deltaY);
	    	int tempWidth = (int) (startWidth + count * deltaWidth);
	    	int tempHeight = (int) (startHeight + count * deltaHeight);
	    	BoundingBox box = new BoundingBox(tempX, tempY, tempWidth, tempHeight);
	    	newLink.setBoxOnCurrentFrame(box);
	    	links.put(row, newLink);
	    	// save the links to their corresponding frame
	    	frameMap.put(i, links);
	    	count++;
		}
	}
	
	/******************************************** Load Primary Video ***********************************/
	
	private void loadPrimaryVideo(String userInput, int count) {
		System.out.println("Start loading primary video frames.");
		try {
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
		    String fullName = userInput + formatted + ".rgb";
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
		    primaryImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		    for (int y = 0; y < HEIGHT; y++) {
		    	for (int x = 0; x < WIDTH; x++) {
		    		byte r = bytes[index];
		   			byte g = bytes[index+HEIGHT*WIDTH];
		   			byte b = bytes[index+HEIGHT*WIDTH*2]; 
		   			int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
		    		primaryImg.setRGB(x,y,pix);
		    		index++;
		    	}
		    }
		    is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	/******************************************** Load Secondary Video ***********************************/
	
	private void loadSecondaryVideo(String userInput, int count) {
		System.out.println("Start loading secondary video frames.");
		try {
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
  	        String fullName = userInput + formatted + ".rgb";
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
	        secondaryImg = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	        for (int y = 0; y < HEIGHT; y++) {
	        	for (int x = 0; x < WIDTH; x++) {
	   				byte r = bytes[index];
	   				byte g = bytes[index+HEIGHT*WIDTH];
	   				byte b = bytes[index+HEIGHT*WIDTH*2]; 
	   				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	    			secondaryImg.setRGB(x,y,pix);
	    			index++;
	    		}
	    	}
	        is.close(); 
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }  
	}
	
	/************************* Change window size **************************/
	
	private static final int PREF_W = 1200;
	private static final int PREF_H = 800;

	public void setUp() {
	   // TODO finish
	}

	@Override
	public Dimension getPreferredSize() {
	    return new Dimension(PREF_W, PREF_H);
	}
	
	/**************************** Event Trigger ****************************/
	@Override
	public void actionPerformed(ActionEvent e) {
		chooser = new JFileChooser(); 
	    chooser.setCurrentDirectory(new java.io.File("."));
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);

	    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
	      System.out.println("getCurrentDirectory(): " +  chooser.getCurrentDirectory());
	      System.out.println("getSelectedFile() : " +  chooser.getSelectedFile());
	      String[] segs = chooser.getSelectedFile().getAbsolutePath().split("/");
	      String videoName = segs[segs.length - 1];
	      String direct = chooser.getSelectedFile().getAbsolutePath() + "/" + videoName;

	      // dynamically load the video as needed, initially just load the first frame
	      if(e.getSource() == this.loadPrimaryButton) {
	    	  // reset the linkTable and frame map whenever we load a new primary video
	    	  for (String[] row : data) {
	    		  Arrays.fill(row, "");	 	
	    	  }
	    	  Arrays.fill(columnNames, "");
	    	  loadNewPrimary = true;
	    	  linkTable.getSelectionModel().clearSelection();
	    	  frameMap = new HashMap<Integer, Map<Integer, HyperLink>>();
	    	  
	    	  directPrimary = direct;
	    	  this.loadPrimaryVideo(directPrimary, 1);
	    	  // reset the slider position
	    	  frameSlider1.setValue(1);
	    	  primaryVideoName = videoName;
	    	  this.imageLabel.setIcon(new ImageIcon(primaryImg));
	    	  
			} else if(e.getSource() == this.loadSecondaryButton) {
			  directSecondary = direct;
			  this.loadSecondaryVideo(directSecondary, 1);
			  // reset the slider position
			  frameSlider2.setValue(1);
			  this.resultImageLabel.setIcon(new ImageIcon(secondaryImg));
			}
	      }	
	    else {
	      System.out.println("No Selection ");
	    }
	}	
	
	/****************************** main *****************************************/
	
	public static void main(String[] args) {	
		VideoGUI ui = new VideoGUI();
		ui.showUI();
		ui.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
 }