import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.URISyntaxException;

import javax.swing.*;

public class MainClass extends JPanel {
	private static final long serialVersionUID = 1L;

	JFrame frame;
	public static String barTitle = "Installing Ambient Clock";
	Font bold, vegur;
	Image download, fail, background;

	public MainClass() {
		try {
			bold = Font.createFont(Font.PLAIN, new BufferedInputStream(MainClass.class.getResourceAsStream("VEGUR-BOLD.OTF"))).deriveFont(Font.PLAIN, 20f);
			vegur = Font.createFont(Font.PLAIN, new BufferedInputStream(MainClass.class.getResourceAsStream("Vegur-L 0602.otf"))).deriveFont(Font.PLAIN, 20f);
			vegur = Font.createFont(Font.PLAIN, new BufferedInputStream(MainClass.class.getResourceAsStream("SINKINSANS-100THIN_0.OTF"))).deriveFont(Font.PLAIN, 400f);
		} catch (FontFormatException | IOException e1) {
			e1.printStackTrace();
		}

		frame = new JFrame();

		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {

			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				if (!oldFileFound || !newFileFound || !configFileFound)
					System.exit(0);
			}

			public void mouseExited(MouseEvent e) {
			}
		});

		download = new ImageIcon(this.getClass().getResource("download.png")).getImage();
		fail = new ImageIcon(this.getClass().getResource("failure.png")).getImage();
		background = new ImageIcon(this.getClass().getResource("Backlight.png")).getImage();

		setFocusable(true);
	}

	public static String getJarFolder() {
		try {
			try {
				return new File(MainClass.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (URISyntaxException e) {}
		return null;
	}

	boolean oldFileFound, configFileFound, newFileFound;

	static File configFile, oldFile, updateFile;

	public void begin() {
		if ((configFile = new File(getJarFolder() + "/jarfile.updateconfig")).exists()) {
			configFileFound = true;
			try {

				BufferedReader r = new BufferedReader(new FileReader(configFile));

				String s = r.readLine();
				if (s != null && (oldFile = new File(s)).exists()) {
					oldFileFound = true;
				}
				if ((s = r.readLine()) != null && (updateFile = new File(s)).exists()) {
					newFileFound = true;
				}
				r.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (newFileFound && oldFileFound) {
			configFile.delete();
			oldFile.delete();
			updateFile.renameTo(oldFile);
			successful = true;
		}
	}

	boolean successful;

	public void finish() {
		try {
			Runtime.getRuntime().exec("java -jar \"" + oldFile.getCanonicalPath() + "\"");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void paint(Graphics gr) {
		super.paint(gr);
		Graphics2D g = (Graphics2D) gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		g.drawImage(background, getWidth(), 0, -getWidth(), getHeight(), null);

		g.setFont(bold);
		g.setColor(Color.white);

		g.drawString("Installation aborted", 100, 100);
		g.drawImage(fail, 100 - 16 - 16, 100 - 16, null);

		g.setFont(vegur);

		FontMetrics FM = g.getFontMetrics();

		String s = "";
		if (!configFileFound) {
			s = "The configuration file has gone missing.";
		} else if (!oldFileFound) {
			try {
				if (oldFile == null)
					s = "The configuration file is corrupt.";
				else
					s = "The file at \"" + oldFile.getCanonicalPath() + "\" is nonexistent.";
			} catch (IOException e) {}
		} else if (!newFileFound) {
			s = "The update file has gone missing.";
		}
		if (FM.stringWidth(s) > 300) {
			//			frame.setSize(FM.stringWidth(s) + 200, 300);
			frame.setLocationRelativeTo(null);
		}
		g.drawString(s, 100, 150);

		g.drawString("Click to close this window.", 100, 200);
		g.setColor(new Color(255, 255, 255, 200));

		try {
			g.drawString("Files searched:", 100, 250);
			if (configFile != null) {
				g.drawString(configFile.getCanonicalPath(), 100, 275);
			}
			if (oldFile != null) {
				g.drawString(oldFile.getCanonicalPath(), 100, 300);
			}
			if (updateFile != null) {
				g.drawString(updateFile.getCanonicalPath(), 100, 325);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void go() {
		long lastTime = System.nanoTime();
		final double ns = 1_000_000_000;
		double delta = 0;
		begin();
		while (!oldFileFound || !newFileFound) {
			if (!frame.isVisible())
				frame.setVisible(true);
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				repaint();
				delta--;
			}
		}
		finish();
	}

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (updateFile != null && updateFile.exists())
					updateFile.delete();
			}
		});

		MainClass host = new MainClass();
		host.frame.setTitle(MainClass.barTitle);
		host.frame.add(host);
		host.frame.setSize(1000, 400);
		host.frame.setUndecorated(true);
		host.frame.setLocationRelativeTo(null);
		host.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		host.frame.setResizable(false);
		host.go();
	}
}