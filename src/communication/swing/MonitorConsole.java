package communication.swing;


import bka.communication.*;
import bka.swing.*;
import gnu.io.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.event.*;
import org.json.*;


public class MonitorConsole extends FrameApplication {
    
    
    public MonitorConsole() {
        initComponents();
        initMenus();
        findPorts();
        for (int baudRate : getBaudRates()) {
            baudComboBox.addItem(baudRate);
        }
        initListeners();
    }

  
    private int[] getBaudRates() {
        JSONArray configurationRates = getConfigurationArray("baud_rates");
        if (configurationRates == null) {
            return DEFAULT_BAUD_RATES;
        }
        int[] baudRates = new int[configurationRates.length()];
        for (int i = 0; i < configurationRates.length(); ++i) {
            baudRates[i] = configurationRates.optInt(i);
        }
        return baudRates;
    }


    private void initComponents() {
        getContentPane().setLayout(new BorderLayout());
        setBackground(java.awt.Color.WHITE);
        JPanel sendPanel = new JPanel();
        sendText.setPreferredSize(new Dimension(430, 25));
        sendPanel.add(sendText);
        sendButton.setText("Send");
        sendButton.setPreferredSize(new Dimension(75, 25));
        sendPanel.add(sendButton);
        getContentPane().add(sendPanel, BorderLayout.SOUTH);
        receivePane = new JScrollPane();
        receivePane.setPreferredSize(new Dimension(430, 400));
        JScrollBar scrollBar = receivePane.getVerticalScrollBar();
        scrollBar.getModel().addChangeListener(new ScrollChangeListener());
        receivedText.setEditable(false);
        receivedText.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
        receivedText.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
        receivePane.getViewport().add(receivedText);
        getContentPane().add(receivePane, BorderLayout.CENTER);
        JPanel choicePanel = new JPanel();
        portComboBox.setPreferredSize(new Dimension(225, 25));
        choicePanel.add(portComboBox, BorderLayout.NORTH);
        baudComboBox.setPreferredSize(new Dimension(100, 25));
        choicePanel.add(baudComboBox);
        textJRadioButton = new JRadioButton("Text", true);
        byteJRadioButton = new JRadioButton("Byte", false);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(textJRadioButton);
        buttonGroup.add(byteJRadioButton);
        choicePanel.add(textJRadioButton);
        choicePanel.add(byteJRadioButton);
        getContentPane().add(choicePanel, BorderLayout.NORTH);
        setTitle("COM Monitor");
        pack();
    }


