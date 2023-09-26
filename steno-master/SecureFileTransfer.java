import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;
import javax.swing.filechooser.*;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import java.io.File;
// import java.io.FileInputStream;
import javax.imageio.ImageIO;
import java.nio.ByteBuffer;
// import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;

class GuiSecureFileTransfer extends JFrame implements ActionListener {
	JButton open;
	JButton send;
	Socket socket_var;
	DataOutputStream outputStream;
	BufferedImage sourceImage = null;
	BufferedImage embeddedImage = null;
	JTextField plain_text;
	JLabel sourceImage_label;
	JLabel l;
	String path;
	JLabel embeddedImage_label;
	JButton embed;

	public GuiSecureFileTransfer() {
		try {
			socket_var = new Socket(InetAddress.getByName("192.168.140.211"), 6666);
			outputStream = new DataOutputStream(socket_var.getOutputStream());
		} catch (Exception ex) {
			System.out.println(ex);
		}
		open = new JButton("Open");
		send = new JButton("Send");
		embed = new JButton("Embed");
		l = new JLabel();
		sourceImage_label = new JLabel();
		sourceImage_label.setBounds(50, 10, 50, 50);
		embeddedImage_label = new JLabel();
		embeddedImage_label.setBounds(50, 10, 10, 10);
		plain_text = new JTextField(10);
		send.addActionListener(this);
		open.addActionListener(this);
		embed.addActionListener(this);
		add(plain_text);
		add(open);
		add(send);
		add(embed);
		add(l);
		add(sourceImage_label);
		add(embeddedImage_label);
		setLayout(new FlowLayout());
		setVisible(true);
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent ae) {
		JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		if (ae.getSource() == open) {
			int response = fc.showOpenDialog(null);
			if (response == JFileChooser.APPROVE_OPTION) {
				l.setText(fc.getSelectedFile().getAbsolutePath());
				path = fc.getSelectedFile().getAbsolutePath();
			} else {
				l.setText("the user cancelled the operation");
			}
			try {
				sourceImage = ImageIO.read(new File(path));
				sourceImage_label.setIcon(new ImageIcon(sourceImage));
			} catch (Exception exce) {
				System.out.println("Exception");
			}
		}
		if (ae.getSource() == embed) {
			String mess = plain_text.getText();
			if (mess.equals("") || sourceImage.getHeight() == 0) {
				JOptionPane.showMessageDialog(this, "Please Select Image and Enter the Text First!");
				return;
			}
			embeddedImage = sourceImage.getSubimage(0, 0, sourceImage.getWidth(), sourceImage.getHeight());
			embeddedImage = embedMessage(embeddedImage, mess);
			embeddedImage_label.setIcon(new ImageIcon(embeddedImage));
			System.out.println(embeddedImage.getHeight());
			this.validate();
			try {
				ImageIO.write(embeddedImage, "png", new File("c://rk//facebook_logo.png"));
			} catch (Exception ex) {
				System.out.println("Exception");
			}
		}
		if (ae.getSource() == send) {
			try {
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ImageIO.write(embeddedImage, "png", byteArrayOutputStream);
				byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
				outputStream.write(size);
				outputStream.write(byteArrayOutputStream.toByteArray());
				outputStream.flush();
			} catch (Exception ex) {
				System.out.println(ex);
			}
		}
	}

	private BufferedImage embedMessage(BufferedImage img, String mess) {
		int messageLength = mess.length();
		int imageWidth = img.getWidth(), imageHeight = img.getHeight(), imageSize = imageWidth * imageHeight;
		if (messageLength * 8 + 32 > imageSize) {
			JOptionPane.showMessageDialog(this, "Message is too long for the chosen image", "Message too long!",
					JOptionPane.ERROR_MESSAGE);
			return img;
		}
		DWT2 obj = new DWT2();
        return obj.embedText(img, mess);
	}

}

public class SecureFileTransfer {
	public static void main(String[] args) throws Exception {
		GuiSecureFileTransfer gft = new GuiSecureFileTransfer();
	}
}


