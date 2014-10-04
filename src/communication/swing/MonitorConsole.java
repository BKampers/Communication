package communication.swing;


import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.comm.*;


public class MonitorConsole extends JFrame implements SerialPortEventListener {
    
    public MonitorConsole() {
        getContentPane().setLayout(new BorderLayout());
        setBackground(java.awt.Color.white);
        setSize(600, 500);
        setVisible(false);
        openFileDialog1.setDialogType(JFileChooser.OPEN_DIALOG);
        JPanel sendPanel = new JPanel();
        sendText.setPreferredSize(new Dimension(430, 25));
        sendPanel.add(sendText);
        sendButton.setText("Send");
        sendButton.setPreferredSize(new Dimension(75, 25));
        sendPanel.add(sendButton);
        getContentPane().add(sendPanel, BorderLayout.SOUTH);
        receivePane = new JScrollPane();
        receivePane.setPreferredSize(new Dimension(430, 400));
        JScrollBar jScrollBar = receivePane.getVerticalScrollBar();
        jScrollBar.getModel().addChangeListener(new ScrollChangeListener());
        receivedText.setEditable(false);
        receivedText.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
        receivedText.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
        receivePane.getViewport().add(receivedText);
        getContentPane().add(receivePane, BorderLayout.CENTER);
        JPanel choicePanel = new JPanel();
        portChoice.setPreferredSize(new Dimension(75, 25));
        choicePanel.add(portChoice, BorderLayout.NORTH);
        baudChoice.setPreferredSize(new Dimension(75, 25));
        choicePanel.add(baudChoice);
        textJRadioButton = new JRadioButton("Text", true);
        byteJRadioButton = new JRadioButton("Byte", false);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(textJRadioButton);
        buttonGroup.add(byteJRadioButton);
        choicePanel.add(textJRadioButton);
        choicePanel.add(byteJRadioButton);
        getContentPane().add(choicePanel, BorderLayout.NORTH); 
        setTitle("COM Monitor");

        menu1.setText("File");
        menu1.add(newMenuItem);
        newMenuItem.setEnabled(false);
        newMenuItem.setText("New");
        newMenuItem.setAccelerator(
            KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_N, 
                java.awt.event.InputEvent.CTRL_MASK));
        menu1.add(openMenuItem);
        openMenuItem.setText("Open...");
        openMenuItem.setAccelerator(
            KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_O,
                java.awt.event.InputEvent.CTRL_MASK));
        menu1.add(saveMenuItem);
        saveMenuItem.setText("Save");
        saveMenuItem.setAccelerator(
            KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_S,
                java.awt.event.InputEvent.CTRL_MASK));
        menu1.add(saveAsMenuItem);
        saveAsMenuItem.setEnabled(false);
        saveAsMenuItem.setText("Save As...");
        menu1.add(separatorMenuItem);
        separatorMenuItem.setText("-");
        menu1.add(exitMenuItem);
        exitMenuItem.setText("Exit");
        mainMenuBar.add(menu1);
        menu2.setText("Edit");
        menu2.add(cutMenuItem);
        cutMenuItem.setEnabled(false);
        cutMenuItem.setText("Cut");
        cutMenuItem.setAccelerator(
            KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_X,
                java.awt.event.InputEvent.CTRL_MASK));
        menu2.add(copyMenuItem);
        copyMenuItem.setEnabled(false);
        copyMenuItem.setText("Copy");
        copyMenuItem.setAccelerator(
            KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_C,
                java.awt.event.InputEvent.CTRL_MASK));
        menu2.add(pasteMenuItem);
        pasteMenuItem.setEnabled(false);
        pasteMenuItem.setText("Paste");
        pasteMenuItem.setAccelerator(
            KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_V,
                java.awt.event.InputEvent.CTRL_MASK));
        mainMenuBar.add(menu2);

        setJMenuBar(mainMenuBar);

        SymWindow aSymWindow = new SymWindow();
        this.addWindowListener(aSymWindow);
        SymAction lSymAction = new SymAction();
        openMenuItem.addActionListener(lSymAction);
        exitMenuItem.addActionListener(lSymAction);
        SymItem lSymItem = new SymItem();
        portChoice.addItemListener(lSymItem);
        sendButton.addActionListener(lSymAction);
        sendText.addActionListener(lSymAction);
        saveMenuItem.addActionListener(lSymAction);
        baudChoice.addItemListener(lSymItem);
        textJRadioButton.addItemListener(lSymItem);
        byteJRadioButton.addItemListener(lSymItem);

        findPorts();
        baudChoice.addItem("115200");
        baudChoice.addItem( "57600");
        baudChoice.addItem( "38400");
        baudChoice.addItem( "19200");
        baudChoice.addItem(  "9600");
        baudChoice.addItem(  "4800");
        baudChoice.addItem(  "2400");
        baudChoice.addItem(  "1200");
        
        pack();
    }

    
    public MonitorConsole(String title) {
        this();
        setTitle(title);
    }


    static public void main(String args[]) {
        String classPath = System.getProperty("java.class.path");
//        classPath += ";comm.jar"; // For running from jar.
        classPath += ";.\\lib\\comm.jar";
        System.setProperty("java.class.path", classPath);
        try {
            //Create a new instance of our application's frame, and make it visible.
            (new MonitorConsole()).setVisible(true);
        }
        catch (Throwable t) {
            System.err.println(t);
            t.printStackTrace();
            //Ensure the application exits with an error condition.
            System.exit(1);
        }
    }

    
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


    public void setVisible(boolean visible) {
        if (visible) {
            setLocation(50, 50);
        }	
        super.setVisible(visible);
    }


    public void addNotify() {
        // Record the size of the window prior to calling parents addNotify.
        Dimension d = getSize();

        super.addNotify();

        if (fComponentsAdjusted) {
            return;
        }

        // Adjust components according to the insets
        setSize(getInsets().left + getInsets().right + d.width, getInsets().top + getInsets().bottom + d.height);
        Component components[] = getComponents();
        for (int i = 0; i < components.length; i++) {
            Point p = components[i].getLocation();
            p.translate(getInsets().left, getInsets().top);
            components[i].setLocation(p);
        }
        fComponentsAdjusted = true;
    }

    
    class SymWindow extends java.awt.event.WindowAdapter
    {
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
        try {
            // OpenFileDialog Create and show as modal
            openFileDialog1 = new JFileChooser();
            openFileDialog1.setFileFilter(new TextFileFilter());
            openFileDialog1.setVisible(true);
            File file = openFileDialog1.getSelectedFile();
            if (file.exists()) {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                String text;
                 do {
                    text = randomAccessFile.readLine();
                    if (text != null) {
                        transmitText(text);
                    }
                } while (text != null);
                randomAccessFile.close();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
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
        portChoice.addItem("-");
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
            String name = portId.getName();
            if (name.substring(0, 3).equals("COM")) {
                tableOfPorts.put(name, portId);
                portChoice.addItem(name);
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
            try {
                // OpenFileDialog Create and show as modal
                File defDirectory = openFileDialog1.getCurrentDirectory();
                openFileDialog1 = new JFileChooser();
                openFileDialog1.setDialogType(JFileChooser.SAVE_DIALOG);
                openFileDialog1.setCurrentDirectory(defDirectory);
                openFileDialog1.setFileFilter(new TextFileFilter());
                openFileDialog1.setVisible(true);
                File file = openFileDialog1.getSelectedFile();
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.writeBytes(receivedText.getText());
                randomAccessFile.close();
            } catch (Exception e) {
                System.out.println(e);
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
                Integer.parseInt(baudChoice.getSelectedItem().toString()),
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        } 
        catch (Exception e) {
            System.out.println(e);
        }
    }


    private class TextFileFilter extends javax.swing.filechooser.FileFilter {
        public boolean accept(File file) {
            return file.getName().endsWith(".txt");
        }
        public String getDescription() {
            return "Text files";
        }
    }

    
    private class SymItem implements java.awt.event.ItemListener {
        public void itemStateChanged(java.awt.event.ItemEvent event) {
            Object object = event.getSource();
            if (object == portChoice) {
                portChoice_ItemStateChanged(event);
            }
            else if (object == baudChoice) {
                baudChoice_ItemStateChanged(event);
            }
            sendText.requestFocus();
        }
    }


    class SymAction implements java.awt.event.ActionListener {
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
    boolean fComponentsAdjusted = false;

    JFileChooser openFileDialog1 = new JFileChooser();
    JTextField sendText = new JTextField();
    JTextArea receivedText = new JTextArea();
    JButton sendButton = new JButton();
    JComboBox portChoice = new JComboBox();
    JComboBox baudChoice = new JComboBox();

    JMenuBar mainMenuBar = new JMenuBar();
    JMenu menu1 = new JMenu();
    JMenuItem newMenuItem = new JMenuItem();
    JMenuItem openMenuItem = new JMenuItem();
    JMenuItem saveMenuItem = new JMenuItem();
    JMenuItem saveAsMenuItem = new JMenuItem();
    JMenuItem separatorMenuItem = new JMenuItem();
    JMenuItem exitMenuItem = new JMenuItem();
    JMenu menu2 = new JMenu();
    JMenuItem cutMenuItem = new JMenuItem();
    JMenuItem copyMenuItem = new JMenuItem();
    JMenuItem pasteMenuItem = new JMenuItem();
    JMenu menu3 = new JMenu();

    private JScrollPane receivePane;
    private JRadioButton textJRadioButton;
    private JRadioButton byteJRadioButton;
    
    private Hashtable tableOfPorts = new Hashtable();
    private CommPortIdentifier openedPort = null;

    private SerialPort serialPort;
    private OutputStream outputStream;
    private InputStream inputStream;


    private boolean shouldScroll = false;
    
    
    private static final String CR = "\n\r";

}