    private void initMenus() {
        fileMenu.setText("File");
        fileMenu.add(newMenuItem);
        newMenuItem.setEnabled(false);
        newMenuItem.setText("New");
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        fileMenu.add(openMenuItem);
        openMenuItem.setText("Open...");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        fileMenu.add(saveMenuItem);
        saveMenuItem.setText("Save");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        fileMenu.add(saveAsMenuItem);
        saveAsMenuItem.setEnabled(false);
        saveAsMenuItem.setText("Save As...");
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        exitMenuItem.setText("Exit");
        mainMenuBar.add(fileMenu);
        editMenu.setText("Edit");
        editMenu.add(cutMenuItem);
        cutMenuItem.setEnabled(false);
        cutMenuItem.setText("Cut");
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
        editMenu.add(copyMenuItem);
        copyMenuItem.setEnabled(false);
        copyMenuItem.setText("Copy");
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        editMenu.add(pasteMenuItem);
        pasteMenuItem.setEnabled(false);
        pasteMenuItem.setText("Paste");
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
        mainMenuBar.add(editMenu);     
        setJMenuBar(mainMenuBar);
    }

 
    private void initListeners() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new MonitorWindowAdapter());
        ComponentActionListener actionListener = new ComponentActionListener();
        sendButton.addActionListener(actionListener);
        sendText.addActionListener(actionListener);
        saveMenuItem.addActionListener(actionListener);
        openMenuItem.addActionListener(actionListener);
        exitMenuItem.addActionListener(actionListener);
        ComponentItemListener itemListener = new ComponentItemListener();
        portComboBox.addItemListener(itemListener);
        baudComboBox.addItemListener(itemListener);
        textJRadioButton.addItemListener(itemListener);
        byteJRadioButton.addItemListener(itemListener);
    }


    public static void main(String args[]) {
        try {
            MonitorConsole console = new MonitorConsole();
            console.parseArguments(args);
            String baud = console.getProperty(BAUD_PROPERTY_KEY);
            if (baud != null) {
                console.baudComboBox.setSelectedItem(Integer.parseInt(console.getProperty(BAUD_PROPERTY_KEY)));
            }
            console.portComboBox.setSelectedItem(console.getProperty(PORT_PROPERTY_KEY));
            console.setVisible(true);
        }
        catch (NumberFormatException ex) {
            getLogger().log(Level.SEVERE, "main", ex);
            System.exit(1);
        }
    }

    
    @Override
    public String manufacturerName() {
        return "Bka";
    }

    
    @Override
    public String applicationName() {
        return "Monitor Console";
    }

    
   @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }


    @Override
    public void addNotify() {
        // Record the size of the window prior to calling parents addNotify.
        Dimension dimension = getSize();
        super.addNotify();
        if (componentsAdjusted) {
            return;
        }
        // Adjust components according to the insets
        setSize(getInsets().left + getInsets().right + dimension.width, getInsets().top + getInsets().bottom + dimension.height);
        Component components[] = getComponents();
        for (Component component : components) {
            Point point = component.getLocation();
            point.translate(getInsets().left, getInsets().top);
            component.setLocation(point);
        }
        componentsAdjusted = true;
    }

    
    private void openMenuItem_actionPerformed(ActionEvent event) {
        JFileChooser fileChooser = createFileChooser();
        fileChooser.showOpenDialog(this);
        File file = fileChooser.getSelectedFile();
        if (file.exists()) {
            defaultDirectory = fileChooser.getCurrentDirectory();
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                String text;
                while ((text = randomAccessFile.readLine()) != null) {
                    transmitText(text);
                }
            }
            catch (IOException ex) {
                Logger.getLogger(MonitorConsole.class.getName()).log(Level.SEVERE, "openMenuItem_ActionPerformed", ex);
            }
        }
    }


    private void saveMenuItem_actionPerformed(ActionEvent event) {
        JFileChooser fileChooser = createFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.showSaveDialog(this);
        File file = fileChooser.getSelectedFile();
        if (file != null) {
            defaultDirectory = fileChooser.getCurrentDirectory();
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                randomAccessFile.writeBytes(receivedText.getText());
                randomAccessFile.close();
            }
            catch (Exception ex) {
                Logger.getLogger(MonitorConsole.class.getName()).log(Level.SEVERE, "saveMenuItem_ActionPerformed", ex);
            }
        }
    }


    private void exitMenuItem_actionPerformed(ActionEvent evt) {
        System.exit(0);
    }


    private void portComboBox_itemStateChanged(ItemEvent evt) {
        String selected = (String) evt.getItem();
        setProperty(PORT_PROPERTY_KEY, selected);
        if (! NO_SELECTION.equals(selected)) {
            openSerialPort((CommPortIdentifier) tableOfPorts.get(selected));
        }
        else {
            openSerialPort(null);
        }
    }


    private void baudComboBox_itemStateChanged(ItemEvent evt) {
        if (serialPort != null) {
            setProperty(BAUD_PROPERTY_KEY, evt.getItem().toString());
            setSerialPortParams();
        }
    }


    private void sendButton_actionPerformed(ActionEvent evt) {
        transmitText(sendText.getText());
    }


    private void sendText_actionPerformed(ActionEvent evt) {
        transmitText(sendText.getText());
    }

    
    private void openSerialPort(CommPortIdentifier commPortIdentifier) {
        if (openedPort != null && serialPort != null) {
            serialPort.close();
        }
        if (commPortIdentifier != null) {
            try {
                serialPort = (SerialPort) commPortIdentifier.open(MonitorConsole.class.getName(), 2000);
            } 
            catch (PortInUseException ex) {
                serialPort = null;
                logOpenPortException(commPortIdentifier, ex);
            }
            if (serialPort != null) {
                try {
                    outputStream = serialPort.getOutputStream();
                    inputStream = serialPort.getInputStream();
                } 
                catch (IOException ex) {
                    logOpenPortException(commPortIdentifier, ex);
                }
                setSerialPortParams();
                serialPort.notifyOnDataAvailable(true);
                try {
                    serialPort.addEventListener(new CommPortListener());
                }
                catch (TooManyListenersException ex) {
                    logOpenPortException(commPortIdentifier, ex);
                }
                openedPort = commPortIdentifier;
            }
        }
    }


    private void logOpenPortException(CommPortIdentifier commPortIdentifier, Exception exception) {
        getLogger().log(Level.SEVERE, "Open port " + commPortIdentifier.getName(), exception);
    }


    private void findPorts() {
        String portFilter = getConfigurationString("port_filter");
        portComboBox.addItem(NO_SELECTION);
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
            String name = portId.getName();
            if (portFilter == null || name.matches(portFilter)) {
                tableOfPorts.put(name, portId);
                portComboBox.addItem(name);
            }
        }
    }


    private void transmitText(String text) {
        if (byteJRadioButton.isSelected()) {
            boolean ready = false;
            Scanner scanner = new Scanner(text);
            while (! ready) {
                try {
                    outputStream.write(scanner.nextInt());
                }
                catch (NoSuchElementException ex) {
                    getLogger().log(Level.FINEST, "Finised scanning", ex);
                    ready = true;
                }
                catch (IOException ex) {
                    getLogger().log(Level.WARNING, "Finised scanning", ex);
                    JOptionPane.showMessageDialog(this, ex.toString(), "Port error", JOptionPane.ERROR_MESSAGE);
                    ready = true;
                }
            }
        }
        else {
            try {
                outputStream.write((text + CR).getBytes());
                sendText.setText("");
            }
            catch (IOException ex) {
                getLogger().log(Level.WARNING, "transmit", ex);
                JOptionPane.showMessageDialog(this, ex.toString(), "Port error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void setSerialPortParams() {
        try {
            serialPort.setSerialPortParams(
                Integer.parseInt(baudComboBox.getSelectedItem().toString()),
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        } 
        catch (NumberFormatException | UnsupportedCommOperationException ex) {
            getLogger().log(Level.SEVERE, "Set serial port params", ex);
        }
    }
    
    
    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new TextFileFilter());
        if (defaultDirectory != null) {
            fileChooser.setCurrentDirectory(defaultDirectory);
        }
        return fileChooser;
        
    }


    private static Logger getLogger() {
        return Logger.getLogger(MonitorConsole.class.getName());
    }


    private class TextFileFilter extends javax.swing.filechooser.FileFilter {
        
        @Override
        public boolean accept(File file) {
            return file.isDirectory() || file.getName().endsWith(".txt");
        }
        
        @Override
        public String getDescription() {
            return "Text files";
        }
        
    }

    
    private class MonitorWindowAdapter extends WindowAdapter
    {

        @Override
        public void windowOpened(WindowEvent evt) {
            loadPlugins();
            for (Plugin plugin : plugins) {
                openPluginPanel(plugin.getView());
            }
        }

        private void openPluginPanel(JPanel panel) {
            if (panel != null) {
                JDialog dialog = new CustomDialog(MonitorConsole.this, panel.getName(), panel, false);
                Dimension d = panel.getPreferredSize();
                dialog.setPreferredSize(panel.getPreferredSize());
                dialog.getContentPane().add(panel);
                dialog.pack();
                dialog.setVisible(true);
            }
        }

        private void loadPlugins() {
            URL[] classUrls = null;
            JSONArray pluginPaths = getConfigurationArray("plugin-paths");
            if (pluginPaths != null) {
                classUrls = new URL[pluginPaths.length()];
                for (int i = 0; i < pluginPaths.length(); ++i) {
                    try {
                        classUrls[i] = new URL(pluginPaths.getString(i));
                    }
                    catch (JSONException | MalformedURLException ex) {
                        Logger.getLogger(MonitorConsole.class.getName()).log(Level.WARNING, "Configuration", ex);
                    }
                }
            }
            URLClassLoader ucl = new URLClassLoader(classUrls);
            JSONArray pluginClassNames = getConfigurationArray("plugins");
            if (pluginClassNames != null) {
                for (int i = 0; i < pluginClassNames.length(); ++i) {
                    try {
                        String name = pluginClassNames.getString(i);
                        Class pluginClass = ucl.loadClass(name);
                        Plugin plugin = (Plugin) pluginClass.newInstance();
                        plugins.add(plugin);
                    }
                    catch (JSONException | ReflectiveOperationException ex) {
                        Logger.getLogger(MonitorConsole.class.getName()).log(Level.WARNING, "Configuration", ex);
                    }
                }
            }
        }

        @Override
        public void windowClosing(WindowEvent evt) {
            for (Window window : getOwnedWindows()) {
                if (window instanceof AbstractDialog) {
                    window.dispose();
                }
            }
            AbstractDialog.store(MonitorConsole.this);
        }

    }


    private class ComponentItemListener implements ItemListener {
        
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                Object object = event.getSource();
                if (object == portComboBox) {
                    portComboBox_itemStateChanged(event);
                }
                else if (object == baudComboBox) {
                    baudComboBox_itemStateChanged(event);
                }
                sendText.requestFocus();
            }
        }
        
    }


    private class ComponentActionListener implements ActionListener {
        
        @Override
        public void actionPerformed(ActionEvent event) {
            Object object = event.getSource();
            if (object == openMenuItem) {
                openMenuItem_actionPerformed(event);
            }
            else if (object == exitMenuItem) {
                exitMenuItem_actionPerformed(event);
            }
            else if (object == sendButton) {
                sendButton_actionPerformed(event);
            }
            else if (object == sendText) {
                sendText_actionPerformed(event);
            }
            else if (object == saveMenuItem) {
                saveMenuItem_actionPerformed(event);
            }
            sendText.requestFocus();
        }
        
    }

    
    private class ScrollChangeListener implements ChangeListener {
        
        @Override
        public void stateChanged(ChangeEvent e) {
            // Test for flag. When scrolling unconditionally,
            // the scroll bar will get stuck at the bottom even when the
            // user tries to drag it. So only scroll when text was added.
            if (shouldScroll) {
                JScrollBar vertBar = receivePane.getVerticalScrollBar();
                vertBar.setValue(vertBar.getMaximum());
                shouldScroll = false;
            }
        }
        
    }

    
    private class CommPortListener implements SerialPortEventListener  {
        
        @Override
        public void serialEvent(SerialPortEvent evt) {
            if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                try {
                    int available = inputStream.available();
                    byte[] receiveBuffer = new byte[available];
                    inputStream.read(receiveBuffer, 0, receiveBuffer.length);
                    StringBuilder received = new StringBuilder();
                    if (byteJRadioButton.isSelected()) {
                        for (int i = 0; i < available; ++i) {
                            received.append(Byte.toString(receiveBuffer[i]));
                            received.append(' ');
                        }
                        received.append(CR);
                    }
                    else {
                        received.append(new String(receiveBuffer));
                    }
                    receivedText.append(received.toString());
                    if (receivedText.getText().length() > 50000) {
                        receivedText.replaceRange("", 0, 1000);
                    }
                    shouldScroll = true;
                    for (Plugin plugin : plugins) {
                        plugin.receive(receiveBuffer);
                    }
                }
                catch (IOException ex) {
                    getLogger().log(Level.WARNING, evt.toString(), ex);
                }
            }
        }

    }
    
    
    // Used for addNotify check.
    private boolean componentsAdjusted = false;

    private final JTextField sendText = new JTextField();
    private final JTextArea receivedText = new JTextArea();
    private final JButton sendButton = new JButton();
    private final JComboBox portComboBox = new JComboBox();
    private final JComboBox baudComboBox = new JComboBox();

    private final JMenuBar mainMenuBar = new JMenuBar();
    private final JMenu fileMenu = new JMenu();
    private final JMenuItem newMenuItem = new JMenuItem();
    private final JMenuItem openMenuItem = new JMenuItem();
    private final JMenuItem saveMenuItem = new JMenuItem();
    private final JMenuItem saveAsMenuItem = new JMenuItem();
    private final JMenuItem exitMenuItem = new JMenuItem();
    private final JMenu editMenu = new JMenu();
    private final JMenuItem cutMenuItem = new JMenuItem();
    private final JMenuItem copyMenuItem = new JMenuItem();
    private final JMenuItem pasteMenuItem = new JMenuItem();
    
    private File defaultDirectory;

    private JScrollPane receivePane;
    private JRadioButton textJRadioButton;
    private JRadioButton byteJRadioButton;
    
    private CommPortIdentifier openedPort;
    private SerialPort serialPort;
    private OutputStream outputStream;
    private InputStream inputStream;

    private final Map<String, CommPortIdentifier> tableOfPorts = new HashMap<>();
    private final Collection<Plugin> plugins = new ArrayList<>();

    private boolean shouldScroll = false;

    
    private static final String CR = "\n\r";

    private static final String PORT_PROPERTY_KEY = "port";
    private static final String BAUD_PROPERTY_KEY = "baud";
    
    private static final String NO_SELECTION = "-";
    
    private static final int[] DEFAULT_BAUD_RATES = { 115200, 19200, 9600, 4800 };

}