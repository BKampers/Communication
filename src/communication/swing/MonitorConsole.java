package communication.swing;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import gnu.io.*;
import java.util.logging.*;


public class MonitorConsole extends JFrame implements SerialPortEventListener {
    
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

        SymWindow aSymWindow = new SymWindow();
        this.addWindowListener(aSymWindow);
        SymAction lSymAction = new SymAction();
        openMenuItem.addActionListener(lSymAction);
        exitMenuItem.addActionListener(lSymAction);
        SymItem lSymItem = new SymItem();
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
//        String classPath = System.getProperty("java.class.path");
//        classPath += ";comm.jar"; // For running from jar.
//        classPath += ";.\\lib\\comm.jar";
//        System.setProperty("java.class.path", classPath);
        try {
            (new MonitorConsole()).setVisible(true);
        }
        catch (Throwable t) {
            Logger.getLogger(MonitorConsole.class.getName()).log(Level.SEVERE, "main", t);
            System.exit(1);
        }
    }

    
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

    
    class SymWindow extends java.awt.event.WindowAdapter
    {
        @Override
        public void windowClosing(java.awt.event.WindowEvent event) {
            Object object = event.getSource();
            if (object == MonitorConsole.this) {
                System.exit(0);
            }
        }
    }


    void openMenuItem_ActionPerformed(java.awt.event.ActionEvent event) {
        openMenuItem_ActionPerformed_Interaction1(event);
    }


    void openMenuItem_ActionPerformed_Interaction1(java.awt.event.ActionEvent event) {
       // try {
            // OpenFileDialog Create and show as modal
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new TextFileFilter());
            fileChooser.setVisible(true);
            File file = fileChooser.getSelectedFile();
            if (file.exists()) {
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                    String text;
                    do {
                        text = randomAccessFile.readLine();
                        if (text != null) {
                            transmitText(text);
                        }
                    } while (text != null);
                }
                catch (IOException ex) {
                    Logger.getLogger(MonitorConsole.class.getName()).log(Level.SEVERE, "openMenuItem_ActionPerformed", ex);
                }
            }
//        } catch (Exception e) {
//            System.out.println(e);
//        }
    }


    void exitMenuItem_ActionPerformed(java.awt.event.ActionEvent event)
    {
        System.exit(0);
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
                serialPort.addEventListener(this);
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


    void portChoice_ItemStateChanged(java.awt.event.ItemEvent event) {
        String selected = (String) event.getItem();
        if (event.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            if (! selected.equals("-")) {
                openSerialPort((CommPortIdentifier) tableOfPorts.get(selected));
            }
            else {
                openSerialPort(null);
            }
        }
    }


    void transmitText(String text) {
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
                text += '\15';
                outputStream.write(text.getBytes());
                sendText.setText("");
            }
            catch (Throwable t) {
                System.out.println(t);
                JOptionPane.showMessageDialog(this, t.toString(), "Port error", JOptionPane.ERROR_MESSAGE); 
            }
        }
    }


    void sendButton_ActionPerformed(java.awt.event.ActionEvent event) {
        transmitText(sendText.getText());
    }


    void sendText_ActionPerformed(java.awt.event.ActionEvent event)
    {
        transmitText(sendText.getText());
    }

    void saveMenuItem_ActionPerformed(java.awt.event.ActionEvent event)
    {
        // OpenFileDialog Create and show as modal
        JFileChooser fileChooser = new JFileChooser();
        File defDirectory = fileChooser.getCurrentDirectory();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setCurrentDirectory(defDirectory);
        fileChooser.setFileFilter(new TextFileFilter());
        fileChooser.setVisible(true);
        File file = fileChooser.getSelectedFile();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            randomAccessFile.writeBytes(receivedText.getText());
            randomAccessFile.close();
        }
        catch (Exception ex) {
            Logger.getLogger(MonitorConsole.class.getName()).log(Level.SEVERE, "saveMenuItem_ActionPerformed", ex);
        }
    }


    void baudChoice_ItemStateChanged(java.awt.event.ItemEvent event)
    {
        if (serialPort != null) {
            setSerialPortParams();
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


    private class TextFileFilter extends javax.swing.filechooser.FileFilter {
        
        @Override
        public boolean accept(File file) {
            return file.getName().endsWith(".txt");
        }
        
        @Override
        public String getDescription() {
            return "Text files";
        }
        
    }

    
    private class SymItem implements java.awt.event.ItemListener {
        
        @Override
        public void itemStateChanged(java.awt.event.ItemEvent event) {
            Object object = event.getSource();
            if (object == portComboBox) {
                portChoice_ItemStateChanged(event);
            }
            else if (object == baudComboBox) {
                baudChoice_ItemStateChanged(event);
            }
            sendText.requestFocus();
        }
        
    }


    class SymAction implements java.awt.event.ActionListener {
        
        @Override
        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (object == openMenuItem) {
                openMenuItem_ActionPerformed(event);
            }
            else if (object == exitMenuItem) {
                exitMenuItem_ActionPerformed(event);
            }
            else if (object == sendButton) {
                sendButton_ActionPerformed(event);
            }
            else if (object == sendText) {
                sendText_ActionPerformed(event);
            }
            else if (object == saveMenuItem) {
                saveMenuItem_ActionPerformed(event);
            }
            sendText.requestFocus();
        }
        
    }

    
    private class ScrollChangeListener implements javax.swing.event.ChangeListener {
        
        @Override
        public void stateChanged(javax.swing.event.ChangeEvent e) {
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

    
    // Used for addNotify check.
    boolean componentsAdjusted = false;

    JTextField sendText = new JTextField();
    JTextArea receivedText = new JTextArea();
    JButton sendButton = new JButton();
    JComboBox portComboBox = new JComboBox();
    JComboBox baudComboBox = new JComboBox();

    JMenuBar mainMenuBar = new JMenuBar();
    JMenu fileMenu = new JMenu();
    JMenuItem newMenuItem = new JMenuItem();
    JMenuItem openMenuItem = new JMenuItem();
    JMenuItem saveMenuItem = new JMenuItem();
    JMenuItem saveAsMenuItem = new JMenuItem();
    JMenuItem separatorMenuItem = new JMenuItem();
    JMenuItem exitMenuItem = new JMenuItem();
    JMenu editMenu = new JMenu();
    JMenuItem cutMenuItem = new JMenuItem();
    JMenuItem copyMenuItem = new JMenuItem();
    JMenuItem pasteMenuItem = new JMenuItem();

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

