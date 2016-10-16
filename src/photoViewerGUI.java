
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.sql.SQLException;
import javax.accessibility.Accessible;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 *
 * @author jack
 */
public class photoViewerGUI extends JFrame implements ActionListener, Serializable, Accessible {

    Container mainWindow;
    JPanel controlPane;
    JMenuBar topBar = null;
    JMenu fileMenu = null;
    JMenuItem saveMenuItem, exitMenuItem, browseMenuItem, maintainMenuItem;
    JMenu viewMenu = null;
    JTextField currPageText;
    JTextArea descriptionTextArea, totalPageText;
    JLabel imageLabel = null;
    JPanel buttonPane;
    JTextField dateTextField;
    JButton nextButton, prevButton, deleteButton, saveButton, addButton;
    private int maxIndex;
    private int currIndex;
    databaseManager db;
    databasePhoto photo;

    public photoViewerGUI() // Constructor
    {

	// Set title to string passed in
	super("Photo viewer");
	mainWindow = getContentPane();

	databaseManager db = new databaseManager();

	imageLabel = new JLabel("", SwingConstants.CENTER);
	JScrollPane scrollPane = new JScrollPane(imageLabel);

	controlPane = new JPanel();
	controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.PAGE_AXIS));

	createMenus();

	createInfoSpace();

	createButtons();

	mainWindow.add(controlPane, BorderLayout.SOUTH);

	mainWindow.add(scrollPane);

	this.setMinimumSize(getSize());

	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});

    }

    void changeImage() {
	try {
	    byte[] rawImage = db.getNewPhoto(currIndex).imageArray;
	    if (rawImage != null) {
		ImageIcon image = new ImageIcon(rawImage);
		imageLabel.setIcon(image);
	    }
	}
	catch (SQLException e) {
	    System.err.println("Error loading new photo");
	    e.printStackTrace();
	}
    }

    private void createMenus() {
	// Create menu system and fill with wonderous sub-menus

	topBar = new JMenuBar();
	fileMenu = new JMenu("File");
	topBar.add(fileMenu);
	saveMenuItem = new JMenuItem("Save");
	exitMenuItem = new JMenuItem("Exit");
	fileMenu.add(saveMenuItem);

	/*

	saveMenuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		try {
		    FileOutputStream fileOut = new FileOutputStream("photolib");
		    ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
		    objectOut.writeObject(images);
		    objectOut.close();
		    fileOut.close();
		}
		catch (SQLException ioe) {
		    System.err.println("SQL error. Database not updated.");
		}
	    }
	});

	 */
	fileMenu.add(exitMenuItem);

	exitMenuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e
	    ) {
		int dialogButton = JOptionPane.YES_NO_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit? Changes may not be saved!", "Warning", dialogButton);
		if (dialogResult == JOptionPane.YES_OPTION) {
		    System.exit(0);
		}
	    }
	});

	viewMenu = new JMenu("View");
	browseMenuItem = new JMenuItem("Browse");

	browseMenuItem.addActionListener(
		new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e
	    ) {
		buttonPane.setVisible(false);
		descriptionTextArea.setEditable(false);
		dateTextField.setEditable(false);
	    }
	}
	);
	maintainMenuItem = new JMenuItem("Maintain");

	maintainMenuItem.addActionListener(
		new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e
	    ) {
		buttonPane.setVisible(true);
		descriptionTextArea.setEditable(true);
		dateTextField.setEditable(true);
	    }
	}
	);
	topBar.add(viewMenu);

	viewMenu.add(browseMenuItem);

	viewMenu.add(maintainMenuItem);

	this.setJMenuBar(topBar);
    }

    private void createInfoSpace() {
	JPanel descriptionPane = new JPanel();
	descriptionPane.setLayout(new FlowLayout(FlowLayout.LEFT));

	JLabel descriptionLabel = new JLabel("Description:");
	descriptionTextArea = new JTextArea(4, 20);
	descriptionTextArea.setEditable(false);

	descriptionPane.add(descriptionLabel);
	descriptionPane.add(descriptionTextArea);

	JPanel datePane = new JPanel();
//		datePane.setLayout(new FlowLayout(FlowLayout.LEFT));
//		datePane.setLayout(new BoxLayout(datePane, BoxLayout.LINE_AXIS));

	JLabel dateLabel = new JLabel("Date:");
	dateLabel.setPreferredSize(new Dimension(descriptionLabel.getPreferredSize().width, dateLabel.getPreferredSize().height));
	dateTextField = new JTextField();
	dateTextField.setEditable(false);
	dateTextField.setPreferredSize(new Dimension(100, 25));
	datePane.add(dateLabel);
	datePane.add(dateTextField);
	//datePane.add(Box.createHorizontalGlue());

	deleteButton = new JButton("Delete");
	deleteButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		try {
		    db.deletePhoto(currIndex);

		}
		catch (SQLException ee) {
		    System.err.println("Couldn't delete photo");
		}
	    }
	});

	saveButton = new JButton("Save");

	/*
	saveButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		images.get(imageNumber).setDescription(descriptionTextArea.getText());
		images.get(imageNumber).setDate(dateTextField.getText());

	    }
	});
	 */
	addButton = new JButton("Add Items");
	addButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.changeToParentDirectory();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
			"JPG & GIF Images", "jpg", "gif");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    preparePhoto(chooser.getSelectedFile().getAbsolutePath());
		}
	    }
	});

	buttonPane = new JPanel();
	buttonPane.add(deleteButton);
	buttonPane.add(saveButton);
	buttonPane.add(addButton);

	JPanel leftRightPane = new JPanel();
	leftRightPane.setLayout(new BorderLayout());
	leftRightPane.add(datePane, BorderLayout.WEST);
	leftRightPane.add(buttonPane, BorderLayout.EAST);
	buttonPane.setVisible(false);
	controlPane.add(descriptionPane);
	controlPane.add(leftRightPane);

    }

    private void createButtons() {
	Container bottomPanel = new JPanel();
	Container bottomPanelWest = new JPanel();
	Container bottomPanelEast = new JPanel();
	FlowLayout flleft = new FlowLayout(FlowLayout.LEFT, 5, 20);
	FlowLayout flright = new FlowLayout(FlowLayout.RIGHT, 5, 20);
	bottomPanelWest.setLayout(new FlowLayout(FlowLayout.LEFT));
	bottomPanelEast.setLayout(new FlowLayout(FlowLayout.RIGHT));
	bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

	// Create buttons and text fields
	currPageText = new JTextField("1");
	totalPageText = new JTextArea();

	totalPageText.setText(String.valueOf(maxIndex));

	prevButton = new JButton("<Prev");
	nextButton = new JButton("Next>");
	prevButton.setEnabled(false);
	if (maxIndex <= 1) {
	    nextButton.setEnabled(false);
	}

	/*
	JSlider zoomSlide = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
	zoomSlide.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		System.out.println("Value : "
			+ ((JSlider) e.getSource()).getValue());
	    }
	});
	 */
	// Init action listeners to check for button presses
	prevButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		prevButtonAction();
	    }
	});
	nextButton.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		nextButtonAction();
	    }
	});

	bottomPanelWest.add(currPageText);
	bottomPanelWest.add(totalPageText);
	bottomPanelWest.add(prevButton);
	bottomPanelWest.add(nextButton);
	// bottomPanelEast.add(zoomSlide);

	bottomPanel.add(bottomPanelWest);
	bottomPanel.add(Box.createHorizontalGlue());
	bottomPanel.add(bottomPanelEast);

	controlPane.add(bottomPanel);

    }

    /*
    private ImageIcon resizeImage(ImageIcon image) {
	int height = image.getIconHeight();
	int width = image.getIconWidth();
	Image tempimg = image.getImage(); // transform it
	Image newimg = tempimg.getScaledInstance(width / 2, height / 2, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
	return new ImageIcon(newimg);
    }

    private void importPhotos() {
	String currentDirectory;
	File file = new File(".");
	currentDirectory = file.getAbsolutePath();

    }

     */
    @Override
    public void actionPerformed(ActionEvent evt) {
	System.out.println("Action?");
    }

    private void prevButtonAction() {
	if (currIndex > 0) {
	    currIndex--;
	    if (currIndex < maxIndex) {
		nextButton.setEnabled(true);
	    }
	    if (currIndex <= 0) {
		prevButton.setEnabled(false);
	    }
	    try {
		photo = db.getNewPhoto(currIndex);
		setPhoto(photo);
	    }
	    catch (SQLException e) {
		System.err.println("SQL error on prev button");
	    }

	}

    }

    private void nextButtonAction() {
	if (currIndex < maxIndex) {
	    currIndex++;
	    if (currIndex == 1) {
		prevButton.setEnabled(true);
	    }
	    else if (currIndex >= maxIndex) {
		nextButton.setEnabled(false);
	    }
	    try {
		photo = db.getNewPhoto(currIndex);
		setPhoto(photo);
	    }
	    catch (SQLException e) {
		System.err.println("SQL error on next button");
	    }

	}

    }

    private void preparePhoto(File input) {
	try {
	    BufferedImage bufferedImage = ImageIO.read(input);

	    // get DataBufferBytes from Raster
	    WritableRaster raster = bufferedImage.getRaster();
	    DataBufferByte data = (DataBufferByte) raster.getDataBuffer();

	    databasePhoto addPhoto = new databasePhoto();
	    addPhoto.imageArray = data.getData();

	    db.addPhotoToDatabase(addPhoto);
	}
	catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private void setPhoto(databasePhoto newPhoto) {

	ImageIcon image = new ImageIcon(newPhoto.imageArray);
	imageLabel.setIcon(image);
	descriptionTextArea.setText(newPhoto.description);
	dateTextField.setText(newPhoto.date);

    }

    @Override
    public Dimension getMinimumSize() {
	return new Dimension(500, 500);
    }

    @Override
    public Dimension getPreferredSize() {
	return new Dimension(750, 750);
    }

    public static void main(String[] args) {

	JFrame frame = new photoViewerGUI();

	frame.pack();

	frame.setVisible(true);

	System.out.println("Program begins...");

    }

}
