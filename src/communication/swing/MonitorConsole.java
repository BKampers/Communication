package communication.swing;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import gnu.io.*;
import java.util.logging.*;


public class MonitorConsole extends JFrame {
    
    public MonitorConsole() {
        getContentPane().setLayout(new BorderLayout());
        setBackground(java.awt.Color.white);
        setSize(600, 500);
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
        portComboBox.setPreferredSize(new Dimension(75, 25));
        choicePanel.add(portComboBox, BorderLayout.NORTH);
        baudComboBox.setPreferredSize(new Dimension(75, 25));
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
        fileMenu.add(separatorMenuItem);
        separatorMenuItem.setText("-");
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

        MonitorWindowAdapter aSymWindow = new MonitorWindowAdapter();
        this.addWindowListener(aSymWindow);
        ComponentActionListener lSymAction = new ComponentActionListener();
        openMenuItem.addActionListener(lSymAction);
        exitMenuItem.addActionListener(lSymAction);
        ComboBoxItemListener lSymItem = new ComboBoxItemListener();
        portComboBox.addItemListener(lSymItem);
        sendButton.addActionListener(lSymAction);
        sendText.addActionListener(lSymAction);
        saveMenuItem.addActionListener(lSymAction);
        baudComboBox.addItemListener(lSymItem);
        textJRadioButton.addItemListener(lSymItem);
        byteJRadioButton.addItemListener(lSymItem);

        findPorts();
        baudComboBox.addItem("115200");
        baudComboBox.addItem("57600");
        baudComboBox.addItem("38400");
        baudComboBox.addItem("19200");
        baudComboBox.addItem("9600");
        baudComboBox.addItem("4800");
        baudComboBox.addItem("2400");
        baudComboBox.addItem("1200");
        
        pack();
    }

    
    public MonitorConsole(String title) {
        this();
        setTitle(title);
    }


    static public void main(String args[]) {
        try {
            (new MonitorConsole()).setVisible(true);
        }
        catch (Throwable t) {
            Logger.getLogger(MonitorConsole.class.getName()).log(Level.SEVERE, "main", t);
            System.exit(1);
        }
    }

    
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setLocation(50, 50);
        }	
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


    private void exitMenuItem_actionPerformed(ActionEvent event) {
        System.exit(0);
    }


    private void portComboBox_itemStateChanged(ItemEvent event) {
        String selected = (String) event.getItem();
        if (event.getStateChange() == ItemEvent.SELECTED) {
            if (! selected.equals("-")) {
                openSerialPort((CommPortIdentifier) tableOfPorts.get(selected));
            }
            else {
                openSerialPort(null);
            }
        }
    }


    private void baudComboBox_itemStateChanged(ItemEvent event) {
        if (serialPort != null) {
            setSerialPortParams();
        }
    }


    private void sendButton_actionPerformed(ActionEvent event) {
        transmitText(sendText.getText());
    }


    private void sendText_actionPerformed(ActionEvent event) {
        transmitText(sendText.getText());
    }

    
    private void openSerialPort(CommPortIdentifier commPortIdentifier) {
        if (openedPort != null) {
            serialPort.close();
        }
        if (commPortIdentifier != null) {
            try {
                serialPort = (SerialPort) commPortIdentifier.open("MonitorConsole", 2000);
            } 
            catch (PortInUseException e) {
                serialPort = null;
                System.out.println(e);
            }
            if (serialPort != null) {
                try {
                    outputStream = serialPort.getOutputStream();
                    inputStream = serialPort.getInputStream();
                } 
                catch (IOException e) {
                    System.out.println(e);
                }
                setSerialPortParams();
            }
            serialPort.notifyOnDataAvailable(true);
            try {
                serialPort.addEventListener(new CommPortListener());
            }
            catch (TooManyListenersException e) {
                    System.out.println(e);
            }
        }
        openedPort = commPortIdentifier;
    }


    private void findPorts() {
        portComboBox.addItem("-");
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
            String name = portId.getName();
            if (name.substring(0, 3).equals("COM")) {
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
                catch (NoSuchElementException e) {
                    ready = true;
                }
                catch (IOException e) {
                    JOptionPane.showMessageDialog(this, e.toString(), "Port error", JOptionPane.ERROR_MESSAGE);
                    ready = true;
                }
            }
        }
        else {
            try {
                text += CR;
                outputStream.write(text.getBytes());
                sendText.setText("");
            }
            catch (Throwable t) {
                System.out.println(t);
                JOptionPane.showMessageDialog(this, t.toString(), "Port error", JOptionPane.ERROR_MESSAGE); 
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
            Logger.getLogger(MonitorConsole.class.getName()).log(Level.SEVERE, "setSerialPortParams", ex);
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
        public void windowClosing(java.awt.event.WindowEvent event) {
            Object object = event.getSource();
            if (object == MonitorConsole.this) {
                System.exit(0);
            }
        }
    }


    private class ComboBoxItemListener implements ItemListener {
        
        @Override
        public void itemStateChanged(ItemEvent event) {
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
            // Test for flag. Otherwise, if we scroll unconditionally, 
            // the scroll bar will be stuck at the bottom even when the 
            // user tries to drag it. So we only scroll when we know 
            // we've added text
            if (shouldScroll) {
                JScrollBar vertBar = receivePane.getVerticalScrollBar();
                vertBar.setValue(vertBar.getMaximum());
                shouldScroll = false;
            }
        }
        
    }

    
    private class CommPortListener implements SerialPortEventListener  {
        
        @Override
        public void serialEvent(SerialPortEvent ev) {
            if (ev.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                try {
                    int available = inputStream.available();
                    byte[] receiveBuffer = new byte[available];
                    inputStream.read(receiveBuffer, 0, receiveBuffer.length);
                    String received;
                    if (byteJRadioButton.isSelected()) {
                        received = "";
                        for (int i = 0; i < available; i++) {
                            received += Byte.toString(receiveBuffer[i]) + ' ';
                        }
                        received += CR;
                    }
                    else {
                        received = new String(receiveBuffer);
                    }
                    receivedText.append(received);
                    if (receivedText.getText().length() > 50000) {
                        receivedText.replaceRange("", 0, 1000);
                    }
                    shouldScroll = true;
                }
                catch (IOException e) {
                    System.out.println(e);
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
    private final JMenuItem separatorMenuItem = new JMenuItem();
    private final JMenuItem exitMenuItem = new JMenuItem();
    private final JMenu editMenu = new JMenu();
    private final JMenuItem cutMenuItem = new JMenuItem();
    private final JMenuItem copyMenuItem = new JMenuItem();
    private final JMenuItem pasteMenuItem = new JMenuItem();
    
    private File defaultDirectory;

    private JScrollPane receivePane;
    private JRadioButton textJRadioButton;
    private JRadioButton byteJRadioButton;
    
    private CommPortIdentifier openedPort = null;
    private SerialPort serialPort;
    private OutputStream outputStream;
    private InputStream inputStream;

    private final Map<String, CommPortIdentifier> tableOfPorts = new HashMap<>();

    private boolean shouldScroll = false;
    
    
    private static final String CR = "\n\r";

}