class DWT2 {
    // Embeds the given text in the given image and returns the modified image
    public BufferedImage embedText(BufferedImage image, String text) {
        // Perform the DWT on the input image
        int[][] dwt = performDWT(image);

        // Embed the text in the d1, d2, d3, and d4 coefficients
        dwt = embedTextInCoefficients(dwt, text);

        // Return the modified image
        BufferedImage t = inverseDWT(dwt);
        return t;
    }

    // Extract the text from the given image using DWT steganography
    public String extractText(BufferedImage image) {
        // Perform the DWT on the image to obtain the d1, d2, d3, and d4 coefficients
        int[][] dwt = performDWTExtract(image);

        // Extract the text from the d1, d2, d3, and d4 coefficients
        String text = extractTextFromCoefficients(dwt);

        System.out.println(text);

        // Return the extracted text
        return text;
    }

    // Performs the DWT on the given image and returns the d1, d2, d3, and d4 coefficients
    public int[][] performDWT(BufferedImage image) {
        // Get the width and height of the image
        int width = image.getWidth();
        int height = image.getHeight();

        // Initialize the DWT array
        int[][] dwt = new int[height][width];

        // Loop over the image and perform the DWT
        for (int i = 0; i < height - 1; i+=2) {
            for (int j = 0; j < width - 1; j+=2) {
                // Get the RGB value of the current pixel
                int p1 = (int)Math.floor(image.getRGB(j, i)/3.0);
                int p2 = (int)Math.floor(image.getRGB(j + 1, i)/3.0);
                int p3 = (int)Math.floor(image.getRGB(j, i + 1)/3.0);
                int p4 = (int)Math.floor(image.getRGB(j + 1, i + 1)/3.0);

                int d1 = p1 + p3 + p4 - 2*p2;
                int d2 = p2 + p4 + p1 - 2*p3;
                int d3 = p3 + p1 + p2 - 2*p4;
                int d4 = p4 + p2 + p3 - 2*p1;

                // Store the d1, d2, d3, and d4 coefficients in the DWT array
                dwt[i][j] = d1;
                dwt[i][j + 1] = d2;
                dwt[i + 1][j] = d3;
                dwt[i + 1][j + 1] = d4;
            }
        }

        // Return the DWT array
        return dwt;
    }

    // Perform the inverse DWT on the given dwt array
    public BufferedImage inverseDWT(int[][] dwt) {
        // Get the width and height of the dwt array
        int width = dwt[0].length;
        int height = dwt.length;

        // Create a new image with the same dimensions as the dwt array
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Loop over the dwt array and perform the inverse DWT
        for (int i = 0; i < height - 1; i += 2) {
            for (int j = 0; j < width - 1; j += 2) {
                int d1 = dwt[i][j];
                int d2 = dwt[i][j + 1];
                int d3 = dwt[i + 1][j];
                int d4 = dwt[i + 1][j + 1];
                
                int p1 = (d1 + d2 + d3) / 3;
                int p2 = (d2 + d3 + d4) / 3;
                int p3 = (d3 + d4 + d1) / 3;
                int p4 = (d4 + d1 + d2) / 3;
                
                p1=normalize(p1, d1+d2+d3);
                p2=normalize(p2, d2+d3+d4);
                p3=normalize(p3, d3+d4+d1);
                p4=normalize(p4, d4+d1+d2);

                // Set the RGB values of the current pixels in the image
                image.setRGB(j, i, p1);
                image.setRGB(j + 1, i, p2);
                image.setRGB(j, i + 1, p3);
                image.setRGB(j + 1, i + 1, p4);
            }
        }

        // Return the reconstructed image
        return image;
    }

    public int normalize(int value, int x){
        value = -value;
        x=-x;
        value=value*3+x%3;
        return -1*value;
    }

    public double denormalize(int value){
        value=-value;
        int d=value%3;
        value/=3;
        double ans=value+d/3.0;
        return -ans;
    }

