package main;

import container.others.State;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Utility class contains utility functions like
 * encryption functions, save and load functions, etc
 */
@Slf4j
public class Utility {

    /**
     * To save a serialized object at a relative location
     *
     * @param filename Location with name of file where Object is stored
     * @param object   Object which is going to be stored
     * @return true when object is stored successfully else false
     */
    public static boolean save(String filename, Object object) {
        try {
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(object);
            out.close();
            file.close();
            log.info("Object is saved");
        } catch (IOException ex) {
            log.error("IOException is caught");
            return false;
        }
        return true;
    }

    /**
     * Load Object stored at <code>filename</code>
     *
     * @param filename Location with filename where object is stored
     * @return Object loaded at given location else returns null
     */
    public static Object load(String filename) {
        Object object = null;
        try {
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);
            object = in.readObject();
            in.close();
            file.close();
            log.info("Object is loaded");
        } catch (IOException ex) {
            log.error("Unable to load: IOException is caught");
        } catch (ClassNotFoundException ex) {
            log.error("Unable to load: ClassNotFoundException is caught");
        }
        return object;
    }


    public static BufferedImage joinBufferedImage(Map<Integer, State> images) {
        if (images.size() == 0)
            return null;
        try {
            if (images.size() == 1) {
                log.info("Only 1 image passed");
                Iterator it = images.values().iterator();
                return resizeImage(((State) it.next()).getBufferedImage(), DAO.videoWidth, DAO.videoHeight);
            }
            Iterator it = images.values().iterator();
            BufferedImage img1 = ((State) it.next()).getBufferedImage();
            while (it.hasNext()) {
                img1 = joinImages(img1, ((State) it.next()).getBufferedImage());
            }
            return resizeImage(img1, DAO.videoWidth, DAO.videoHeight);
        } catch (NoSuchElementException e) {
            try {
                return ImageIO.read(new File("C:\\Users\\iOSDev474\\IdeaProjects\\Sakshatkar\\default.png"));
            } catch (IOException ex) {
                return null;
            }
        }
    }

    public static BufferedImage resizeImage(final Image image, int width, int height) {
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.Src);
        //below three lines are for RenderingHints for better image quality at cost of higher processing time
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }

    public static BufferedImage joinBufferedImage(BufferedImage[] images) {
        BufferedImage newImage;
        if (images.length < 2) {
            log.error("less than 2 images passed");
            return null;
        }
        newImage = joinImages(images[0], images[1]);
        for (int i = 2; i < images.length; i++) {
            newImage = joinImages(newImage, images[i]);
        }
        return newImage;
    }

    public static BufferedImage joinImages(BufferedImage img1, BufferedImage img2) {
        //do some calculate first
        int offset = 5;
        int wid = img1.getWidth() + img2.getWidth() + offset;
        int height = Math.max(img1.getHeight(), img2.getHeight()) + offset;
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(wid, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(Color.BLACK);
        g2.fillRect(0, 0, wid, height);
        //draw image
        g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, img1.getWidth() + offset, 0);
        g2.dispose();
        return newImage;
    }

    private static Rectangle rect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

    private static Robot robot = null;

    public static BufferedImage getDesktop() {
        if (robot == null) {
            try {
                robot = new Robot();
            } catch (AWTException e) {
                log.error("Unable to create a Robot instance");
            }
        }
        if (robot != null) {
            return resizeImage(robot.createScreenCapture(rect), DAO.videoWidth, DAO.videoHeight);
        } else {
            try {
                return ImageIO.read(new File(DAO.testJPGImage));
            } catch (IOException e) {
                log.error("Unable to read Image");
                return null;
            }
        }
    }

    public static String getSha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(value.getBytes());
            return bytesToHex(md.digest());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }


    public static void main(String args[]) {
        String filename = System.getProperty("user.home") + File.separator;
        try {
            BufferedImage img1 = ImageIO.read(new File(filename + "1.png"));
            BufferedImage img2 = ImageIO.read(new File(filename + "2.png"));
            BufferedImage img3 = ImageIO.read(new File(filename + "3.png"));
            BufferedImage img4 = ImageIO.read(new File(filename + "4.png"));
            BufferedImage[] images = {getDesktop(), img2, img3, img4};
            BufferedImage joinedImg = joinBufferedImage(images);
            boolean success = ImageIO.write(joinedImg, "png", new File(filename + "joined.png"));
            System.out.println("saved success? " + success);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}