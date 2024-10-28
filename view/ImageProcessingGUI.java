package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageProcessingGUI extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    private int[][][] imageMatrix;
    private ArrayList<Integer> interestedPoints = new ArrayList<Integer>();
    private ArrayList<Integer> uninterestedPoints = new ArrayList<Integer>();

    private JLabel imageLabel;
    private JSpinner widthSpinner;
    private JSpinner heightSpinner;
    private JPanel textFieldPanel;
    private JPanel buttonPanel;
    private JLabel imageSizeLabel;
    private JLabel statusLabel;

    private boolean areaSelected = false;
    boolean buttonPressed = false; // 是否开始处理
    boolean InterestedArea = false; // 是否选了感兴趣区域
    boolean UninterestedArea = false; // 是否选了不感兴趣区域

    double ratio; // 图像缩放比例
    private int scaledWidth; // 缩放后的大小
    private int scaledHeight;
    private int imageWidth; // 图像大小
    private int imageHeight;
    private final int labelWidth; // 页面大小
    private final int labelHeight;

    private boolean[][] interested;
    private boolean[][] uninterested;
    private BufferedImage overlayImage;

    private final int sizeOfRegion = 8;

    public ImageProcessingGUI() {

        // 创建界面
        setTitle("Image Processing");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

        // 创建显示图像
        imageLabel = new JLabel();
        mainPanel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);
        setVisible(true);

        // 创建按钮面板和按钮（感兴趣区域、不感兴趣区域）
        buttonPanel = new JPanel();
        JButton selectInterestedAreaButton = new JButton("Protect");
        selectInterestedAreaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!areaSelected) {
                    selectInterestedArea();
                    areaSelected = true;
                    selectInterestedAreaButton.setEnabled(false);
                    InterestedArea = true;
                }
            }
        });
        buttonPanel.add(selectInterestedAreaButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setVisible(true);
        JButton selectUninterestedAreaButton = new JButton("Erase");
        selectUninterestedAreaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!areaSelected) {
                    selectUninterestedArea();
                    areaSelected = true;
                    selectUninterestedAreaButton.setEnabled(false);
                    UninterestedArea = true;
                }
            }
        });
        buttonPanel.add(selectUninterestedAreaButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setVisible(true);

        // 创建文本框面板和文本框
        textFieldPanel = new JPanel();
        imageSizeLabel = new JLabel("Original Image Size: N/A");
        textFieldPanel.add(imageSizeLabel);
        JLabel widthLabel = new JLabel("Width: ");
        textFieldPanel.add(widthLabel);
        widthSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 5));
        widthSpinner.setPreferredSize(new Dimension(60, 25));
        textFieldPanel.add(widthSpinner);

        JLabel heightLabel = new JLabel("Height: ");
        textFieldPanel.add(heightLabel);
        heightSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 5));
        heightSpinner.setPreferredSize(new Dimension(60, 25));
        textFieldPanel.add(heightSpinner);

        statusLabel = new JLabel("  Status: Not Yet Started");
        textFieldPanel.add(statusLabel);
        mainPanel.add(textFieldPanel, BorderLayout.NORTH);

        heightSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (heightSpinnerHasFocus()) {
                    buttonPressed = true;
                    statusLabel.setText("  Status: Processing...");
                }
            }
        });
        widthSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (widthSpinnerHasFocus()) {
                    buttonPressed = true;
                    statusLabel.setText("  Status: Processing...");
                }
            }
        });

        // 创建处理按钮
        JButton processButton = new JButton("Process Image");
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonPressed = true; // 按钮被按下时设置标志为 true
                statusLabel.setText("  Status: Processing...");
                for (MouseListener listener : imageLabel.getMouseListeners()) {
                    imageLabel.removeMouseListener(listener);
                }
                for (MouseMotionListener listener : imageLabel.getMouseMotionListeners()) {
                    imageLabel.removeMouseMotionListener(listener);
                }
                Component[] components = imageLabel.getComponents();
                for (Component component : components) {
                    if (component instanceof JPanel) {
                        imageLabel.remove(component);
                    }
                }
                imageLabel.revalidate();
                imageLabel.repaint();
                if (originalImage == null) {
                    // 图像文件加载失败，显示错误消息
                    JOptionPane.showMessageDialog(ImageProcessingGUI.this, "Please upload the image.\nPlease restart the program.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(processButton);

        // 创建菜单
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openImage();
            }
        });
        fileMenu.add(openItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        setVisible(true);

        labelWidth = imageLabel.getWidth();
        labelHeight = imageLabel.getHeight();
    }

    private boolean widthSpinnerHasFocus() {
        Component editor = widthSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            return textField != null && textField.hasFocus();
        }
        return false;
    }

    private boolean heightSpinnerHasFocus() {
        Component editor = heightSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            return textField != null && textField.hasFocus();
        }
        return false;
    }

    private void openImage() {
        buttonPressed = false;
        InterestedArea = false;
        UninterestedArea = false;
        interestedPoints.clear();
        uninterestedPoints.clear();
        JFileChooser fileChooser = new JFileChooser();
        // 限制用户只能选择图像文件
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                originalImage = ImageIO.read(selectedFile);
                if (originalImage == null) {
                    // 图像文件加载失败，显示错误消息
                    JOptionPane.showMessageDialog(this, "Selected file is not a valid image file.\nPlease restart the program.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    // 图像加载成功，处理成RGB数组
                    imageWidth = originalImage.getWidth();
                    imageHeight = originalImage.getHeight();
                    imageMatrix = convertToRGBMatrix();
                    displayImage();
                    statusLabel.setText("  Status: Not Yet Started");
                    imageSizeLabel.setText("Original Image Size: " + originalImage.getWidth() + " x " + originalImage.getHeight());
                    overlayImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading image.\nPlease restart the program.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //原始
    public int[][][] convertToRGBMatrix() {
        BufferedImage image = originalImage;
        int width = image.getWidth();
        int height = image.getHeight();
        int[][][] matrix = new int[height][width][3]; // 3 channels for RGB
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                matrix[y][x][0] = color.getRed(); // Red channel
                matrix[y][x][1] = color.getGreen(); // Green channel
                matrix[y][x][2] = color.getBlue(); // Blue channel
            }
        }
        return matrix;
    }


    private void displayImage() {
        BufferedImage image = originalImage;
        // 自适应长宽
        double widthRatio = (double) labelWidth / image.getWidth();
        double heightRatio = (double) labelHeight / image.getHeight();
        ratio = Math.min(widthRatio, heightRatio);
        scaledWidth = (int) (imageWidth * ratio);
        scaledHeight = (int) (imageHeight * ratio);
        Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

        ImageIcon icon = new ImageIcon(scaledImage);
        imageLabel.setIcon(icon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.revalidate();
    }


    public ArrayList<Integer> selectInterestedArea() {
        // 移除之前的监听器
        for (MouseListener listener : imageLabel.getMouseListeners()) {
            imageLabel.removeMouseListener(listener);
        }
        for (MouseMotionListener listener : imageLabel.getMouseMotionListeners()) {
            imageLabel.removeMouseMotionListener(listener);
        }

        double widthRatio = (double) labelWidth / imageWidth;
        double heightRatio = (double) labelHeight / imageHeight;
        ratio = Math.min(widthRatio, heightRatio);
        scaledWidth = (int) (imageWidth * ratio);
        scaledHeight = (int) (imageHeight * ratio);

        // 创建存储空间
        interested = new boolean[originalImage.getWidth()][originalImage.getHeight()];

        JButton selectAreaButton1 = (JButton) buttonPanel.getComponent(0); // 获取第一个按钮（选择区域按钮）
        JButton selectAreaButton2 = (JButton) buttonPanel.getComponent(2); // 获取第三个按钮（处理按钮）

        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectAreaButton1.setEnabled(false);
                selectAreaButton2.setEnabled(false);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 鼠标释放时启用选择区域按钮并重置区域选择标志

                selectAreaButton1.setEnabled(true);
                areaSelected = false; // 重置区域选择状态为 false
                selectAreaButton2.setEnabled(true);
            }
        });

        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // 获取当前鼠标位置
                int x = e.getX();
                int y = e.getY();

                // 记录被涂的点并标记为感兴趣
                int offsetX = (labelWidth - scaledWidth) / 2;
                int offsetY = (labelHeight - scaledHeight) / 2;

                for (int i = x; i < x + sizeOfRegion; i++) {
                    for (int j = y; j < y + sizeOfRegion; j++) {
                        int originalX = (int) ((i - offsetX) / ratio);
                        int originalY = (int) ((j - offsetY) / ratio);
                        if (originalX >= 0 && originalX < imageWidth && originalY >= 0 && originalY < imageHeight) {
                            if (!interested[originalX][originalY]) {
                                interestedPoints.add(originalX);
                                interestedPoints.add(originalY);
                            }
                            interested[originalX][originalY] = true;
                            overlayImage.setRGB(originalX, originalY, new Color(255, 0, 0, 80).getRGB());
                        }
                    }
                }

                // 创建一个新的图像，将原始图像和覆盖层图像合并
                BufferedImage combinedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics g = combinedImage.getGraphics();
                g.drawImage(originalImage, 0, 0, null);
                g.drawImage(overlayImage, 0, 0, null);
                g.dispose();

                // 缩放合并后的图像以适应JLabel的大小
                Image scaledImage = combinedImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);
                imageLabel.setIcon(icon);
                imageLabel.revalidate();
                imageLabel.repaint();
            }
        });
        return interestedPoints;
    }


    public ArrayList<Integer> selectUninterestedArea() {
        // 移除之前的监听器
        for (MouseListener listener : imageLabel.getMouseListeners()) {
            imageLabel.removeMouseListener(listener);
        }
        for (MouseMotionListener listener : imageLabel.getMouseMotionListeners()) {
            imageLabel.removeMouseMotionListener(listener);
        }

        double widthRatio = (double) labelWidth / imageWidth;
        double heightRatio = (double) labelHeight / imageHeight;
        ratio = Math.min(widthRatio, heightRatio);
        scaledWidth = (int) (imageWidth * ratio);
        scaledHeight = (int) (imageHeight * ratio);

        // 创建存储空间
        uninterested = new boolean[originalImage.getWidth()][originalImage.getHeight()];


        JButton selectAreaButton1 = (JButton) buttonPanel.getComponent(1); // 获取第二个按钮（选择区域按钮）
        JButton selectAreaButton2 = (JButton) buttonPanel.getComponent(2); // 获取第三个按钮（处理按钮）

        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectAreaButton1.setEnabled(false);
                selectAreaButton2.setEnabled(false);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 鼠标释放时启用选择区域按钮并重置区域选择标志

                selectAreaButton1.setEnabled(true);
                areaSelected = false; // 重置区域选择状态为 false
                selectAreaButton2.setEnabled(true);
            }
        });

        imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // 获取当前鼠标位置
                int x = e.getX();
                int y = e.getY();

                // 记录被涂的点并标记为感兴趣
                int offsetX = (labelWidth - scaledWidth) / 2;
                int offsetY = (labelHeight - scaledHeight) / 2;

                for (int i = x; i < x + sizeOfRegion; i++) {
                    for (int j = y; j < y + sizeOfRegion; j++) {
                        int originalX = (int) ((i - offsetX) / ratio);
                        int originalY = (int) ((j - offsetY) / ratio);
                        if (originalX >= 0 && originalX < imageWidth && originalY >= 0 && originalY < imageHeight) {
                            if (!uninterested[originalX][originalY]) {
                                uninterestedPoints.add(originalX);
                                uninterestedPoints.add(originalY);
                            }
                            uninterested[originalX][originalY] = true;
                            overlayImage.setRGB(originalX, originalY, new Color(0, 255, 0, 80).getRGB());
                        }
                    }
                }

                // 创建一个新的图像，将原始图像和覆盖层图像合并
                BufferedImage combinedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics g = combinedImage.getGraphics();
                g.drawImage(originalImage, 0, 0, null);
                g.drawImage(overlayImage, 0, 0, null);
                g.dispose();

                // 缩放合并后的图像以适应JLabel的大小
                Image scaledImage = combinedImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaledImage);
                imageLabel.setIcon(icon);
                imageLabel.revalidate();
                imageLabel.repaint();
            }
        });
        return uninterestedPoints;
    }

    public int[] expectedSize() {
        int width = 0;
        int height = 0;
        try {
            width = (int) widthSpinner.getValue();
            height = (int) heightSpinner.getValue();
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Width and height must be positive.\nPlease restart the program.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid width and height.\nPlease restart the program.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return new int[]{width, height};
    }

    private void convertToBufferedImage(int[][][] processedImageMatrix) {
        int height = processedImageMatrix.length;
        int width = processedImageMatrix[0].length;
        processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int red = processedImageMatrix[y][x][0];
                int green = processedImageMatrix[y][x][1];
                int blue = processedImageMatrix[y][x][2];
                int rgb = (red << 16) | (green << 8) | blue; // Combine RGB components into a single int
                processedImage.setRGB(x, y, rgb); // Set pixel color in BufferedImage
            }
        }
    }

    public void displayProcessedImage(int[][][] processedImageMatrix) {
        imageLabel.setIcon(null);

        convertToBufferedImage(processedImageMatrix);
        imageWidth = processedImage.getWidth();
        imageHeight = processedImage.getHeight();
        // 自适应长宽
        double widthRatio = (double) labelWidth / imageWidth;
        double heightRatio = (double) labelHeight / imageHeight;
        ratio = Math.min(widthRatio, heightRatio);
        int scaledWidth = (int) (imageWidth * ratio);
        int scaledHeight = (int) (imageHeight * ratio);
        Image scaledImage = processedImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

        ImageIcon icon = new ImageIcon(scaledImage);
        imageLabel.setIcon(icon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.revalidate();

        statusLabel.setText("  Status:  Finished");
        imageSizeLabel.setText("Current Image Size: " + imageWidth + " x " + imageHeight);
        textFieldPanel.revalidate();

        // 清除上次操作
        buttonPressed = false;
        InterestedArea = false;
        UninterestedArea = false;
        originalImage = processedImage;
        interestedPoints.clear();
        uninterestedPoints.clear();
        overlayImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

    }

    public boolean startProcessing() {
        return buttonPressed;
    }

    public boolean isInterestedArea() {
        return InterestedArea;
    }

    public boolean isUninterestedArea() {
        return UninterestedArea;
    }

    public static void main(String[] args) {
        new ImageProcessingGUI();
    }
}