    public int[][] performDWTExtract(BufferedImage image) {
        // Get the width and height of the image
        int width = image.getWidth();
        int height = image.getHeight();

        // Initialize the DWT array
        int[][] dwt = new int[height][width];

        // Loop over the image and perform the DWT
        for (int i = 0; i < height - 1; i+=2) {
            for (int j = 0; j < width - 1; j+=2) {
                // Get the RGB value of the current pixel
                int px1 = image.getRGB(j, i);
                int px2 = image.getRGB(j + 1, i);
                int px3 = image.getRGB(j, i + 1);
                int px4 = image.getRGB(j + 1, i + 1);

                double p1=denormalize(px1);
                double p2=denormalize(px2);
                double p3=denormalize(px3);
                double p4=denormalize(px4);

                int d1 = (int)Math.floor(p1 + p3 + p4 - 2*p2);
                int d2 = (int)Math.floor(p2 + p4 + p1 - 2*p3);
                int d3 = (int)Math.floor(p3 + p1 + p2 - 2*p4);
                int d4 = (int)Math.floor(p4 + p2 + p3 - 2*p1);

                // Store the d1, d2, d3, and d4 coefficients in the DWT array
                dwt[i][j] = d1;
                dwt[i][j + 1] = d2;
                dwt[i + 1][j] = d3;
                dwt[i + 1][j + 1] = d4;
            }
        }

        // Return the DWT array
        return dwt;
    }

    // Embed the given text in the given d1, d2, d3, and d4 coefficients
    public int[][] embedTextInCoefficients(int[][] dwt, String text) {
        // Convert the text into a binary string, where each character is represented by 8 bits
        String binaryText = "";
        for (char c : text.toCharArray()) {
            String binaryChar = Integer.toBinaryString(c);
            while (binaryChar.length() < 8) {
                // pad with 0s
                binaryChar = "0" + binaryChar;
            }
            binaryText += binaryChar;
        }
        System.out.println("binaryText: " + binaryText);

        // Loop over the d1, d2, d3, and d4 coefficients and embed each bit of the
        // binary string in the least significant bit of the coefficients
        int messageIndex = 0;
        for (int i = 0; i < dwt.length; i++) {
            for (int j = 0; j < dwt[i].length; j++) {
                if (messageIndex >= binaryText.length()) {
                    // message has been stored, so stop
                    System.out.println("breaked");
                    return dwt;
                }
                // set the least significant bit of the coefficient to the value of the corresponding message bit
                // dwt[i][j] = (coefficient & ~1) | bit;
                int coefficient = (int) dwt[i][j];
                if (coefficient%2==0){
                    if(binaryText.charAt(messageIndex)=='1') {
                        coefficient |= 1; // set the least significant bit
                    }
                }
                else{
                    if(binaryText.charAt(messageIndex)=='0') {
                        coefficient ^= 1; // set the least significant bit
                    }
                }
                dwt[i][j] = coefficient;
                messageIndex++;
            }
        }

        // Return the modified d1, d2, d3, and d4 coefficients
        return dwt;
    }

    // Extract the text from the given d1, d2, d3, and d4 coefficients
    public String extractTextFromCoefficients(int[][] dwt) {
        // Loop over the d1, d2, d3, and d4 coefficients and extract the bits from the
        // least significant bit of the coefficients
        String binaryText = "";
        String text = "";
        int bits = 0;
        for (int i = 0; i < dwt.length; i++) {
            for (int j = 0; j < dwt[i].length; j++) {
                // Get the current coefficient and the least significant bit

                int coefficient = dwt[i][j];
                bits++;

                // Add the bit to the binary string
                // binaryText += bit;
                binaryText += ((coefficient & 1) == 1) ? '1' : '0';
                if (bits==8){
                    if (binaryText.equals("00000000") || binaryText.equals("11111111")){
                        return text;
                    }
                    char c = (char) Integer.parseInt(binaryText, 2);
                    text += c;
                    binaryText = "";
                    bits = 0;
                }
            }
        }

        // Return the extracted binary string
        return text;
    }
}


